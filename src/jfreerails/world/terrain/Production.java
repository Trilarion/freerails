/*
 * Created on 27-Apr-2003
 *
 */
package jfreerails.world.terrain;

import jfreerails.world.common.FreerailsSerializable;

/**
 * This class represents the production of a raw material on a tile.
 * @author Luke
 *
 */
public class Production implements FreerailsSerializable {
    private final int cargoType;

    /**
     * The number of tonnes per year
     */
    private final int rate;

    public Production(int type, int rate) {
        this.cargoType = type;
        this.rate = rate;
    }

    /**
     * @return An index into the CARGO_TYPES table for the cargo type for
     * which this Production object measures production
     */
    public int getCargoType() {
        return cargoType;
    }

    /**
     * @return the rate of production of the cargo type in tonnes per year
     */
    public int getRate() {
        return rate;
    }
}
