
package jfreerails.world.track;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.terrain.TerrainTile;
import jfreerails.world.terrain.TerrainType;


public class FreerailsTile
	implements TrackPiece, TerrainTile, FreerailsSerializable {

	public static final FreerailsTile NULL = new FreerailsTile(new TerrainType(){

		public String getTerrainTypeName() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getTerrainCategory() {
			return null;
		}
		public int getRGB() {
			// TODO Auto-generated method stub
			return 0;
		}
		
	});

	private final TrackPiece trackPiece;

	private final TerrainType terrainType;

	public FreerailsTile(TerrainType terrainType) {
		this.terrainType = terrainType;
		this.trackPiece = NullTrackPiece.getInstance();

	}
	public FreerailsTile(
		TerrainType terrainType,
		TrackPiece trackPiece) {
		this.terrainType = terrainType;
		this.trackPiece = trackPiece;

	}


	/*
	 * @see TrackPiece#getTrackGraphicNumber()
	 */
	public int getTrackGraphicNumber() {
		return trackPiece.getTrackGraphicNumber();
	}

	/*
	 * @see Tile#getRGB()
	 */
	public int getRGB() {
		if(trackPiece== NullTrackPiece.getInstance()){
			return terrainType.getRGB();
		}else{
			return 0;
		}
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

	/*
	 * @see TerrainType#getTerrainType()
	 */
	public TerrainType getTerrainType() {
		return terrainType;
	}
	
	public String getTerrainCategory() {
		return terrainType.getTerrainCategory();
	}
	public String getTypeName(){
		return terrainType.getTerrainTypeName();
	}
	public int terrainRgb() {
		return terrainType.getRGB();
	}

}