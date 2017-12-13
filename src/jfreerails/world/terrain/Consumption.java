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