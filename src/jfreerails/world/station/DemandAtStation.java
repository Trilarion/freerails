package jfreerails.world.station;

import jfreerails.world.common.FreerailsSerializable;


/** This class represents the demand for cargo at a station.
 * @author Luke
 */
public class DemandAtStation implements FreerailsSerializable {
    private final boolean[] m_demand;

    public DemandAtStation( /*=const*/
        boolean[] demand) {
        m_demand = demand;
    }

    public boolean isCargoDemanded(int cargoNumber) {
        return m_demand[cargoNumber];
    }

    public int hashCode() {
        int result = 0;

        for (int i = 0; i < m_demand.length; i++) {
            result = 29 * result + (m_demand[i] ? 1 : 0);
        }

        return result;
    }

    public boolean equals(Object o) {
        if (o instanceof DemandAtStation) {
            DemandAtStation test = (DemandAtStation)o;

            if (this.m_demand.length != test.m_demand.length) {
                return false;
            }

            for (int i = 0; i < m_demand.length; i++) {
                if (m_demand[i] != test.m_demand[i]) {
                    return false;
                }
            }

            return true;
        }
		return false;
    }
}