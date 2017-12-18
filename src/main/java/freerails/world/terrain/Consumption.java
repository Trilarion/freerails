/*
 * Created on 27-Apr-2003
 *
 */
package freerails.world.terrain;

import freerails.world.common.FreerailsSerializable;

/**
 * This class represents the demand for a certain cargo for consumption.
 *
 * @author Luke
 */
public class Consumption implements FreerailsSerializable {
    private static final long serialVersionUID = 3258133565631051064L;

    private final int cargoType;

    /**
     * The number of tiles that must be within the station radius before the
     * station demands the cargo.
     */
    private final int prerequisite;

    /**
     *
     * @param ct
     * @param pq
     */
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
        return prerequisite == consumption.prerequisite;
    }

    @Override
    public int hashCode() {
        int result;
        result = cargoType;
        result = 29 * result + prerequisite;
        return result;
    }

    /**
     *
     * @return
     */
    public int getCargoType() {
        return cargoType;
    }

    /**
     *
     * @return
     */
    public int getPrerequisite() {
        return prerequisite;
    }
}