package jfreerails.move;

import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.World;

/**
 * Undoes the Move passed to its constructor.
 * 
 * @author luke
 */
public class UndoMove implements Move {
	private static final long serialVersionUID = 3977582498051929144L;

	private Move move2undo;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof UndoMove))
			return false;

		final UndoMove undoMove = (UndoMove) o;

		if (!move2undo.equals(undoMove.move2undo))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return move2undo.hashCode();
	}

	/**
	 * @param move
	 *            The move that was undone
	 */
	public UndoMove(Move move) {
		if (move instanceof UndoMove) {
			throw new IllegalArgumentException();
		}

		move2undo = move;
	}

	public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
		return move2undo.tryUndoMove(w, p);
	}

	public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
		return move2undo.tryDoMove(w, p);
	}

	public MoveStatus doMove(World w, FreerailsPrincipal p) {
		return move2undo.undoMove(w, p);
	}

	public MoveStatus undoMove(World w, FreerailsPrincipal p) {
		return move2undo.undoMove(w, p);
	}

	public Move getUndoneMove() {
		return move2undo;
	}
}