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

package freerails.model.operations.station;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents the supply at a station.
 *
 * @param supplyByCargoId the available quantity for each cargo identifier
 */
public record StationSupply(Map<Integer, Integer> supplyByCargoId) implements Serializable {

    /**
     * Creates a station supply from the supplied values.
     *
     * @param supplyByCargoId the identifier to use
     */
    public StationSupply {
        supplyByCargoId = Map.copyOf(supplyByCargoId);
    }

    /**
     * Returns the carloads supplied per year for a cargo type.
     *
     * @param cargoId stable cargo identifier
     * @return annual number of carloads supplied
     */
    public int getSupply(int cargoId) {
        return supplyByCargoId.getOrDefault(cargoId, 0);
    }

}