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

import freerails.world.FreerailsSerializable;

/**
 * This class represents the demand for a certain cargo for consumption.
 *
 */
public class Consumption implements FreerailsSerializable {

    private static final long serialVersionUID = 3258133565631051064L;
    private final int cargoType;

    /**
     * The number of tiles that must be within the station radius before the
     * station demands the cargo.
     */
    private final int prerequisite;

    /**
     *
     * @param ct
     * @param pq
     */
    public Consumption(int ct, int pq) {
        cargoType = ct;
        prerequisite = pq; // default value.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Consumption))
            return false;

        final Consumption consumption = (Consumption) o;

        if (cargoType != consumption.cargoType)
            return false;
        return prerequisite == consumption.prerequisite;
    }

    @Override
    public int hashCode() {
        int result;
        result = cargoType;
        result = 29 * result + prerequisite;
        return result;
    }

    /**
     *
     * @return
     */
    public int getCargoType() {
        return cargoType;
    }

    /**
     *
     * @return
     */
    public int getPrerequisite() {
        return prerequisite;
    }
}