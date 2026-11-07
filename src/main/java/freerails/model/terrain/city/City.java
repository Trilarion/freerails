/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.model.terrain.city;

import freerails.util.Identifiable;
import freerails.util.IntPoint2D;

/**
 * Stores a city's stable identifier, display name, and map location.
 */
public final class City extends Identifiable {

    private final String name;
    private final IntPoint2D location;

    /**
     * Creates a city at the specified map location.
     *
     * @param id stable city identifier
     * @param name display name
     * @param location map coordinate of the city center
     */
    public City(int id, String name, IntPoint2D location) {
        super(id);
        this.name = name;
        this.location = location;
    }

    /**
     * Returns the city's display name.
     *
     * @return the display name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the map coordinate of the city center.
     *
     * @return the city center location
     */
    public IntPoint2D getLocation() {
        return location;
    }

    /** Returns the city name followed by its location. */
    @Override
    public String toString() {
        return String.format("%s %s", name, location);
    }
}
