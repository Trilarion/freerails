package jfreerails.move;

import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.World;


/**
 * Specifies a move that has been rejected (ie not executed) by the
 * MoveExecuter. This move has already been attempted and thus all attempts to
 * try/perform the move will fail.
 */
public class RejectedMove implements Move {
    private Move attemptedMove;
    private MoveStatus moveStatus;

    /**
     * @param attemptedMove the move that failed to complete successfully
     * @param result the result of attempting to process the move
     */
    public RejectedMove(Move attemptedMove, MoveStatus result) {
        this.attemptedMove = attemptedMove;
        moveStatus = result;
    }

    /**
     * @return the result that was obtained when the server attempted the move
     */
    public MoveStatus getMoveStatus() {
        return moveStatus;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        return MoveStatus.moveFailed(this.getClass().getName());
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        return MoveStatus.moveFailed(this.getClass().getName());
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        return MoveStatus.moveFailed(this.getClass().getName());
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        return MoveStatus.moveFailed(this.getClass().getName());
    }

    /**
     * @return the move that was attempted by the server
     */
    public Move getAttemptedMove() {
        return attemptedMove;
    }
}