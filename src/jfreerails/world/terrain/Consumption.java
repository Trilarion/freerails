/*
 * Created on 27-Apr-2003
 *
 */
package jfreerails.world.terrain;

import jfreerails.world.common.FreerailsSerializable;


/** This class represents the demand for a certain cargo for consumption.
 *
 * @author Luke
 *
 */
public class Consumption implements FreerailsSerializable {
    private final int cargoType;

    /** The number of tiles that must be within the station radius before
     * the station demands the cargo.
     */
    private final int prerequisite;

    public Consumption(int cargoType) {
        this.cargoType = cargoType;
        prerequisite = 1; //default value.
    }

    public Consumption(int cargoType, int prerequisite) {
        this.cargoType = cargoType;
        this.prerequisite = prerequisite; //default value.
    }

    public int getCargoType() {
        return cargoType;
    }

    public int getPrerequisite() {
        return prerequisite;
    }
}