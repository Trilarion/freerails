/*
 * Created on 27-Apr-2003
 *
 */
package jfreerails.world.terrain;

import jfreerails.world.common.FreerailsSerializable;

/**
 * This class represents the demand for a certain cargo for consumption.
 * 
 * @author Luke
 * 
 */
public class Consumption implements FreerailsSerializable {
    private static final long serialVersionUID = 3258133565631051064L;

    private final int cargoType;

    /**
     * The number of tiles that must be within the station radius before the
     * station demands the cargo.
     */
    private final int prerequisite;

    public Consumption(int ct, int pq) {
        cargoType = ct;
        prerequisite = pq; // default value.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Consumption))
            return false;

        final Consumption consumption = (Consumption) o;

        if (cargoType != consumption.cargoType)
            return false;
        if (prerequisite != consumption.prerequisite)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = cargoType;
        result = 29 * result + prerequisite;
        return result;
    }

    public int getCargoType() {
        return cargoType;
    }

    public int getPrerequisite() {
        return prerequisite;
    }
}