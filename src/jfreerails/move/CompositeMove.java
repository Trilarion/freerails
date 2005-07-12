/*
 * Created on 26-May-2003
 *
 */
package jfreerails.move;

import java.util.ArrayList;

import jfreerails.world.common.ImList;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.World;

/**
 * 
 * This Move may be subclassed to create a move composed of a number of
 * component Moves where atomicity of the move is required. This class defines a
 * number of methods which may not be subclassed - all changes must be
 * encapsulated as sub-moves of this move.
 * 
 * @author Luke
 */
public class CompositeMove implements Move {
	private static final long serialVersionUID = 3257289149391517489L;

	private final ImList<Move> m_moves;

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CompositeMove))
			return false;

		final CompositeMove compositeMove = (CompositeMove) o;

		if (!m_moves.equals(compositeMove.m_moves))
			return false;

		return true;
	}

	/**
	 * This method lets sub classes look at the moves.
	 */
	final Move getMove(int i) {
		return m_moves.get(i);
	}

	public int hashCode() {
		// This will do for now.
		return m_moves.size();
	}

	public final ImList<Move> getMoves() {
		return m_moves;
	}

	CompositeMove(ArrayList<Move> movesArrayList) {

		m_moves = new ImList<Move>(movesArrayList);
	}

	public CompositeMove(Move... moves) {
		this.m_moves = new ImList<Move>(moves);
	}

	public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
		// Since whether a move later in the list goes through could
		// depend on whether an ealier move has been executed, we need
		// actually execute moves, then undo them to test whether the
		// array of moves can be excuted ok.
		MoveStatus ms = doMove(w, p);

		if (ms.ok) {
			// We just wanted to see if we could do them so we undo them again.
			undoMoves(w, m_moves.size() - 1, p);
		}

		// If its not ok, then doMove would have undone the moves so we don't
		// need to undo them.
		return ms;
	}

	public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
		MoveStatus ms = undoMove(w, p);

		if (ms.isOk()) {
			redoMoves(w, 0, p);
		}

		return ms;
	}

	public final MoveStatus doMove(World w, FreerailsPrincipal p) {
		MoveStatus ms = compositeTest(w, p);

		if (!ms.ok) {
			return ms;
		}

		for (int i = 0; i < m_moves.size(); i++) {
			ms = m_moves.get(i).doMove(w, p);

			if (!ms.ok) {
				// Undo any moves we have already done.
				undoMoves(w, i - 1, p);

				return ms;
			}
		}

		return ms;
	}

	public final MoveStatus undoMove(World w, FreerailsPrincipal p) {
		MoveStatus ms = MoveStatus.MOVE_OK;

		for (int i = m_moves.size() - 1; i >= 0; i--) {
			ms = m_moves.get(i).undoMove(w, p);

			if (!ms.ok) {
				// Redo any moves we have already undone.
				redoMoves(w, i + 1, p);

				return ms;
			}
		}

		return ms;
	}

	private final void undoMoves(World w, int number, FreerailsPrincipal p) {
		for (int i = number; i >= 0; i--) {
			MoveStatus ms = m_moves.get(i).undoMove(w, p);

			if (!ms.ok) {
				throw new IllegalStateException(ms.message);
			}
		}
	}

	private final void redoMoves(World w, int number, FreerailsPrincipal p) {
		for (int i = number; i < m_moves.size(); i++) {
			MoveStatus ms = m_moves.get(i).doMove(w, p);

			if (!ms.ok) {
				throw new IllegalStateException(ms.message);
			}
		}
	}

	/**
	 * Subclasses may override this method to perform tests which pass or fail
	 * depending on the combination of moves making up this composite move.
	 */
	MoveStatus compositeTest(World w, FreerailsPrincipal p) {
		return MoveStatus.MOVE_OK;
	}

	public final String toString() {
		String s = "";

		for (int i = 0; i < m_moves.size(); i++) {
			s += m_moves.get(i).toString() + ((i > 0) ? ", " : "");
		}

		return s;
	}
}