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

package freerails.world.track;

import freerails.world.FreerailsSerializable;

/**
 * Defines methods to access the properties of the track on a tile.
 *
 */
public interface TrackPiece extends FreerailsSerializable {

    /**
     *
     * @return
     */
    int getTrackGraphicID();

    /**
     *
     * @return
     */
    int getTrackTypeID();

    /**
     *
     * @return
     */
    TrackRule getTrackRule();

    /**
     *
     * @return
     */
    TrackConfiguration getTrackConfiguration();

    /**
     *
     * @return
     */
    int getOwnerID();
}