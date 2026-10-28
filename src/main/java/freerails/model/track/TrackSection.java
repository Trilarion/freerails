/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.model.track;

import freerails.util.Vec2D;
import freerails.model.terrain.TileTransition;

import java.io.Serializable;

/**
 * Represents the undirected track connection between two adjacent tiles.
 */
public class TrackSection implements Serializable {

    private static final long serialVersionUID = -3776624056097990938L;
    private final TileTransition tileTransition;
    private final Vec2D tile;

    /**
     * Creates a section from a tile and its outgoing transition. The stored
     * endpoint/direction is normalized so either traversal identifies the same
     * section.
     *
     * @param tileTransition transition from {@code tile} to the adjacent tile
     * @param tile origin tile coordinate
     */
    public TrackSection(final TileTransition tileTransition, final Vec2D tile) {

        // TODO this is to make it unique, but there are other ways, for example storing the other tile too, then it would even have a direction
        Vec2D otherTile = TileTransition.move(tile, tileTransition);
        if (tile.compareTo(otherTile) > 0) {
            this.tileTransition = tileTransition.getOpposite();
            this.tile = otherTile;
        } else {
            this.tileTransition = tileTransition;
            this.tile = tile;
        }
    }

    /**
     * Returns a hash code based on the normalized endpoints.
     *
     * @return this section's hash code
     */
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (tileTransition == null ? 0 : tileTransition.hashCode());
        result = PRIME * result + (tile == null ? 0 : tile.hashCode());
        return result;
    }

    /**
     * Tests equality of the normalized track sections.
     *
     * @param obj object to compare
     * @return {@code true} if {@code obj} represents the same section
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final TrackSection other = (TrackSection) obj;
        if (tileTransition == null) {
            if (other.tileTransition != null) return false;
        } else if (!tileTransition.equals(other.tileTransition)) return false;
        if (tile == null) {
            return other.tile == null;
        } else return tile.equals(other.tile);
    }

    /**
     * Returns a textual representation of the normalized tile and transition.
     *
     * @return section description
     */
    @Override
    public String toString() {
        return tile.toString() + ' ' + tileTransition.toString();
    }

    /**
     * Returns the coordinate of the normalized first endpoint.
     *
     * @return first tile coordinate
     */
    public Vec2D tileA() {
        return tile;
    }

    /**
     * Returns the coordinate of the other endpoint.
     *
     * @return second tile coordinate
     */
    public Vec2D tileB() {
        return TileTransition.move(tile, tileTransition);
    }

}
