package jfreerails.client.top;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Logger;
import jfreerails.client.common.ModelRoot;
import jfreerails.controller.MoveReceiver;
import jfreerails.controller.SychronizedQueue;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.RejectedMove;
import jfreerails.move.UndoMove;
import jfreerails.util.GameModel;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.Player;
import jfreerails.world.top.World;


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
public class NonAuthoritativeMoveExecuter implements MoveReceiver, GameModel {
    private static final Logger logger = Logger.getLogger(NonAuthoritativeMoveExecuter.class.getName());
    private final PendingQueue pendingQueue = new PendingQueue();
    private final ModelRoot modelRoot;
    private final MoveReceiver moveReceiver;
    private final World world;
    private final SychronizedQueue sychronizedQueue = new SychronizedQueue();
    private static final boolean debug = (System.getProperty(
            "jfreerails.move.NonAuthoritativeMoveExecuter.debug") != null);

    public NonAuthoritativeMoveExecuter(World w, MoveReceiver mr,
        ModelRoot modelRoot) {
        this.modelRoot = modelRoot;
        moveReceiver = mr;
        world = w;
    }

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
        }
    }

    /**
     * @return an UncommittedMoveReceiver which is used to submit moves to be
     * pre-committed. New moves are added at the end, and removed from the front
     * when committed.
     */
    public MoveReceiver getUncommittedMoveReceiver() {
        return pendingQueue;
    }

    public class PendingQueue implements MoveReceiver {
        /**
         * synchronize access to this list of unverified moves.
         */
        private final LinkedList pendingMoves = new LinkedList();
        private final ArrayList rejectedMoves = new ArrayList();
        private final LinkedList approvedMoves = new LinkedList();
        private MoveReceiver moveReceiver;

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

                ms = attempted.tryUndoMove(world, Player.AUTHORITATIVE);

                if (ms == MoveStatus.MOVE_OK) {
                    logger.warning("undoing " + attempted.toString());
                    attempted.undoMove(world, Player.AUTHORITATIVE);
                    rejectedMoves.remove(i);
                    forwardMove(new UndoMove(attempted), ms);
                    n++;
                }
            }

            if (n > 0) {
                modelRoot.setProperty(ModelRoot.QUICK_MESSAGE,
                    "Undid " + n + " moves rejected by " + "server!");
            }

            return (n > 0);
        }

        /**
         * Called when a move is accepted or rejected by the server.
         */
        private synchronized void moveCommitted(Move move) {
            MoveStatus ms;

            if (!pendingMoves.isEmpty()) {
                Move pendingMove = (Move)pendingMoves.getFirst();

                if (move instanceof RejectedMove) {
                    /* moves are rejected in order hence only the first can
                     * match*/
                    if (((RejectedMove)move).getAttemptedMove().equals(pendingMove)) {
                        /* Move was one of ours so we add it to the list of
                         * rejected moves and remove it from the list of pending
                         * moves */
                        rejectedMoves.add(move);
                        pendingMoves.removeFirst();

                        do {
                            if (!undoMoves()) {
                                break;
                            }

                            /* attempt to commit any moves which were previously
                             * blocked */
                            while (!approvedMoves.isEmpty()) {
                                Move am = (Move)approvedMoves.getFirst();

                                /* TODO
                                 * if (am.doMove(world, am.getPrincipal()) !=
                                 */
                                if (am.doMove(world, Player.AUTHORITATIVE) != MoveStatus.MOVE_OK) {
                                    break;
                                }

                                approvedMoves.removeFirst();
                            }
                        } while (!approvedMoves.isEmpty());

                        return;
                    }
                } else if (move.equals(pendingMove)) {
                    // move succeeded and we have already executed it
                    pendingMoves.removeFirst();

                    return;
                }
            }

            /* move must be from another client */
            if (!(move instanceof RejectedMove)) {
                /* TODO
                 * ms = move.doMove(world, move.getPrincipal());
                 */
                ms = move.doMove(world, Player.AUTHORITATIVE);

                if (ms != MoveStatus.MOVE_OK) {
                    logger.fine("Move " + move + " rejected " + "because " +
                        ms.toString());

                    /* move could not be committed because of
                     * a pre-commited move yet to be rejected by the server */
                    approvedMoves.addLast(move);
                } else {
                    forwardMove(move, ms);
                }

                return;
            }
        }

        /**
         * Pre-commits a move sent from the client.
         */
        public synchronized void processMove(Move move) {
            if (moveReceiver != null) {
                MoveStatus ms;

                ms = move.doMove(world, Player.AUTHORITATIVE);

                if (ms == MoveStatus.MOVE_OK) {
                    pendingMoves.add(move);
                    // send it to the client-side listeners
                    forwardMove(move, ms);
                    // send it to the server
                    moveReceiver.processMove(move);
                }
            }
        }

        public synchronized void addMoveReceiver(MoveReceiver mr) {
            if (moveReceiver == null) {
                moveReceiver = mr;
            }
        }
    }

    /**
     * Forwards moves after execution. This implementation forwards all
     * successful moves submitted. Subclasses may choose to override this to
     * forward moves differently.
     */
    private void forwardMove(Move move, MoveStatus status) {
        if (status != MoveStatus.MOVE_OK) {
            logger.warning("Couldn't commit move: " + status.message);

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
}