/*
 * Created on 27-Apr-2003
 *
 */
package jfreerails.world.terrain;

import jfreerails.world.common.FreerailsSerializable;


/**This class represents the production of a raw material on a tile.
 * @author Luke
 *
 */
public class Production implements FreerailsSerializable {
    private final int cargoType;

    /** The number of units per year (40 units = 1 car load) */
    private final int rate;

    public Production(int type, int rate) {
        this.cargoType = type;
        this.rate = rate;
    }

    public int getCargoType() {
        return cargoType;
    }

    public int getRate() {
        return rate;
    }
}