
package jfreerails.world.track;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.terrain.TerrainTile;


public class FreerailsTile
	implements TrackPiece, TerrainTile, FreerailsSerializable {

	public static final FreerailsTile NULL = new FreerailsTile(0);

	private final TrackPiece trackPiece;

	private  int terrainType;

	public FreerailsTile(int terrainType) {
		this.terrainType = terrainType;
		this.trackPiece = NullTrackPiece.getInstance();

	}
	
	public FreerailsTile(
		int terrainType,
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
//	public int getRGB() {
//		if(trackPiece== NullTrackPiece.getInstance()){
//			return terrainType.getRGB();
//		}else{
//			return 0;
//		}
//	}

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
//	public TerrainType getTerrainType() {
//		return terrainType;
//	}
	
//	public String getTerrainCategory() {
//		return terrainType.getTerrainCategory();
//	}
	
//	public int terrainRgb() {
//		return terrainType.getRGB();
//	}

	public boolean equals(Object o) {
		if(o instanceof FreerailsTile){
			FreerailsTile test = (FreerailsTile)o;
			
			boolean trackPieceFieldsEqual = (this.trackPiece.equals(test.getTrackPiece()));
			
			boolean terrainTypeFieldsEqual = (terrainType == test.getTerrainTypeNumber());
			if(trackPieceFieldsEqual && terrainTypeFieldsEqual ){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}		
	}

	public TrackPiece getTrackPiece() {
		return trackPiece;
	}
	public int getTerrainTypeNumber() {		
		return terrainType;
	}	
}