package jfreerails.world.station;

import jfreerails.world.common.FreerailsSerializable;

/** 
 * This class represents a station.
 * 
 * @author Luke
 *
 */

public class StationModel implements FreerailsSerializable {
	
	public int x;
	public int y;
	
	private final String name;
	
	/** What this station is building. */
	private ProductionAtEngineShop production;
	
	public StationModel(int x, int y, String stationName) {
		this.name = stationName;
		this.x = x;
		this.y = y;
		
	}
	
	public StationModel() {
		this.name = "No name";
		x = 0;
		y = 0;	
	}
	
	public String getStationName() {
		return name;
	}
	
	public int getStationX() {
		return x;
	}
	
	public int getStationY() {
		return y;
	}
	
	public ProductionAtEngineShop getProduction() {
		return production;
	}

	public void setProduction(ProductionAtEngineShop production) {
		this.production = production;
	}

}