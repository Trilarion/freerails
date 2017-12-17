/*
 * Created on 27-Apr-2003
 *
 */
package freerails.world.terrain;

import freerails.world.common.FreerailsSerializable;

/**
 * This class represents the production of a raw material on a tile.
 *
 * @author Luke
 */
public class Production implements FreerailsSerializable {
    private static final long serialVersionUID = 3258125847641536052L;

    private final int cargoType;

    /**
     * The number of units per year (40 units = 1 car load).
     */
    private final int rate;

    public Production(int type, int r) {
        cargoType = type;
        rate = r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Production))
            return false;

        final Production production = (Production) o;

        if (cargoType != production.cargoType)
            return false;
        return rate == production.rate;
    }

    @Override
    public int hashCode() {
        int result;
        result = cargoType;
        result = 29 * result + rate;
        return result;
    }

    public int getCargoType() {
        return cargoType;
    }

    public int getRate() {
        return rate;
    }
}