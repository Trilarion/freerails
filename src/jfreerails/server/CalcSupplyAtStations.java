/** 
 * @author Scott Bennett
 * Created: 19th May 2003
 * 
 * This class loops through all of the known stations and recalculates
 * the cargoes that they supply.
 */

package jfreerails.server;

import java.util.Vector;

import jfreerails.world.station.StationModel;
import jfreerails.world.station.SupplyAtStation;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.World;

public class CalcSupplyAtStations {

	private World w;

	/**
	 * 
	 * Constructor, currently called from GUIComponentFactory
	 * 
	 * @param world The World object that contains all about the game world
	 * 
	 */
	public CalcSupplyAtStations(World world) {
		this.w = world;	
	}
	
	/**
	 * 
	 * Loop through each known station, call calculations method
	 * 
	 */
	public void doProcessing() {
		StationModel station;
		NonNullElements iterator = new NonNullElements(KEY.STATIONS, w);
		while(iterator.next()){				
			station = (StationModel)iterator.getElement();
			calculations(station);
		}
	}
	
	/**
	 * 
	 * Process each existing station, updating what is supplied to it
	 * 
	 * @param station A StationModel ojbect to be processed
	 * 
	 */
	public void calculations(StationModel station){
		int x = station.getStationX();
		int y = station.getStationY();
		
		//init vars
		CalcCargoSupplyRateAtStation supplyRate;
		Vector supply = new Vector();
		int[] cargoSupplied = new int[w.size(KEY.CARGO_TYPES)];
		
		//calculate the supply rates and put information into a vector
		supplyRate = new CalcCargoSupplyRateAtStation(w,x,y);
		supply = supplyRate.ScanAdjacentTiles();	
		
		//grab the supply rates from the vector
		for (int i=0; i<supply.size(); i++) {
			cargoSupplied[i] = ((CargoElementObject)supply.elementAt(i)).getRate();
		}
	
		//set the supply rates for the current station	
		SupplyAtStation supplyAtStation = new SupplyAtStation(cargoSupplied);
		station.setSupply(supplyAtStation);
		station.setDemand(supplyRate.getDemand());
	}
	
}