package freerails.world.track;

import freerails.world.common.FreerailsSerializable;
import freerails.world.terrain.TerrainTile;

import java.io.ObjectStreamException;
import java.util.HashMap;

/**
 * A tile on the map.
 * <p>
 * Instances are stored in a HashMap to avoid creating 100,000s of objects.
 *
 * @author Luke
 */
public class FreerailsTile implements TerrainTile, FreerailsSerializable {
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final FreerailsTile that = (FreerailsTile) o;

        if (terrainType != that.terrainType)
            return false;
        if (trackPiece != null ? !trackPiece.equals(that.trackPiece)
                : that.trackPiece != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (trackPiece != null ? trackPiece.hashCode() : 0);
        result = 29 * result + terrainType;
        return result;
    }

    private static final long serialVersionUID = 3617574907538847544L;

    public static final FreerailsTile NULL = new FreerailsTile(0);

    private final TrackPiece trackPiece;

    private final int terrainType;

    private static HashMap<FreerailsTile, FreerailsTile> instances = new HashMap<FreerailsTile, FreerailsTile>();

    public static FreerailsTile getInstance(int terrainType) {
        FreerailsTile tile = new FreerailsTile(terrainType);
        FreerailsTile storedTile = instances.get(tile);
        if (storedTile != null) {
            return storedTile;
        }
        instances.put(tile, tile);

        return tile;
    }

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

    private Object readResolve() throws ObjectStreamException {
        FreerailsTile storedTile = instances.get(this);
        if (storedTile != null) {
            return storedTile;
        }
        instances.put(this, this);
        return this;
    }

    private FreerailsTile(int terrainType) {
        this.terrainType = terrainType;
        this.trackPiece = NullTrackPiece.getInstance();
    }

    private FreerailsTile(int terrainType, TrackPiece trackPiece) {
        this.terrainType = terrainType;
        this.trackPiece = trackPiece;
    }

    public int getTerrainTypeID() {
        return terrainType;
    }

    @Override
    public String toString() {
        return "trackPiece=" + trackPiece.toString() + " and terrainType is "
                + terrainType;
    }

    public TrackPiece getTrackPiece() {
        return trackPiece;
    }

    public boolean hasTrack() {
        return trackPiece.getTrackTypeID() != NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER;
    }
}