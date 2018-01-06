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

import freerails.util.ImPoint;
import freerails.world.terrain.TileTransition;

import java.io.Serializable;

/**
 * Represents the track connecting two adjacent tiles.
 */
public class TrackSection implements Serializable {

    private static final long serialVersionUID = -3776624056097990938L;
    private final TileTransition tileTransition;
    private final ImPoint tile;

    /**
     * @param tileTransition
     * @param tile
     */
    public TrackSection(final TileTransition tileTransition, final ImPoint tile) {
        ImPoint otherTile = TileTransition.move(tile, tileTransition);
        if (tile.compareTo(otherTile) > 0) {
            this.tileTransition = tileTransition.getOpposite();
            this.tile = otherTile;
        } else {
            this.tileTransition = tileTransition;
            this.tile = tile;
        }
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((tileTransition == null) ? 0 : tileTransition.hashCode());
        result = PRIME * result + ((tile == null) ? 0 : tile.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final TrackSection other = (TrackSection) obj;
        if (tileTransition == null) {
            if (other.tileTransition != null)
                return false;
        } else if (!tileTransition.equals(other.tileTransition))
            return false;
        if (tile == null) {
            return other.tile == null;
        } else return tile.equals(other.tile);
    }

    @Override
    public String toString() {
        return tile.toString() + ' ' + tileTransition.toString();
    }

    /**
     * @return
     */
    public ImPoint tileA() {
        return tile;
    }

    /**
     * @return
     */
    public ImPoint tileB() {
        return TileTransition.move(tile, tileTransition);
    }

}
