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
	
	private SupplyAtStation supply;
	
	private DemandAtStation demand;
	
	private CargoWaitingAtStation waiting;
	
	/** What this station is building. */
	private ProductionAtEngineShop production;
	
	public StationModel(int x, int y, String stationName, int numberOfCargoTypes) {
		this.name = stationName;
		this.x = x;
		this.y = y;
		
		supply = new SupplyAtStation(new int[numberOfCargoTypes]);
		demand = new DemandAtStation(new boolean[numberOfCargoTypes]);
		waiting = new CargoWaitingAtStation(new int[numberOfCargoTypes]);
		
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

	public DemandAtStation getDemand() {
		return demand;
	}

	public SupplyAtStation getSupply() {
		return supply;
	}

	public CargoWaitingAtStation getWaiting() {
		return waiting;
	}

	public void setDemand(DemandAtStation demand) {
		this.demand = demand;
	}

	public void setSupply(SupplyAtStation supply) {
		this.supply = supply;
	}

	public void setWaiting(CargoWaitingAtStation waiting) {
		this.waiting = waiting;
	}

}