package jfreerails.world.station;

import jfreerails.world.common.FreerailsSerializable;

/** This class represents the demand for cargo at a station.  */

public class DemandAtStation implements FreerailsSerializable {

	final boolean[] demand;

	public DemandAtStation(boolean[] demand) {
		this.demand = (boolean[]) demand.clone(); //defensive copy.
	}

	public boolean isCargoDemanded(int cargoNumber) {
		return demand[cargoNumber];
	}

	public boolean equals(Object o) {
		if (o instanceof DemandAtStation) {
			DemandAtStation test = (DemandAtStation) o;
			return demand.equals(test.demand);
		} else {
			return false;
		}
	}

}
