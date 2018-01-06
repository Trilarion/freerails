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

package freerails.world.terrain;

import freerails.world.track.NullTrackPiece;
import freerails.world.track.NullTrackType;
import freerails.world.track.TrackPiece;

import java.io.Serializable;
import java.util.HashMap;

/**
 * A tile on the map.
 *
 * Instances are stored in a HashMap to avoid creating 100,000s of objects.
 */
// TODO find better name for what it really is
public class FreerailsTile implements TerrainTile {

    /**
     *
     */
    public static final FreerailsTile NULL = new FreerailsTile(0);
    private static final long serialVersionUID = 3617574907538847544L;
    private static final HashMap<FreerailsTile, FreerailsTile> instances = new HashMap<>();
    private final TrackPiece trackPiece;
    private final int terrainType;

    private FreerailsTile(int terrainType) {
        this.terrainType = terrainType;
        this.trackPiece = NullTrackPiece.getInstance();
    }

    private FreerailsTile(int terrainType, TrackPiece trackPiece) {
        this.terrainType = terrainType;
        this.trackPiece = trackPiece;
    }

    /**
     * @param terrainType
     * @return
     */
    public static FreerailsTile getInstance(int terrainType) {
        FreerailsTile tile = new FreerailsTile(terrainType);
        FreerailsTile storedTile = instances.get(tile);
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
    public static FreerailsTile getInstance(int terrainType,
                                            TrackPiece trackPiece) {
        FreerailsTile tile = new FreerailsTile(terrainType, trackPiece);

        FreerailsTile storedTile = instances.get(tile);
        if (storedTile != null) {
            return storedTile;
        }
        instances.put(tile, tile);

        return tile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final FreerailsTile that = (FreerailsTile) o;

        if (terrainType != that.terrainType)
            return false;
        return trackPiece != null ? trackPiece.equals(that.trackPiece) : that.trackPiece == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = (trackPiece != null ? trackPiece.hashCode() : 0);
        result = 29 * result + terrainType;
        return result;
    }

    private Object readResolve() {
        FreerailsTile storedTile = instances.get(this);
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
        return "trackPiece=" + trackPiece.toString() + " and terrainType is "
                + terrainType;
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