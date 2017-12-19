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

import java.io.Serializable;

/**
 * All moves should implement this interface and obey the contract described
 * below.
 * <p>
 * (1) They should be immutable.
 * <p>
 * <p>
 * (2) They should overide {@code Object.equals()} to test for logical
 * equality.
 * <p>
 * <p>
 * (3) They should store 'before' and 'after' values for all properties of the
 * world object that they change.
 * <p>
 * (4) The changes they encapsulate should be stored in an address space
 * independent way, so that a move generated on a client can be serialised, sent
 * over a network, and then deserialised and executed on a server. To achieve
 * this, they should refer to items in the game world via either their
 * coorinates, e.g. tile 10,50, or their position in a list, e.g. train #4.
 * <p>
 * <p>
 * (5) They should be undoable. To achieve this, they need to store the
 * information necessary to undo the change. E.g. a change-terrain-type move
 * might store the tile coorindates, the terrain type before the change and the
 * terrain type after the change.
 * <p>
 * <p>
 * (6) The tryDoMove and tryUndoMove methods should test whether the move is
 * valid but leave the gameworld unchanged
 *
 * @see MoveStatus
 * @see freerails.world.top.World
 * @see freerails.controller.PreMove
 */
public interface Move extends Serializable {
    /**
     * Tests whether this Move can be executed on the specifed world object,
     * this method should leave the world object unchanged.
     *
     * @param w
     * @param p
     * @return
     */
    MoveStatus tryDoMove(World w, FreerailsPrincipal p);

    /**
     * Tests whether this Move can be undone on the specifed world object, this
     * method should leave the world object unchanged.
     *
     * @param w
     * @param p
     * @return
     */
    MoveStatus tryUndoMove(World w, FreerailsPrincipal p);

    /**
     * Executes this move on the specifed world object.
     *
     * @param w
     * @param p
     * @return
     */
    MoveStatus doMove(World w, FreerailsPrincipal p);

    /**
     * If {@code doMove} has just been executed on the specified world
     * object, calling this method changes the state of the world object back to
     * how it was before {@code doMove} was called.
     *
     * @param w
     * @param p
     * @return
     */
    MoveStatus undoMove(World w, FreerailsPrincipal p);
}