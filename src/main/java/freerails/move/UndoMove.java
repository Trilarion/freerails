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

import freerails.world.World;
import freerails.world.player.FreerailsPrincipal;

/**
 * Undoes the Move passed to its constructor.
 */
@SuppressWarnings("unused")
public class UndoMove implements Move {
    private static final long serialVersionUID = 3977582498051929144L;

    private Move moveToUndo;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof UndoMove))
            return false;

        final UndoMove undoMove = (UndoMove) obj;

        return moveToUndo.equals(undoMove.moveToUndo);
    }

    @Override
    public int hashCode() {
        return moveToUndo.hashCode();
    }

    public MoveStatus tryDoMove(World world, FreerailsPrincipal principal) {
        return moveToUndo.tryUndoMove(world, principal);
    }

    public MoveStatus tryUndoMove(World world, FreerailsPrincipal principal) {
        return moveToUndo.tryDoMove(world, principal);
    }

    public MoveStatus doMove(World world, FreerailsPrincipal principal) {
        return moveToUndo.undoMove(world, principal);
    }

    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        return moveToUndo.undoMove(world, principal);
    }

    /**
     * @return
     */
    public Move getUndoneMove() {
        return moveToUndo;
    }
}