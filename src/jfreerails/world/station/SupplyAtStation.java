package jfreerails.world.station;

import jfreerails.world.common.FreerailsSerializable;


/** This class represents the supply at a station. */
public class SupplyAtStation implements FreerailsSerializable {
    private final int[] supply;

    public SupplyAtStation(int[] cargoWaiting) {
        supply = (int[])cargoWaiting.clone();
    }

    /** Returns the number of car loads of the specified cargo that the station
     * supplies per year.
     */
    public int getSupply(int cargoType) {
        return supply[cargoType];
    }

    public boolean equals(Object o) {
        if (o instanceof SupplyAtStation) {
            SupplyAtStation test = (SupplyAtStation)o;

            if (this.supply.length != test.supply.length) {
                return false;
            }

            for (int i = 0; i < supply.length; i++) {
                if (supply[i] != test.supply[i]) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }
}