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
 *
 * Date: 12th April 2003
 *
 * Class to find the nearest city and return that name, so that a train station
 * can be named appropriately.
 */
package org.railz.controller;

import org.railz.world.city.CityModel;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;


public class CalcNearestCity {
    private int x;
    private int y;
    private ReadOnlyWorld w;

    public CalcNearestCity(ReadOnlyWorld world, int x, int y) {
        this.w = world;
        this.x = x;
        this.y = y;
    }

    public String findNearestCity() {
        double cityDistance;
        String cityName = null;
        double tempDistance;
        CityModel tempCity;

        if (w.size(KEY.CITIES) > 0) {
            tempCity = (CityModel)w.get(KEY.CITIES, 0);
            cityDistance = getDistance(tempCity.getCityX(), tempCity.getCityY());
            cityName = tempCity.getCityName();

            for (int i = 1; i < w.size(KEY.CITIES); i++) {
                tempCity = (CityModel)w.get(KEY.CITIES, i);
                tempDistance = getDistance(tempCity.getCityX(),
                        tempCity.getCityY());

                if (tempDistance < cityDistance) {
                    cityDistance = tempDistance;
                    cityName = tempCity.getCityName();
                }
            }

            return cityName;
        }

        return null;
    }

    public double getDistance(int cityX, int cityY) {
        double distance = 0;
        double a = (this.x - cityX) * (this.x - cityX);
        double b = (this.y - cityY) * (this.y - cityY);

        distance = Math.sqrt(a + b);

        return distance;
    }
}
