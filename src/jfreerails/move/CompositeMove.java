/*
 * Created on 26-May-2003
 * 
 */
package jfreerails.move;

import jfreerails.world.top.World;

/**
 * 
 * This Move may be subclassed to create a move composed of a number of
 * component Moves where atomicity of the move is required.
 * This class defines a number of methods which may not be subclassed - all
 * changes must be encapsulated as sub-moves of this move.
 * 
 *  @author Luke
 */
public class CompositeMove implements Move {

	private Move[] moves;
	
	/**
	 * This method lets sub classes look at the moves.
	 */
	protected final Move getMove(int i){
		return moves[i];
	}	

	public final Move[] getMoves() {
	    return moves;
	}
	    
	public CompositeMove(Move[] moves) {
		this.moves = moves;
	}

	public MoveStatus tryDoMove(World w) {
		//Since whether a move later in the list goes through could
		//depend on whether an ealier move has been executed, we need
		//actually execute moves, then undo them to test whether the 
		//array of moves can be excuted ok.
		MoveStatus ms = doMove(w);

		if (ms.ok) {
			//We just wanted to see if we could do them so we undo them again.
			undoMoves(w, moves.length - 1);
		}
		//If its not ok, then doMove would have undone the moves so we don't need to undo them.
		return ms;
	}

	public MoveStatus tryUndoMove(World w) {
		MoveStatus ms = undoMove(w);
		if (ms.isOk()) {
			redoMoves(w, 0);		
		}
		return ms;
	}

	public final MoveStatus doMove(World w) {
		MoveStatus ms = MoveStatus.MOVE_OK;
		for (int i = 0; i < moves.length; i++) {
			ms = moves[i].doMove(w);
			if (!ms.ok) {
				//Undo any moves we have already done.
				undoMoves(w, i - 1);
				return ms;
			}
		}
		return ms;
	}

	public final MoveStatus undoMove(World w) {
		MoveStatus ms = MoveStatus.MOVE_OK;
		for (int i = moves.length - 1; i >= 0; i--) {
			ms = moves[i].undoMove(w);
			if (!ms.ok) {
				//Redo any moves we have already undone.
				redoMoves(w, i+1);
				return ms;
			}
		}
		return ms;
	}

	private final void undoMoves(World w, int number) {
		for (int i = number; i >= 0; i--) {
			MoveStatus ms = moves[i].undoMove(w);
			if (!ms.ok) {
				throw new IllegalStateException(ms.message);
			}
		}
	}

	private final void redoMoves(World w, int number) {
		for (int i = number; i < moves.length; i++) {
			MoveStatus ms = moves[i].doMove(w);
			if (!ms.ok) {
				throw new IllegalStateException(ms.message);
			}
		}
	}

	public final boolean equals(Object o) {
		if (o instanceof CompositeMove) {
			CompositeMove test = (CompositeMove) o;
			if (this.moves.length != test.moves.length) {
				return false;
			} else {
				for (int i = 0; i < this.moves.length; i++) {
					if (!this.moves[i].equals(test.moves[i])) {
						return false;
					}
				}
				return true;
			}
		} else {
			return false;
		}
	}
}
