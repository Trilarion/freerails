package jfreerails.world.track;

import java.io.ObjectStreamException;
import java.util.HashMap;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.terrain.TerrainTile;


/** A tile on the map.
 *
 * Instances are stored in a HashMap to avoid creating 100,000s of objects.
 * @author Luke
 */
public class FreerailsTile implements TrackPiece, TerrainTile,
    FreerailsSerializable {
    public static final FreerailsTile NULL = new FreerailsTile(0);
    private final TrackPiece trackPiece;
    private final int terrainType;
    private static HashMap<FreerailsTile, FreerailsTile> instances = new HashMap<FreerailsTile, FreerailsTile>();

    public static FreerailsTile getInstance(int terrainType) {
        FreerailsTile tile = new FreerailsTile(terrainType);

        if (instances.containsKey(tile)) {
            return instances.get(tile);
        }
		instances.put(tile, tile);

		return tile;
    }

    public static FreerailsTile getInstance(int terrainType,
        TrackPiece trackPiece) {
        FreerailsTile tile = new FreerailsTile(terrainType, trackPiece);

        if (instances.containsKey(tile)) {
            return instances.get(tile);
        }
		instances.put(tile, tile);

		return tile;
    }

    private Object readResolve() throws ObjectStreamException {
        if (instances.containsKey(this)) {
            return instances.get(this);
        }
		instances.put(this, this);

		return this;
    }

    private FreerailsTile(int terrainType) {
        this.terrainType = terrainType;
        this.trackPiece = NullTrackPiece.getInstance();
    }

    public int hashCode() {
        int result;
        result = (trackPiece != null ? trackPiece.hashCode() : 0);
        result = 29 * result + terrainType;

        return result;
    }

    private FreerailsTile(int terrainType, TrackPiece trackPiece) {
        this.terrainType = terrainType;
        this.trackPiece = trackPiece;
    }

    /*
     * @see TrackPiece#getTrackGraphicNumber()
     */
    public int getTrackGraphicID() {
        return trackPiece.getTrackGraphicID();
    }

    /*
     * @see TrackPiece#getTrackRule()
     */
    public TrackRule getTrackRule() {
        return trackPiece.getTrackRule();
    }

    /*
     * @see TrackPiece#getTrackConfiguration()
     */
    public TrackConfiguration getTrackConfiguration() {
        return trackPiece.getTrackConfiguration();
    }

    public boolean equals(Object o) {
        if (o instanceof FreerailsTile) {
            FreerailsTile test = (FreerailsTile)o;

            boolean trackPieceFieldsEqual = (this.trackPiece.equals(test.trackPiece));

            boolean terrainTypeFieldsEqual = (terrainType == test.getTerrainTypeID());

            if (trackPieceFieldsEqual && terrainTypeFieldsEqual) {
                return true;
            }
			return false;
        }
		return false;
    }

    public int getTerrainTypeID() {
        return terrainType;
    }

    public String toString() {
        return "trackPiece=" + trackPiece.toString() + " and terrainType is " +
        terrainType;
    }

    public TrackPiece getTrackPiece() {
        return trackPiece;
    }

    public int getOwnerID() {
        return trackPiece.getOwnerID();
    }

	public int getTrackTypeID() {		
		return trackPiece.getTrackTypeID();
	}
}