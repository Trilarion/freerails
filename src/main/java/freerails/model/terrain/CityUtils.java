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

import freerails.model.world.UnmodifiableWorld;
import freerails.util.Vec2D;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

/**
 *
 */
public final class CityUtils {

    private CityUtils() {
    }

    /**
     * Finds the nearest city and returns that name, so that a station can be
     * named appropriately.
     *
     * @return
     */
    public static String findNearestCity(@NotNull UnmodifiableWorld world, @NotNull Vec2D location) {
        double closestDistance = Double.MAX_VALUE;
        String cityName = null;

        for (City city: world.getCities()) {
            Vec2D delta = Vec2D.subtract(location, city.getLocation());
            double distance = delta.norm();
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
}
