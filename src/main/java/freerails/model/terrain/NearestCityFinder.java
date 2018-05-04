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

import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;

import java.util.NoSuchElementException;

/**
 * Finds the nearest city and returns that name, so that a station can be
 * named appropriately.
 */
public class NearestCityFinder {

    private final Vec2D location;
    private final UnmodifiableWorld world;

    /**
     * @param world
     * @param location
     */
    public NearestCityFinder(UnmodifiableWorld world, Vec2D location) {
        this.world = world;
        this.location = location;
    }

    /**
     * @return
     */
    public String findNearestCity() {
        double closestDistance = Double.MAX_VALUE;
        String cityName = null;

        for (City2 city: world.getCities()) {
            double distance = getDistance(city.getLocation());
            if (distance < closestDistance) {
                closestDistance = distance;
                cityName = city.getName();
            }
        }

        if (cityName != null) {
            return cityName;
        } else {
            throw new NoSuchElementException();
        }
    }

    private double getDistance(Vec2D cityLocation) {
        Vec2D delta = Vec2D.subtract(location, cityLocation);
        return delta.norm();
    }
}