/*
 * Created on 26-May-2003
 *
 */
package jfreerails.move;

import java.util.ArrayList;
import jfreerails.world.top.World;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;

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
     * @return the first encountered player that is not Player.NOBODY,
     * otherwise return Player.NOBODY
     */
    public final FreerailsPrincipal getPrincipal() {
	for (int i = 0; i < moves.length; i++) {
	    if (! moves[i].getPrincipal().equals(Player.NOBODY))
		return moves[i].getPrincipal();
	}
	return Player.NOBODY;
    }

    /**
     * This method lets sub classes look at the moves.
     */
    protected final Move getMove(int i) {
        return moves[i];
    }

    public final Move[] getMoves() {
        return moves;
    }

    public CompositeMove(ArrayList movesArrayList) {
        moves = new Move[movesArrayList.size()];

        for (int i = 0; i < movesArrayList.size(); i++) {
            moves[i] = (Move)movesArrayList.get(i);
	    assert moves[i].getPrincipal() != null;
        }
    }

    public CompositeMove(Move[] moves) {
        this.moves = moves;
	for (int i = 0; i < moves.length; i++) 
	    assert moves[i].getPrincipal() != null;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        //Since whether a move later in the list goes through could
        //depend on whether an ealier move has been executed, we need
        //actually execute moves, then undo them to test whether the 
        //array of moves can be excuted ok.
        MoveStatus ms = doMove(w, p);

        if (ms.ok) {
            //We just wanted to see if we could do them so we undo them again.
            undoMoves(w, moves.length - 1, p);
        }

        //If its not ok, then doMove would have undone the moves so we don't need to undo them.
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
        MoveStatus ms = MoveStatus.MOVE_OK;

        for (int i = 0; i < moves.length; i++) {
            ms = moves[i].doMove(w, p);

            if (!ms.ok) {
		System.err.println("Move " + moves[i].toString() + " failed" +
		" because " + ms.toString());
                //Undo any moves we have already done.
                undoMoves(w, i - 1, p);

                return ms;
            }
        }

        return ms;
    }

    public final MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = MoveStatus.MOVE_OK;

        for (int i = moves.length - 1; i >= 0; i--) {
            ms = moves[i].undoMove(w, p);

            if (!ms.ok) {
                //Redo any moves we have already undone.
                redoMoves(w, i + 1, p);

                return ms;
            }
        }

        return ms;
    }

    private final void undoMoves(World w, int number, FreerailsPrincipal p) {
        for (int i = number; i >= 0; i--) {
            MoveStatus ms = moves[i].undoMove(w, p);

            if (!ms.ok) {
                throw new IllegalStateException(ms.message);
            }
        }
    }

    private final void redoMoves(World w, int number, FreerailsPrincipal p) {
        for (int i = number; i < moves.length; i++) {
            MoveStatus ms = moves[i].doMove(w, p);

            if (!ms.ok) {
                throw new IllegalStateException(ms.message);
            }
        }
    }

    public final boolean equals(Object o) {
        if (o instanceof CompositeMove) {
            CompositeMove test = (CompositeMove)o;

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

    public final String toString() {
        String s = "";

        for (int i = 0; i < moves.length; i++) {
            s += moves[i].toString() + ((i > 0) ? ", " : "");
        }

        return s;
    }
}
