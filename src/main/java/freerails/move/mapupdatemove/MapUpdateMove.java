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

package freerails.move.mapupdatemove;

import freerails.move.Move;

import java.awt.*;

/**
 * This interface tags Moves that change items on the map and tells the caller
 * which tiles have been updated. It is used by the map-view classes to
 * determine which tiles need repainting.
 */
public interface MapUpdateMove extends Move {

    /**
     * @return
     */
    Rectangle getUpdatedTiles();
}