/*
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

package org.railz.world.cargo;

import org.railz.world.common.FreerailsSerializable;
import org.railz.world.train.TransportCategory;


/** This class represents a type of cargo */
final public class CargoType implements FreerailsSerializable {
    private final String name;
    private final TransportCategory category;
    /** Base value of 1 unit of cargo */
    private final long baseValue;
    /** Time in Ticks over which the value of cargo depreciates by 50% */
    private final int halfLife;
    /** Time in Ticks after which a CargoBatch of this cargo type will expire */
    private final int expiryTime;

    public int getExpiryTime() {
	return expiryTime;
    }

    /**
     * @param elapsedTime the elapsed time in TimeTicks since the cargo was
     * made.
     * @return the unit value of the cargo adjusted for the specified amount
     * of elapsed time.
     */
    public long getAgeAdjustedValue(int elapsedTime) {
	return (long) (baseValue * Math.pow(2, -((double) elapsedTime /
			halfLife)));
    }

    /**
     * @return the value of a single unit of the cargo type
     */
    public long getBaseValue() {
	return baseValue;
    }

    public String getName() {
        return name;
    }

    /** Returns the name, replacing any underscores with spaces. */
    public String getDisplayName() {
        return this.name.replace('_', ' ');
    }

    public CargoType(String name, TransportCategory category, long
	    baseValue, int halfLife, int expiryTime) {
        this.category = category;
        this.name = name;
	this.baseValue = baseValue;
	this.halfLife = halfLife;
	this.expiryTime = expiryTime;
    }

    public String toString() {
       return "CargoType: category=" + category +
	   ", name=" + name + ", halfLife = " + halfLife + ", baseValue = " +
	   baseValue + ", expiryTime = " + expiryTime;
    }

    public TransportCategory getCategory() {
        return category;
    }
}
