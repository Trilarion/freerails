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

/*
 *
 */
package freerails.model.world;

import freerails.model.player.Player;

/**
 * Classes that need to be notified of changes to the lists on the world object
 * should implement this interface.
 */
public interface WorldListListener {

    /**
     * @param index
     * @param player
     */
    void listUpdated(int index, Player player);

    /**
     * @param index
     * @param player
     */
    void itemAdded(int index, Player player);

    /**
     * @param index
     * @param player
     */
    void itemRemoved(int index, Player player);
}