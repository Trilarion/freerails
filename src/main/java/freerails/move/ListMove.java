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

import freerails.world.FreerailsSerializable;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;

/**
 * This interface provides information about changes to the lists in the World
 * database.
 *
 */
public interface ListMove extends Move {
    /**
     * @return the type of object which was changed
     */
    KEY getKey();

    /**
     * @return the old item or null if not any.
     */
    FreerailsSerializable getBefore();

    /**
     * @return the new item or null if not any.
     */
    FreerailsSerializable getAfter();

    /**
     * @return the index of the item which changed.
     */
    int getIndex();

    /**
     *
     * @return
     */
    FreerailsPrincipal getPrincipal();
}