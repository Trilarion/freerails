package jfreerails.server;

import jfreerails.controller.MoveExecuter;
import jfreerails.controller.MoveReceiver;
import jfreerails.move.RejectedMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.top.World;


/**
 * An implementation of MoveExecuter which has the authority to reject moves
 * outright.
 */
public class AuthoritativeMoveExecuter extends MoveExecuter {
    /**
     * @deprecated
     */
    public AuthoritativeMoveExecuter(World w, MoveReceiver mr, Object mutex) {
        super(w, mr, mutex);
    }

    /**
     * forwards move as a RejectedMove if it failed.
     */
    protected void forwardMove(Move move, MoveStatus status) {
        if (moveReceiver == null) {
            return;
        }

        if (status != MoveStatus.MOVE_OK) {
            moveReceiver.processMove(new RejectedMove(move, status));
        } else {
            moveReceiver.processMove(move);
        }
    }
}