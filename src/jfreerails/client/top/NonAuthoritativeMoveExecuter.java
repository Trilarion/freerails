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

package jfreerails.client.top;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import jfreerails.client.model.ModelRoot;
import jfreerails.controller.MoveReceiver;
import jfreerails.controller.UncommittedMoveReceiver;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.RejectedMove;
import jfreerails.move.TimeTickMove;
import jfreerails.move.UndoneMove;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.top.World;
import jfreerails.util.GameModel;
import jfreerails.util.SychronizedQueue;


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
    static boolean debug = false;

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
     * Processes moves confirmed or rejected by the server.
     */
    private void executeOutstandingMoves() {
        FreerailsSerializable[] items = sychronizedQueue.read();

        for (int i = 0; i < items.length; i++) {
            Move move = (Move)items[i];
            pendingQueue.moveCommitted(move);
	    if (debug && ! (move instanceof TimeTickMove))
		System.err.println("pending: " + pendingQueue.pendingMoves.size());
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
        private ArrayList rejectedMoves = new ArrayList();
        private LinkedList approvedMoves = new LinkedList();
        private UncommittedMoveReceiver moveReceiver;

        private boolean undoMoves() {
            int n = 0;
            MoveStatus ms;

            for (int i = rejectedMoves.size() - 1; i >= 0; i--) {
                // attempt to undo moves starting with the last one
                RejectedMove rm = (RejectedMove)rejectedMoves.get(i);

                // our attempt to undo may fail due to a
                // pre-committed move which is yet to be
                // rejected
                Move attempted = rm.getAttemptedMove();

		ms = attempted.tryUndoMove(world, attempted.getPrincipal());

                if (ms == MoveStatus.MOVE_OK) {
		    attempted.undoMove(world, attempted.getPrincipal());
		    if (debug)
			System.err.println("Unrolled precommited move " +
			       	attempted.toString());
                    rejectedMoves.remove(i);
                    forwardMove(new UndoneMove(attempted), ms);
                    n++;
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
        private synchronized void moveCommitted(Move move) {
            MoveStatus ms;

            if (!pendingMoves.isEmpty()) {
                Move pendingMove = (Move)pendingMoves.getFirst();

                if (move instanceof RejectedMove) {
                    /* moves are rejected in order hence only the first can
                     * match*/
                    if (((RejectedMove)move).getAttemptedMove().equals(pendingMove)) {
			if (debug)
			    System.err.println("Logging rejected move " +
				    ((RejectedMove) move).getAttemptedMove());
                        /* Move was one of ours so we add it to the list of
                         * rejected moves and remove it from the list of pending
                         * moves */
                        rejectedMoves.add(move);
                        pendingMoves.removeFirst();

                        do {
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

				if (debug)
				    System.err.println("Committed queued move "
					    + am);
                                approvedMoves.removeFirst();
                            }
                        } while (!approvedMoves.isEmpty());

                        return;
                    }
                } else if (move.equals(pendingMove)) {
		    if (debug)
			System.err.println("Precommitted move acknowledged "
				+ "by server:" + move);
                    // move succeeded and we have already executed it
                    pendingMoves.removeFirst();

                    return;
                }
            }

            /* move must be from another client */
            if (!(move instanceof RejectedMove)) {
		if (debug && !(move instanceof TimeTickMove)) {
		    System.err.println("committing unknown move " + move);
		}
		ms = move.doMove(world, move.getPrincipal());

                if (ms != MoveStatus.MOVE_OK) {
                    if (debug) {
                        System.out.println("Queueing blocked move " +
			       move);
                    }

                    /* move could not be committed because of
                     * a pre-commited move yet to be rejected by the server */
                    approvedMoves.addLast(move);
                } else {
                    forwardMove(move, ms);
                }

                return;
            } else {
		if (debug) {
		    System.err.println("received unknown rejected move!" +
			    move);
		}
	    }
        }

        public synchronized void undoLastMove() {
            if (moveReceiver != null) {
                try {
                    pendingMoves.removeLast();
                } catch (NoSuchElementException e) {
                    // ignore
                }

                moveReceiver.undoLastMove();
            }
        }

        /**
         * pre-commits a move sent from the client
         */
        public synchronized void processMove(Move move) {
            if (moveReceiver != null) {
                MoveStatus ms;

		ms = move.doMove(world, move.getPrincipal());

                if (ms == MoveStatus.MOVE_OK) {
                    pendingMoves.add(move);
                    // send it to the client-side listeners
                    forwardMove(move, ms);
                    // send it to the server
                    moveReceiver.processMove(move);
                }
            }
        }

        public synchronized void addMoveReceiver(UncommittedMoveReceiver mr) {
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
            System.err.println("Couldn't commit move: " + status.message);

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
		debug =
		    modelRoot.getDebugModel().getClientMoveDebugModel().isSelected();
	    }
	};
}
