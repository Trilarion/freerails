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

package freerails.model.terrain;

import freerails.model.track.TrackPiece;

import java.io.Serializable;
import java.util.Objects;

/**
 * A tile on the map.
 *
 */
// TODO find better name for what it really is
public class TerrainTile implements Serializable {

    private static final long serialVersionUID = 3617574907538847544L;
    private final TrackPiece trackPiece;
    private final int terrainTypeId;

    public TerrainTile(int terrainTypeId) {
        this.terrainTypeId = terrainTypeId;
        trackPiece = null;
    }

    public TerrainTile(int terrainTypeId, TrackPiece trackPiece) {
        this.terrainTypeId = terrainTypeId;
        this.trackPiece = trackPiece;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        final TerrainTile that = (TerrainTile) obj;

        if (terrainTypeId != that.terrainTypeId) return false;
        return trackPiece != null ? trackPiece.equals(that.trackPiece) : that.trackPiece == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = trackPiece != null ? trackPiece.hashCode() : 0;
        result = 29 * result + terrainTypeId;
        return result;
    }

    /**
     * @return
     */
    public int getTerrainTypeId() {
        return terrainTypeId;
    }

    @Override
    public String toString() {
        return "trackPiece=" + trackPiece + " and terrainType is " + terrainTypeId;
    }

    /**
     * @return
     */
    public TrackPiece getTrackPiece() {
        return trackPiece;
    }

    /**
     * @return
     */
    public boolean hasTrack() {
        return trackPiece != null;
    }
}