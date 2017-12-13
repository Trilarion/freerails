/*
 * Copyright (C) 2003 Luke Lindsay
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

/*
 * Created on 28-Mar-2003
 *
 */
package org.railz.world.station;

import org.railz.world.common.FreerailsSerializable;


/**
 * This class represents the blue print for what a engine shop
 * is producing.
 *
 * @author Luke
 *
 */
public class ProductionAtEngineShop implements FreerailsSerializable {
    private final int engineType;
    private final int[] wagonTypes;

    public ProductionAtEngineShop(int e, int[] wagons) {
        engineType = e;
        wagonTypes = wagons;
    }

    public int getEngineType() {
        return engineType;
    }

    public int[] getWagonTypes() {
        return (int[])wagonTypes.clone(); //Defensive copy.
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

    /**
     *
     */
    public String toString() {
        return "engine type: " + this.engineType + ", with " +
        wagonTypes.length + "wagons";
    }
}
