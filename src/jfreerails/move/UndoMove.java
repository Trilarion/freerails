package jfreerails.move;
import jfreerails.world.World;

/**
 *
 *
 *
 * @author lindsal
 */

final public class UndoMove implements Move {

	private final Move move;

	public Move getMove() {
		return move;
	}

	public MoveStatus tryDoMove(World world) {
		return null;
	}

	public MoveStatus tryUndoMove(World world) {
		return null;
	}

	public MoveStatus doMove(World world) {
		return null;
	}

	public MoveStatus undoMove(World world) {
		return null;
	}

	public UndoMove(Move m) {

		this.move = m;
	}

}