package jfreerails.world.station;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.ImInts;

/**
 * This class represents the demand for cargo at a station.
 * 
 * @author Luke
 */
public class DemandAtStation implements FreerailsSerializable {
	private static final long serialVersionUID = 3257565088071038009L;

	private final ImInts m_demand;

	public DemandAtStation(boolean[] demand) {
		m_demand = ImInts.fromBoolean(demand);
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof DemandAtStation))
			return false;

		final DemandAtStation demandAtStation = (DemandAtStation) o;

		if (!m_demand.equals(demandAtStation.m_demand))
			return false;

		return true;
	}

	public int hashCode() {
		int result = 0;

		for (int i = 0; i < m_demand.size(); i++) {
			result = 29 * result + m_demand.get(i);
		}

		return result;
	}

	public boolean isCargoDemanded(int cargoNumber) {
		return m_demand.get(cargoNumber) == 1;
	}

}