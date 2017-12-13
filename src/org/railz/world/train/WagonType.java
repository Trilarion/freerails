/*
 * Copyright (C) Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.world.train;

import org.railz.world.common.FreerailsSerializable;


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
