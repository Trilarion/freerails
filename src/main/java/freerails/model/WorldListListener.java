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
package freerails.model;

import freerails.model.player.FreerailsPrincipal;

/**
 * Classes that need to be notified of changes to the lists on the world object
 * should implement this interface.
 */
public interface WorldListListener {

    /**
     * @param key
     * @param index
     * @param principal
     */
    void listUpdated(KEY key, int index, FreerailsPrincipal principal);

    /**
     * @param key
     * @param index
     * @param principal
     */
    void itemAdded(KEY key, int index, FreerailsPrincipal principal);

    /**
     * @param key
     * @param index
     * @param principal
     */
    void itemRemoved(KEY key, int index, FreerailsPrincipal principal);
}