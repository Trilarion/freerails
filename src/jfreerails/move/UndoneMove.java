package jfreerails.move;

import jfreerails.world.top.World;
import jfreerails.world.player.FreerailsPrincipal;

/**
 * Specifies a move that has been undone by the MoveExecuter. This move has
 * already been executed and thus all attempts to try/perform the move will
 * fail.
 */
public class UndoneMove implements Move {
    private Move undoneMove;

    /**
     * @param move The move that was undone
     */
    public UndoneMove(Move move) {
        undoneMove = move;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        return MoveStatus.MOVE_FAILED;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        return MoveStatus.MOVE_FAILED;
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        return MoveStatus.MOVE_FAILED;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        return MoveStatus.MOVE_FAILED;
    }

    public Move getUndoneMove() {
        return undoneMove;
    }

    public FreerailsPrincipal getPrincipal() {
	return undoneMove.getPrincipal();
    }
}
