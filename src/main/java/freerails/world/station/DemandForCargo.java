package freerails.world.station;

import freerails.world.FreerailsSerializable;
import freerails.world.common.ImInts;

/**
 * This class represents the demand for cargo at a station.
 *
 */
public class DemandForCargo implements FreerailsSerializable {
    private static final long serialVersionUID = 3257565088071038009L;

    private final ImInts demand;

    /**
     *
     * @param demandArray
     */
    public DemandForCargo(boolean[] demandArray) {
        demand = ImInts.fromBoolean(demandArray);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DemandForCargo))
            return false;

        final DemandForCargo demandAtStation = (DemandForCargo) o;

        return demand.equals(demandAtStation.demand);
    }

    @Override
    public int hashCode() {
        int result = 0;

        for (int i = 0; i < demand.size(); i++) {
            result = 29 * result + demand.get(i);
        }

        return result;
    }

    /**
     *
     * @param cargoNumber
     * @return
     */
    public boolean isCargoDemanded(int cargoNumber) {
        return demand.get(cargoNumber) == 1;
    }

}