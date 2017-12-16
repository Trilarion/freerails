package freerails.world.station;

import freerails.world.common.FreerailsSerializable;
import freerails.world.common.ImInts;

/**
 * This class represents the demand for cargo at a station.
 *
 * @author Luke
 */
public class Demand4Cargo implements FreerailsSerializable {
    private static final long serialVersionUID = 3257565088071038009L;

    private final ImInts demand;

    public Demand4Cargo(boolean[] demandArray) {
        demand = ImInts.fromBoolean(demandArray);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Demand4Cargo))
            return false;

        final Demand4Cargo demandAtStation = (Demand4Cargo) o;

        if (!demand.equals(demandAtStation.demand))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;

        for (int i = 0; i < demand.size(); i++) {
            result = 29 * result + demand.get(i);
        }

        return result;
    }

    public boolean isCargoDemanded(int cargoNumber) {
        return demand.get(cargoNumber) == 1;
    }

}