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

package freerails.model.terrain;

import freerails.model.Identifiable;
import freerails.util.Vec2D;

// TODO Possible potential for expansion?? Initial size of city, growth rate etc.??? incorporate CityModel?
/**
 * Simply stores the name and x and y coordinates of a city.
 */
public class City extends Identifiable {

    private final String name;
    private final Vec2D location;

    public City(int id, String name, Vec2D location) {
        super(id);
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Vec2D getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return String.format("%s %s", name, location);
    }
}
