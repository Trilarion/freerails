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

import freerails.model.track.NullTrackPiece;
import freerails.model.track.NullTrackType;
import freerails.model.track.TrackPiece;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A tile on the map.
 *
 * Instances are stored in a HashMap to avoid creating 100,000s of objects.
 */
// TODO find better name for what it really is
public class FullTerrainTile implements TerrainTile {

    /**
     *
     */
    public static final Serializable NULL = new FullTerrainTile(0);
    private static final long serialVersionUID = 3617574907538847544L;
    private static final Map<FullTerrainTile, FullTerrainTile> instances = new HashMap<>();
    private final TrackPiece trackPiece;
    private final int terrainType;

    private FullTerrainTile(int terrainType) {
        this.terrainType = terrainType;
        trackPiece = NullTrackPiece.getInstance();
    }

    private FullTerrainTile(int terrainType, TrackPiece trackPiece) {
        this.terrainType = terrainType;
        this.trackPiece = trackPiece;
    }

    /**
     * @param terrainType
     * @return
     */
    public static FullTerrainTile getInstance(int terrainType) {
        FullTerrainTile tile = new FullTerrainTile(terrainType);
        FullTerrainTile storedTile = instances.get(tile);
        if (storedTile != null) {
            return storedTile;
        }
        instances.put(tile, tile);

        return tile;
    }

    /**
     * @param terrainType
     * @param trackPiece
     * @return
     */
    public static FullTerrainTile getInstance(int terrainType, TrackPiece trackPiece) {
        FullTerrainTile tile = new FullTerrainTile(terrainType, trackPiece);

        FullTerrainTile storedTile = instances.get(tile);
        if (storedTile != null) {
            return storedTile;
        }
        instances.put(tile, tile);

        return tile;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        final FullTerrainTile that = (FullTerrainTile) obj;

        if (terrainType != that.terrainType) return false;
        return trackPiece != null ? trackPiece.equals(that.trackPiece) : that.trackPiece == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = (trackPiece != null ? trackPiece.hashCode() : 0);
        result = 29 * result + terrainType;
        return result;
    }

    protected Object readResolve() {
        FullTerrainTile storedTile = instances.get(this);
        if (storedTile != null) {
            return storedTile;
        }
        instances.put(this, this);
        return this;
    }

    /**
     * @return
     */
    public int getTerrainTypeID() {
        return terrainType;
    }

    @Override
    public String toString() {
        return "trackPiece=" + trackPiece.toString() + " and terrainType is " + terrainType;
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
        return trackPiece.getTrackTypeID() != NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER;
    }
}