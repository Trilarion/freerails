package jfreerails.move;

import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.World;


/**
 * Undoes the Move passed to its constructor.
 * @author luke
 */
public class UndoMove implements Move {
    private Move move2undo;

    /**
    * @param move The move that was undone
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