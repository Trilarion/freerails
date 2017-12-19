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

import freerails.world.terrain.City;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;

import java.util.NoSuchElementException;

/**
 * Class to find the nearest city and return that name, so that a station can be
 * named appropriately. Date: 12th April 2003
 *
 */
public class CalcNearestCity {
    private final int x;

    private final int y;

    private final ReadOnlyWorld w;

    /**
     *
     * @param world
     * @param x
     * @param y
     */
    public CalcNearestCity(ReadOnlyWorld world, int x, int y) {
        this.w = world;
        this.x = x;
        this.y = y;
    }

    /**
     *
     * @return
     */
    public String findNearestCity() {
        double cityDistance;
        String cityName = null;
        double tempDistance;
        City tempCity;

        if (w.size(SKEY.CITIES) > 0) {
            tempCity = (City) w.get(SKEY.CITIES, 0);
            cityDistance = getDistance(tempCity.getCityX(), tempCity.getCityY());
            cityName = tempCity.getCityName();

            for (int i = 1; i < w.size(SKEY.CITIES); i++) {
                tempCity = (City) w.get(SKEY.CITIES, i);
                tempDistance = getDistance(tempCity.getCityX(), tempCity
                        .getCityY());

                if (tempDistance < cityDistance) {
                    cityDistance = tempDistance;
                    cityName = tempCity.getCityName();
                }
            }

            return cityName;
        }

        throw new NoSuchElementException();
    }

    private double getDistance(int cityX, int cityY) {
        double distance = 0;
        double a = (this.x - cityX) * (this.x - cityX);
        double b = (this.y - cityY) * (this.y - cityY);

        distance = Math.sqrt(a + b);

        return distance;
    }
}