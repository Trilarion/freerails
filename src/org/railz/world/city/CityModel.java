/*
 * Copyright (C) Scott Bennett
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

/**
 * @author Scott Bennett
 * Date 31st March 2003
 *
 * Class for a city. Simply storing the city name and x & y co-ords.
 * Possible potential for expansion?? Initial size of city, growth rate etc.???
 */
package org.railz.world.city;

import org.railz.world.common.FreerailsSerializable;


public class CityModel implements FreerailsSerializable {
    private String name;
    private int x;
    private int y;

    public CityModel(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getCityName() {
        return name;
    }

    public int getCityX() {
        return x;
    }

    public int getCityY() {
        return y;
    }
}
