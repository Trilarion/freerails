package jfreerails.server;

import java.util.LinkedList;
import java.util.logging.Logger;
import jfreerails.controller.MoveReceiver;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.RejectedMove;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.World;


/**
 * A move executer which has the authority to reject moves
 * outright.
 * @author rob
 */
class AuthoritativeMoveExecuter implements MoveReceiver {
    private static final Logger logger = Logger.getLogger(AuthoritativeMoveExecuter.class.getName());
    private static final int MAX_UNDOS = 10;
    private final World world;
    private final MoveReceiver moveReceiver;
    private final LinkedList moveStack = new LinkedList();

    public AuthoritativeMoveExecuter(World w, MoveReceiver mr) {
        world = w;
        moveReceiver = mr;
    }

    /**
     * forwards move as a RejectedMove if it failed.
     */
    private void forwardMove(Move move, MoveStatus status) {
        if (moveReceiver == null) {
            return;
        }

        if (status != MoveStatus.MOVE_OK) {
            moveReceiver.processMove(new RejectedMove(move, status));
        } else {
            moveReceiver.processMove(move);
        }
    }

    void processMove(Move move, FreerailsPrincipal p) {
        /* TODO
         * if the server is submitting the move, then act on behalf of whoever
         * move was submitted for
        if (p.equals(Player.AUTHORITATIVE))
            p = move.getPrincipal();
         */
        moveStack.add(move);

        if (moveStack.size() > MAX_UNDOS) {
            moveStack.removeFirst();
        }

        MoveStatus ms;

        /* TODO
         * ms = move.doMove(world, p);
         */
        ms = move.doMove(world, Player.AUTHORITATIVE);

        /* retain mutex since order of forwarded moves is important */
        forwardMove(move, ms);
    }

    public void processMove(Move move) {
        /* TODO
        processMove(move, move.getPrincipal());
        */
        processMove(move, Player.AUTHORITATIVE);
    }

    /**
     * FIXME clients can undo each others moves.
     * FIXME information about the principal is lost.
     */
    public void undoLastMove() {
        if (moveStack.size() > 0) {
            Move m = (Move)moveStack.removeLast();
            MoveStatus ms;

            /* TODO
             * ms = m.undoMove(world, Player.NOBODY);
             */
            ms = m.undoMove(world, Player.AUTHORITATIVE);

            if (ms != MoveStatus.MOVE_OK) {
                logger.warning("Couldn't undo move!");

                /* push it back on the stack to prevent further
                 * out-of-order undos */
                moveStack.add(m);
            }

            forwardMove(m, ms);
        } else {
            logger.warning("No moves on stack.");
        }
    }
}