package jfreerails.world.station;

import java.util.Vector;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.top.World;

/** This class represents the supply at a station. */

public class SupplyAtStation implements FreerailsSerializable {

	private World w;
	private int x;
	private int y;
	private int[] supply;
	//final int[] supply;
	private Vector supplies;
	private CalcCargoSupplyRateAtStation supplyRate;
	
	public SupplyAtStation(World world, int x, int y, int[] cargoWaiting) {
		this.w = world;
		this.x = x;
		this.y = y;
		supply = (int[]) cargoWaiting.clone();
		
		supplies = new Vector();
		
		//call the CaclCargoSupplyRateAtStation class here
		getSupplyRatesFromTiles();
	}
	
	/** Returns the number of car loads of the specified cargo that the station
	 * supplies per year.	 	
	 */
	public int getSupply(int cargoType){
		return supply[cargoType];
	}

	public boolean equals(Object o) {		
		if(o instanceof SupplyAtStation){
			SupplyAtStation test = (SupplyAtStation)o;
			return this.supply.equals(test.supply);
		}else{		
			return false;
		}
	}	
	
	public void getSupplyRatesFromTiles() {
		supplyRate = new CalcCargoSupplyRateAtStation(w,x,y);
		supplies = supplyRate.ScanAdjacentTiles();
		
		convertSupplyDataForArray();
	}
	
	//this isn't at all ideal, but for now its ok
	public void convertSupplyDataForArray() {
		System.out.println("supply size:" + supply.length);
		System.out.println("supplies size:" + supplies.size());
		
		for (int i=0; i<supply.length; i++) {
			supply[i] = ((CargoElementObject)supplies.elementAt(i)).getRate();
		}
	}

}