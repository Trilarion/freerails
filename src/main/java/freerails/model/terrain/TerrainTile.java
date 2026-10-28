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

package freerails.model.terrain;

import freerails.model.track.TrackPiece;

import java.io.Serializable;
import java.util.Objects;

// TODO find better name for what it really is

/**
 * A map tile containing a terrain type and, optionally, a track piece.
 */
public record TerrainTile(TrackPiece trackPiece, int terrainTypeId) implements Serializable {

    private static final long serialVersionUID = 3617574907538847544L;

    /**
     * Creates a tile with the specified terrain and no track.
     *
     * @param terrainTypeId identifier of the terrain on the tile
     */
    public TerrainTile(int terrainTypeId) {
        this(null, terrainTypeId);
    }

    /**
     * Creates a tile with the specified terrain and track piece.
     *
     * @param terrainTypeId identifier of the terrain on the tile
     * @param trackPiece    track piece present on the tile
     */
    public TerrainTile(int terrainTypeId, TrackPiece trackPiece) {
        this(trackPiece, terrainTypeId);
    }

    /**
     * Compares this tile's terrain identifier and track piece with another
     * object.
     *
     * @param obj object to compare
     * @return {@code true} when {@code obj} is an equal terrain tile
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        final TerrainTile that = (TerrainTile) obj;

        if (terrainTypeId != that.terrainTypeId) return false;
        return Objects.equals(trackPiece, that.trackPiece);
    }

    /**
     * @return the terrain type identifier
     */
    @Override
    public int terrainTypeId() {
        return terrainTypeId;
    }

    /**
     * Returns a readable representation of the track and terrain identifier.
     */
    @Override
    public String toString() {
        return "trackPiece=" + trackPiece + " and terrainType is " + terrainTypeId;
    }

    /**
     * @return the track piece, or {@code null} if the tile has no track
     */
    @Override
    public TrackPiece trackPiece() {
        return trackPiece;
    }

    /**
     * @return {@code true} if a track piece is present
     */
    public boolean hasTrack() {
        return trackPiece != null;
    }
}