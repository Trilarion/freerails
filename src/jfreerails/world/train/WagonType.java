package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;


/**
 * This class represents a specific wagon type, for example 'mk IV 40 tonne
 * livestock wagon'.  It encapsulates the properties of a wagon that are common
 * to all wagons of the same type.
 *
 * @author Luke
 *
 */
public class WagonType implements FreerailsSerializable {
    private final String typeName;

    private final TransportCategory typeCategory;

    /**
     * capacity of this wagon type in arbitrary units (tonnes?)
     */
    private final int capacity;

    /**
     * index into the CARGO_TYPES table for the corresponding cargo type
     */
    private final int cargoType;
    
    /**
     * @param name descriptive name of the wagon type
     * @param category transport category of the wagon
     * @param capacity capacity of the wagon in arbitrary units
     * @param cargoType index into the CARGO_TYPES table for the corresponding
     * cargo type
     */
    public WagonType(String name, TransportCategory category, int capacity, int
	    cargoType) {
        typeName = name;
        typeCategory = category;
	this.cargoType = cargoType;
	this.capacity = capacity;
    }

    public String getName() {
        return typeName;
    }

    public TransportCategory getCategory() {
        return typeCategory;
    }

    /**
     * @return index into the CARGO_TYPES table
     */
    public int getCargoType() {
	return cargoType;
    }

    /**
     * @return the capacity of a wagon of this type in arbitrary units
     */
    public int getCapacity() {
	return capacity;
    }

    public String toString() {
       return "name=" + typeName + ", typeCategory=" + typeCategory +
       ", capacity=" + capacity + ", cargoType=" + cargoType;
    }

}
