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

package freerails.controller;

import freerails.util.Vector2D;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.terrain.City;

import java.util.NoSuchElementException;

/**
 * Finds the nearest city and returns that name, so that a station can be
 * named appropriately.
 */
class NearestCityFinder {

    // TODO use Vector2D
    private final int x;
    private final int y;
    private final ReadOnlyWorld world;

    /**
     * @param world
     * @param x
     * @param y
     */
    public NearestCityFinder(ReadOnlyWorld world, int x, int y) {
        this.world = world;
        this.x = x;
        this.y = y;
    }

    /**
     * @return
     */
    public String findNearestCity() {
        double cityDistance;
        String cityName;
        double tempDistance;
        City tempCity;

        if (world.size(SKEY.CITIES) > 0) {
            tempCity = (City) world.get(SKEY.CITIES, 0);
            cityDistance = getDistance(tempCity.getLocation());
            cityName = tempCity.getName();

            for (int i = 1; i < world.size(SKEY.CITIES); i++) {
                tempCity = (City) world.get(SKEY.CITIES, i);
                tempDistance = getDistance(tempCity.getLocation());

                if (tempDistance < cityDistance) {
                    cityDistance = tempDistance;
                    cityName = tempCity.getName();
                }
            }

            return cityName;
        }

        throw new NoSuchElementException();
    }

    private double getDistance(Vector2D cityLocation) {
        double distance;
        // TODO Vector arigthmetics, is this code duplicated
        double a = (x - cityLocation.x) * (x - cityLocation.x);
        double b = (y - cityLocation.y) * (y - cityLocation.y);

        distance = Math.sqrt(a + b);

        return distance;
    }
}