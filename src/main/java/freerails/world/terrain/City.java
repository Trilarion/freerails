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

import java.io.Serializable;

/**
 * A city.
 * <p>
 * Simply storing the city name and x & y co-ords.
 * Possible potential for expansion?? Initial size of city, growth rate etc.???
 */
public class City implements Serializable {

    private static final long serialVersionUID = 3256720697500709428L;
    private final String name;
    private final int x;
    private final int y;
    // TODO replace x, y by Pair<> or Point2D<>

    /**
     * @param s
     * @param xx
     * @param yy
     */
    public City(String s, int xx, int yy) {
        name = s;
        x = xx;
        y = yy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof City))
            return false;

        final City city = (City) o;

        if (x != city.x)
            return false;
        if (y != city.y)
            return false;
        return name.equals(city.name);
    }

    @Override
    public int hashCode() {
        int result;
        result = name.hashCode();
        result = 29 * result + x;
        result = 29 * result + y;
        return result;
    }

    /**
     * @return
     */
    public String getCityName() {
        return name;
    }

    /**
     * @return
     */
    public int getCityX() {
        return x;
    }

    /**
     * @return
     */
    public int getCityY() {
        return y;
    }

    @Override
    public String toString() {
        return name + " " + x + ", " + y;
    }
}