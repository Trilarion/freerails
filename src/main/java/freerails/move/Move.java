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

import freerails.move.generator.MoveGenerator;
import freerails.model.world.World;
import freerails.model.player.FreerailsPrincipal;

import java.io.Serializable;

/**
 * All moves should implement this interface and obey the contract described below.
 *
 * (1) They should be immutable.
 *
 * (2) They should override {@code Object.equals()} to test for logical
 * equality.
 *
 * (3) They should store 'before' and 'after' values for all properties of the
 * world object that they change.
 *
 * (4) The changes they encapsulate should be stored in an address space
 * independent way, so that a move generated on a client can be serialised, sent
 * over a network, and then deserialized and executed on a server. To achieve
 * this, they should refer to items in the game world via either their
 * coordinates, e.g. tile 10,50, or their position in a list, e.g. train #4.
 *
 * (5) They should be undoable. To achieve this, they need to store the
 * information necessary to undo the change. E.g. a change-terrain-type move
 * might store the tile coordinates, the terrain type before the change and the
 * terrain type after the change.
 *
 * (6) The tryDoMove and tryUndoMove methods should test whether the move is
 * valid but leave the game world unchanged.
 *
 * @see MoveStatus
 * @see World
 * @see MoveGenerator
 */
public interface Move extends Serializable {

    // TODO when are we trying it, in the beginning of doMove itself or also before
    /**
     * Tests whether this Move can be executed on the specified world object.
     *
     * This method must leave the world object unchanged.
     */
    MoveStatus tryDoMove(World world, FreerailsPrincipal principal);

    /**
     * Tests whether this Move can be undone on the specified world object.
     *
     * This method must leave the world object unchanged.
     */
    MoveStatus tryUndoMove(World world, FreerailsPrincipal principal);

    // TODO does this method also has to tryDoMove at the beginning
    /**
     * Executes this move on the specified world object.
     */
    MoveStatus doMove(World world, FreerailsPrincipal principal);

    /**
     * If {@code doMove} has just been executed on the specified world
     * object, calling this method changes the state of the world object back to
     * how it was before {@code doMove} was called.
     */
    MoveStatus undoMove(World world, FreerailsPrincipal principal);
}