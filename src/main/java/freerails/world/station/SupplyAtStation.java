package freerails.world.station;

import freerails.world.common.FreerailsSerializable;
import freerails.world.common.ImInts;

/**
 * This class represents the supply at a station.
 *
 * @author Luke
 */
public class SupplyAtStation implements FreerailsSerializable {
    private static final long serialVersionUID = 4049918272826847286L;

    private final ImInts supply;

    /**
     *
     * @param cargoWaiting
     */
    public SupplyAtStation(int[] cargoWaiting) {
        supply = new ImInts(cargoWaiting);
    }

    /**
     * Returns the number of car loads of the specified cargo that the station
     * supplies per year.
     * @param cargoType
     * @return 
     */
    public int getSupply(int cargoType) {
        return supply.get(cargoType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SupplyAtStation))
            return false;

        final SupplyAtStation supplyAtStation = (SupplyAtStation) o;

        return supply.equals(supplyAtStation.supply);
    }

    @Override
    public int hashCode() {
        return supply.hashCode();
    }

}