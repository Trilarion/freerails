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
	
	/** What this station is building. */
	private ProductionAtEngineShop production;
	
	public StationModel(int x, int y){
		this.x =x;
		this.y =y;
		
	}
	public StationModel(){
		x=0;
		y=0;	
	}
	
	public ProductionAtEngineShop getProduction() {
		return production;
	}

	public void setProduction(ProductionAtEngineShop production) {
		this.production = production;
	}

}