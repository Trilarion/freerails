/*
 * Created on 28-Mar-2003
 *
 */
package jfreerails.world.station;

import jfreerails.world.common.FreerailsSerializable;


/**
 * This class represents the blue print for what an engine shop
 * is producing.
 *
 * @author Luke
 *
 */
public class ProductionAtEngineShop implements FreerailsSerializable {
    private static final long serialVersionUID = 3545515106038592057L;
	private final int engineType;
    private final int[] wagonTypes;

    public ProductionAtEngineShop(int e, int[] wagons) {
        engineType = e;
        wagonTypes = wagons;
    }

    public int hashCode() {
        return engineType;
    }

    public int getEngineType() {
        return engineType;
    }

    public int[] getWagonTypes() {
        return wagonTypes.clone(); //Defensive copy.
    }

    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }

        if (!(o instanceof ProductionAtEngineShop)) {
            return false;
        }

        ProductionAtEngineShop other = (ProductionAtEngineShop)o;

        if (other.getEngineType() != this.engineType) {
            return false;
        }

        if (other.getWagonTypes().length != this.getWagonTypes().length) {
            return false;
        }

        int[] otherWagonTypes = other.getWagonTypes();

        for (int i = 0; i < this.getWagonTypes().length; i++) {
            if (wagonTypes[i] != otherWagonTypes[i]) {
                return false;
            }
        }

        return true;
    }

    public String toString() {
        return "engine type: " + this.engineType + ", with " +
        wagonTypes.length + "wagons";
    }
}