package jfreerails.move;

import jfreerails.world.top.World;


/**
 * Specifies a move that has been rejected by the server
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

    /**
     * @return the result that was obtained when the server attempted the move
     */
    public MoveStatus tryDoMove(World w) {
        return moveStatus;
    }

    public MoveStatus tryUndoMove(World w) {
        return MoveStatus.MOVE_FAILED;
    }

    public MoveStatus doMove(World w) {
        return MoveStatus.MOVE_FAILED;
    }

    public MoveStatus undoMove(World w) {
        return MoveStatus.MOVE_FAILED;
    }

    /**
     * @return the move that was attempted by the server
     */
    public Move getAttemptedMove() {
        return attemptedMove;
    }
}