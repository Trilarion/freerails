package jfreerails.client.top;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import jfreerails.controller.UncommittedMoveReceiver;
import jfreerails.controller.MoveExecuter;
import jfreerails.controller.MoveReceiver;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.RejectedMove;
import jfreerails.move.UndoneMove;
import jfreerails.world.top.World;
import jfreerails.client.common.UserMessageLogger;
import jfreerails.client.view.ModelRoot;


/**
 * An implementation of MoveExecuter which pre-commits moves on the outward trip
 * to the server, and rolls back rejected moves when received back from the
 * server.
 *
 * @author rtuck99@users.sourceforge.net
 */
public class NonAuthoritativeMoveExecuter extends MoveExecuter {
    private PendingQueue pendingQueue = new PendingQueue();
    private ModelRoot modelRoot;

    /**
     * @deprecated
     */
    public NonAuthoritativeMoveExecuter(World w, MoveReceiver mr, Object mutex,
        ModelRoot modelRoot) {
        super(w, mr, mutex);
        this.modelRoot = modelRoot;
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
        pendingQueue.moveCommitted(move);
    }

    public class PendingQueue implements UncommittedMoveReceiver {
        /**
         * synchronize access to this list of unverified moves
         */
        private LinkedList pendingMoves = new LinkedList();
        private ArrayList rejectedMoves = new ArrayList();
        private LinkedList approvedMoves = new LinkedList();
        private UncommittedMoveReceiver moveReceiver;
        int count = 0;

        private boolean undoMoves() {
            int n = 0;
            MoveStatus ms;

            for (int i = rejectedMoves.size() - 1; i >= 0; i--) {
                // attempt to undo moves starting with the last one
                RejectedMove rm = (RejectedMove)rejectedMoves.get(i);

                synchronized (mutex) {
                    // our attempt to undo may fail due to a
                    // pre-committed move which is yet to be
                    // rejected
                    ms = rm.getAttemptedMove().tryUndoMove(world);

                    if (ms == MoveStatus.MOVE_OK) {
                        rm.getAttemptedMove().undoMove(world);
                        rejectedMoves.remove(i);
                        forwardMove(new UndoneMove(rm.getAttemptedMove()), ms);
                        n++;
                    }
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

                                if (am.doMove(world) != MoveStatus.MOVE_OK) {
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
                synchronized (mutex) {
                    ms = move.doMove(world);
                }

                if (ms != MoveStatus.MOVE_OK) {
                    /* move could not be committed because of
                     * a pre-commited move yet to be rejected by the server */
                    approvedMoves.addLast(move);
                } else {
                    forwardMove(move, ms);
                }

                return;
            }
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

        public synchronized void removeMoveReceiver(UncommittedMoveReceiver mr) {
            moveReceiver = null;
        }
    }

    public void undoLastMove() {
        assert false : "attempted to undo move in client on return from server";
    }
}