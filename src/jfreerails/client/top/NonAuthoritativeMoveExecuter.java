package jfreerails.client.top;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import jfreerails.controller.UncommittedMoveReceiver;
import jfreerails.controller.MoveExecuter;
import jfreerails.controller.MoveReceiver;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.RejectedMove;
import jfreerails.world.top.World;


/**
 * An implementation of MoveExecuter which pre-commits moves on the outward trip
 * to the server, and rolls back rejected moves when received back from the
 * server.
 *
 * @author rtuck99@users.sourceforge.net
 */
public class NonAuthoritativeMoveExecuter extends MoveExecuter {
    private PendingQueue pendingQueue = new PendingQueue();

    /**
     * @deprecated
     */
    public NonAuthoritativeMoveExecuter(World w, MoveReceiver mr, Object mutex) {
        super(w, mr, mutex);
    }

    /**
     * @return an UncommittedMoveReceiver which is used to submit moves to be
     * pre-committed. New moves are added at the end, and removed from the front
     * when committed.
     */
    public UncommittedMoveReceiver getUncommittedMoveReceiver() {
        return pendingQueue;
    }

    /**
     * Processes moves confirmed or rejected by the server.
     */
    public synchronized void processMove(Move move) {
        forwardMove(move, pendingQueue.moveCommitted(move));
    }

    public class PendingQueue implements UncommittedMoveReceiver {
        /**
         * synchronize access to this list of unverified moves
         */
        private LinkedList pendingMoves = new LinkedList();
        private UncommittedMoveReceiver moveReceiver;

        /**
         * Called when a move is accepted or rejected by the server
         */
        private synchronized MoveStatus moveCommitted(Move move) {
            MoveStatus ms;

            if (!pendingMoves.isEmpty()) {
                Move pendingMove = (Move)pendingMoves.getFirst();

                if (move instanceof RejectedMove) {
                    /* moves are rejected in order hence only the first can
                     * match*/
                    if (move.equals(
                                ((RejectedMove)pendingMove).getAttemptedMove())) {
                        /* Move was one of ours so we must undo it */
                        try {
                            synchronized (mutex) {
                                while (pendingMove.undoMove(world) != MoveStatus.MOVE_OK) {
                                    /* keep uncommitting moves off the end of
                                     * the queue until all inhibiting moves are
                                     * undone
                                     */
                                    ms = ((Move)pendingMoves.removeLast()).undoMove(world);
                                    assert ms == MoveStatus.MOVE_OK : "Couldn't undo our last move";
                                }
                            }
                        } catch (NoSuchElementException e) {
                            assert false : "Undid all our moves and still " +
                            "couldn't undo failed move " + pendingMove;
                        }
                    }

                    /* return the original reason the move was rejected */
                    return ((RejectedMove)move).getMoveStatus();
                } else if (move.equals(pendingMove)) {
                    // move succeeded and we have already executed it
                    pendingMoves.removeFirst();

                    return MoveStatus.MOVE_OK;
                }
            }

            /* move must be from another client */
            if (!(move instanceof RejectedMove)) {
                synchronized (mutex) {
                    ms = move.doMove(world);
                }

                assert ms == MoveStatus.MOVE_OK;

                return ms;
            }

            return ((RejectedMove)move).getMoveStatus();
        }

        public synchronized void undoLastMove() {
            if (moveReceiver != null) {
                try {
                    pendingMoves.removeLast();
                } catch (NoSuchElementException e) {
                    // ignore
                }

                synchronized (mutex) {
                    moveReceiver.undoLastMove();
                }
            }
        }

        /**
         * pre-commits a move sent from the client
         */
        public synchronized void processMove(Move move) {
            if (moveReceiver != null) {
                MoveStatus ms;

                synchronized (mutex) {
                    ms = move.doMove(world);
                }

                if (ms == MoveStatus.MOVE_OK) {
                    pendingMoves.add(move);
                    moveReceiver.processMove(move);
                }
            }
        }

        public synchronized void addMoveReceiver(UncommittedMoveReceiver mr) {
            if (moveReceiver == null) {
                moveReceiver = mr;
            }
        }

        public synchronized void removeMoveReceiver(UncommittedMoveReceiver mr) {
            moveReceiver = null;
        }
    }

    public void undoLastMove() {
        assert false : "attempted to undo move in client on return from server";
    }
}