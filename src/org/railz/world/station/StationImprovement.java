/*
 * Copyright (C) 2004 Robert Tuck
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

package org.railz.world.station;

import org.railz.world.common.*;

/**
 * An instance of this class represents a particular type of station
 * improvement.
 */
public class StationImprovement implements FreerailsSerializable {
    static final long serialVersionUID = -5228217736356964948L;
    
    private String name;
    private String description;
    private long price;
    private int[] prerequisiteImprovements;
    private int[] replaces;

    /**
     * @return Indices into the STATION_IMPROVEMENTS table of the improvements
     * that this improvement replaces.
     */
    public int[] getReplacedImprovements() {
	return (int[]) replaces.clone();
    }

    /**
     * @return Indices into the STATION_IMPROVEMENTS table of the
     * prerequisites required to build this improvement
     */
    public int[] getPrerequisites() {
	return (int[]) prerequisiteImprovements.clone();
    }
    
    /** @return A resource key to a name by which this improvement is known */
    public String getName() {
	return name;
    }

    /** @return a resource key to a description of the Improvement */
    public String getDescription() {
	return description;
    }

    /** @return the base price of this improvement */
    public long getBasePrice() {
	return price;
    }

    public StationImprovement(String name, String description, long basePrice,
	    int[] prerequisites, int[] replaces)
	{
	    this.name = name;
	    this.description = description;
	    this.price = basePrice;
	    this.prerequisiteImprovements = prerequisites;
	    this.replaces = replaces;
	}

    public boolean equals(Object o) {
	if (!(o instanceof StationImprovement))
	    return false;

	StationImprovement si = (StationImprovement) o;
	
	return name.equals(si.name) &&
	    description.equals(si.description) &&
	    price == si.price;
    }

    public int hashCode() {
	return name.hashCode();
    }
}
