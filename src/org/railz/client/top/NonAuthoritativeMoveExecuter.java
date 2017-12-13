/*
 * Copyright (C) Robert Tuck
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.client.top;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.logging.*;

import org.railz.client.model.ModelRoot;
import org.railz.controller.*;
import org.railz.move.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.train.*;
import org.railz.util.GameModel;
import org.railz.util.SychronizedQueue;


/**
 * A move executer which pre-commits moves on the outward trip
 * to the server, and rolls back rejected moves when received back from the
 * server.
 *
 * TODO sort out "undo moves" mess.
 *
 * Requirements for Undo Move:
 * *Move must already have been performed
 * *Undo must not succeed if circumstances have changed such that the undo is
 * not possible.
 * *Undo cannot be performed more than once
 * *Client must receive notification of change.
 * *Order of composite undo must be reversed wrt forwards move.
 * *Undo must only be executable by client which performed the move.
 * *Undos must remain in stack for sufficient time for human to manually perform
 * them
 *
 * @author rtuck99@users.sourceforge.net
 */
public class NonAuthoritativeMoveExecuter implements UncommittedMoveReceiver,
    GameModel {
    private PendingQueue pendingQueue = new PendingQueue();
    private ModelRoot modelRoot;
    private MoveReceiver moveReceiver;
    private World world;
    private final SychronizedQueue sychronizedQueue = new SychronizedQueue();
    private static final Logger logger = Logger.getLogger("global");

    public NonAuthoritativeMoveExecuter(World w, MoveReceiver mr,
        ModelRoot modelRoot) {
        this.modelRoot = modelRoot;
        moveReceiver = mr;
        world = w;
	modelRoot.getDebugModel().getClientMoveDebugModel().
	    getAction().addPropertyChangeListener(debugChangeListener);
    }

    /**
     * @see MoveReceiver#processMove(Move)
     */
    public void processMove(Move move) {
        sychronizedQueue.write(move);
    }

    /**
     * Executed once per TimeTick
     */
    private void timeTickElapsed() {
    }

    /**
     * Processes moves confirmed or rejected by the server.
     */
    private void executeOutstandingMoves() {
        FreerailsSerializable[] items = sychronizedQueue.read();

        for (int i = 0; i < items.length; i++) {
            Move move = (Move)items[i];
            pendingQueue.moveCommitted(move);
	    if (move instanceof TimeTickMove)
		timeTickElapsed();
	    
	    if (logger.isLoggable(Level.FINE) &&
		    ! (move instanceof TimeTickMove))
		logger.log(Level.FINE, "pending: " +
		       	pendingQueue.pendingMoves.size());
        }
    }

    /**
     * @return an UncommittedMoveReceiver which is used to submit moves to be
     * pre-committed. New moves are added at the end, and removed from the front
     * when committed.
     */
    public UncommittedMoveReceiver getUncommittedMoveReceiver() {
        return pendingQueue;
    }

    public class PendingQueue implements UncommittedMoveReceiver {
        /**
         * synchronize access to this list of unverified moves
         */
        private LinkedList pendingMoves = new LinkedList();

	/** rejected but not yet undone moves */
        private ArrayList rejectedMoves = new ArrayList();

	/** moves approved by server but not yet committed */
        private LinkedList approvedMoves = new LinkedList();
        private UncommittedMoveReceiver moveReceiver;

	/** For debug purposes */
	int getNumBlockedMoves() {
	    return approvedMoves.size();
	}

        private boolean undoMoves() {
            int n = 0;
            MoveStatus ms;

	    synchronized (pendingMoves) {
		while (!pendingMoves.isEmpty()) {
		    Move m = (Move) pendingMoves.removeLast();
		    ms = m.undoMove(world, m.getPrincipal());
		    assert ms == MoveStatus.MOVE_OK;
		}
	    }

            for (int i = rejectedMoves.size() - 1; i >= 0; i--) {
                // attempt to undo moves starting with the last one
                RejectedMove rm = (RejectedMove)rejectedMoves.get(i);

                Move attempted = rm.getAttemptedMove();

		ms = attempted.tryUndoMove(world, attempted.getPrincipal());

                if (ms == MoveStatus.MOVE_OK) {
		    attempted.undoMove(world, attempted.getPrincipal());
		    logger.log(Level.INFO, "Unrolled precommited move " +
			       	attempted.toString());
                    rejectedMoves.remove(i);
                    forwardMove(new UndoneMove(attempted), ms);
                    n++;
                } else {
		    logger.log(Level.WARNING, "FAILED to undo move " +
			    attempted + " because " + ms);
		    assert false;
		}
            }

            if (n > 0) {
                modelRoot.getUserMessageLogger().println("Undid " + n +
                    " moves rejected by " + "server!");
            }

            return (n > 0);
        }

        /**
         * Called when a move is accepted or rejected by the server
         */
        private void moveCommitted(Move move) {
            MoveStatus ms;

	    Move pendingMove = null;
	    synchronized (pendingMoves) {
		if (!pendingMoves.isEmpty()) {
		    pendingMove = (Move)pendingMoves.removeFirst();
		}
	    }

	    if (pendingMove != null) {
                if (move instanceof RejectedMove) {
                    /* moves are rejected in order hence only the first can
                     * match*/
                    if (((RejectedMove)move).getAttemptedMove()
			    .equals(pendingMove)) {
			logger.log(Level.INFO, "Logging rejected move " +
				    ((RejectedMove) move).getAttemptedMove());
                        /* Move was one of ours so we add it to the list of
                         * rejected moves and remove it from the list of pending
                         * moves */
                        rejectedMoves.add(move);

                        do {
			    // undo all rejected moves
                            if (!undoMoves()) {
				assert false;
                                break;
                            }

                            /* attempt to commit any moves which were previously
                             * blocked */
                            while (!approvedMoves.isEmpty()) {
                                Move am = (Move)approvedMoves.getFirst();

				if (am.doMove(world, am.getPrincipal()) !=
					MoveStatus.MOVE_OK) {
                                    break;
                                }

				logger.log(Level.INFO, "Committed queued move "
					+ am);
                                approvedMoves.removeFirst();
                            }
                        } while (!approvedMoves.isEmpty());

                        return;
                    }
		    // rejected move was submitted  by another client.
                } else if (move.equals(pendingMove)) {
		    logger.log(Level.FINE, "Precommitted move acknowledged "
			    + "by server:" + move);
                    // move succeeded and we have already executed it
                    return;
                }
		// move succeeded but was submitted  by another client
		logger.log(Level.FINE, "Didn't recognise move: " + move + 
			    "<=>" + pendingMove);
            }

	    // Replace our pending move
	    if (pendingMove != null) {
		synchronized (pendingMoves) {
		    pendingMoves.addFirst(pendingMove);
		}
	    }

            /* move must be from another client */
            if (!(move instanceof RejectedMove)) {
		if (logger.isLoggable(Level.FINE) &&
		       	!(move instanceof TimeTickMove)) {
		    logger.log(Level.FINE, "committing unknown move " + move);
		}
		GameTime t = (GameTime) world.get(ITEM.TIME,
			Player.AUTHORITATIVE);
		ms = move.doMove(world, move.getPrincipal());

                if (ms != MoveStatus.MOVE_OK) {
		    if (pendingMoves.isEmpty()) {
			// there are no pre-committed moves, therefore our 
			// game-world is in sync with the server.
			// We have already received the information in this
			// move, this can occur when we load the game initially
			logger.log(Level.INFO, "Dropping redundant move " +
				"received from server:" + move);
			return;
		    }

		    logger.log(Level.INFO, "Queueing blocked move " +
			    move + " - " + ms.toString());

                    /* move could not be committed because of
                     * a pre-commited move yet to be rejected by the server */
                    approvedMoves.addLast(move);
                } else {
                    forwardMove(move, ms);
                }

                return;
            } else {
		logger.log(Level.FINE, "received unknown rejected move!" +
			move);
	    }
        }

        public void undoLastMove() {
            if (moveReceiver != null) {
                try {
		    synchronized (pendingMoves) {
			pendingMoves.removeLast();
		    }
                } catch (NoSuchElementException e) {
                    // ignore
                }

                moveReceiver.undoLastMove();
            }
        }

        /**
         * pre-commits a move sent from the client
         */
        public void processMove(Move move) {
            if (moveReceiver != null) {
                MoveStatus ms;

		ms = move.doMove(world, move.getPrincipal());

                if (ms == MoveStatus.MOVE_OK) {
		    synchronized (pendingMoves) {
			pendingMoves.add(move);
		    }
                    // send it to the client-side listeners
                    forwardMove(move, ms);
                    // send it to the server
                    moveReceiver.processMove(move);
                }
            }
        }

        public void addMoveReceiver(UncommittedMoveReceiver mr) {
            if (moveReceiver == null) {
                moveReceiver = mr;
            }
        }
    }

    public void undoLastMove() {
        assert false : "attempted to undo move in client on return from server";
    }

    /**
     * Forwards moves after execution. This implementation forwards all
     * successful moves submitted. Subclasses may choose to override this to
     * forward moves differently.
     */
    private void forwardMove(Move move, MoveStatus status) {
        if (status != MoveStatus.MOVE_OK) {
            logger.log(Level.FINE, "Couldn't commit move: " + status.message);

            return;
        }

        if (moveReceiver == null) {
            return;
        }

        moveReceiver.processMove(move);
    }

    public void update() {
        executeOutstandingMoves();
    }

    private PropertyChangeListener debugChangeListener = new
	PropertyChangeListener() {
	    public void propertyChange(PropertyChangeEvent e) {
		if (modelRoot.getDebugModel().getClientMoveDebugModel()
			.isSelected()) {
		    System.out.println("finer logging");
		    logger.setLevel(Level.FINER);
		} else {
		    System.out.println("info logging");
		    logger.setLevel(Level.INFO);
		}
	    }
	};
}
