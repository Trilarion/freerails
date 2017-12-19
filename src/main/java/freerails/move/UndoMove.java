/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.move;

import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.World;

/**
 * Undoes the Move passed to its constructor.
 */
public class UndoMove implements Move {
    private static final long serialVersionUID = 3977582498051929144L;

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UndoMove))
            return false;

        final UndoMove undoMove = (UndoMove) o;

        return move2undo.equals(undoMove.move2undo);
    }

    @Override
    public int hashCode() {
        return move2undo.hashCode();
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

    /**
     * @return
     */
    public Move getUndoneMove() {
        return move2undo;
    }
}