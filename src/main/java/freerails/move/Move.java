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

import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * All moves should implement this interface and obey the contract described below.
 *
 * (1) They should be immutable.
 *
 * (2) They should override {@code Object.equals()} to test for logical
 * equality.
 *
 * (4) The changes they encapsulate should be stored in an address space
 * independent way, so that a move generated on a client can be serialised, sent
 * over a network, and then deserialized and executed on a server. To achieve
 * this, they should refer to items in the game world via either their
 * coordinates, e.g. tile 10,50, or their position in a list, e.g. train #4.
 *
 * (6) The applicable method should test whether the move is
 * valid but leave the game world unchanged.
 *
 * @see Status
 * @see World
 */
public interface Move extends Serializable {

    /**
     * Tests whether this move can be applied on the specified world object.
     * The test must leave the world object unchanged.
     *
     * @param world
     * @return
     */
    @NotNull Status applicable(@NotNull UnmodifiableWorld world);

    /**
     * Applies the move on the specified world object.
     * It should not check if the move is applicable.
     *
     * @param world
     * @return
     */
    @NotNull void apply(@NotNull World world);

}