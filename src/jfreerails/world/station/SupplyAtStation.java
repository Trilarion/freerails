package jfreerails.world.station;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.ImInts;

/**
 * This class represents the supply at a station.
 * 
 * @author Luke
 */
public class SupplyAtStation implements FreerailsSerializable {
	private static final long serialVersionUID = 4049918272826847286L;

	private final ImInts supply;

	public SupplyAtStation(int[] cargoWaiting) {
		supply = new ImInts(cargoWaiting);
	}

	/**
	 * Returns the number of car loads of the specified cargo that the station
	 * supplies per year.
	 */
	public int getSupply(int cargoType) {
		return supply.get(cargoType);
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SupplyAtStation))
			return false;

		final SupplyAtStation supplyAtStation = (SupplyAtStation) o;

		if (!supply.equals(supplyAtStation.supply))
			return false;

		return true;
	}

	public int hashCode() {
		return supply.hashCode();
	}

}