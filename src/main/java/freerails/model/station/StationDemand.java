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

package freerails.model.station;


import freerails.util.Utils;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Represents the demand for cargo at a station.
 */
public class StationDemand implements Serializable {

    @Serial
    private static final long serialVersionUID = 3257565088071038009L;
    private final List<Integer> demand;

    /**
     * Creates demand settings from a boolean flag for each cargo type.
     *
     * @param demandArray demand flags indexed by the world's cargo ordering
     */
    public StationDemand(boolean[] demandArray) {
        demand = Utils.integerListFromBoolean(demandArray);
    }

    /**
     * Tests whether another demand object has the same cargo flags.
     *
     * @param obj object to compare
     * @return {@code true} if both demand lists match
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StationDemand demandAtStation)) return false;

        return demand.equals(demandAtStation.demand);
    }

    /**
     * Returns a hash code derived from the cargo demand flags.
     *
     * @return hash code for these demand settings
     */
    @Override
    public int hashCode() {
        int result = 0;

        for (Integer aDemand : demand) {
            result = 29 * result + aDemand;
        }

        return result;
    }

    /**
     * Tests whether a cargo type is demanded at this station.
     *
     * @param cargoIndex zero-based position in the world's cargo collection
     * @return {@code true} if the station demands that cargo type
     * @throws IndexOutOfBoundsException if the index is outside the configured
     *         cargo range
     */
    public boolean isCargoDemanded(int cargoIndex) {
        return demand.get(cargoIndex) == 1;
    }

}