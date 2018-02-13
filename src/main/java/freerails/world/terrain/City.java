/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.world.terrain;

import freerails.util.Vector2D;

import java.io.Serializable;

/**
 * A city.
 *
 * Simply storing the city name and x and y coordinates.
 * TODO Possible potential for expansion?? Initial size of city, growth rate etc.???
 */
public class City implements Serializable {

    private static final long serialVersionUID = 3256720697500709428L;
    private final String name;
    private final Vector2D location;

    /**
     * @param name
     * @param location
     */
    public City(String name, Vector2D location) {
        this.name = name;
        this.location = location;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof City)) return false;

        final City other = (City) obj;

        if (!location.equals(other.location)) return false;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result += 29 * location.hashCode();
        return result;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public Vector2D getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return name + ' ' + location.x + ", " + location.y;
    }
}