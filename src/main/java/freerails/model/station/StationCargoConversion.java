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

import java.io.Serializable;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Maps source cargo IDs to their product cargo IDs at a station.
 * A source cargo absent from the map is not converted.
 */
public record StationCargoConversion(Map<Integer, Integer> conversions) implements Serializable {

    public StationCargoConversion {
        conversions = Map.copyOf(conversions);
    }

    /**
     * Returns the configured product cargo ID for a source cargo ID.
     *
     * @param sourceCargoId cargo ID to look up
     * @return the product cargo ID, or empty when no conversion is configured
     */
    public OptionalInt conversionFor(int sourceCargoId) {
        Integer productCargoId = conversions.get(sourceCargoId);
        return productCargoId == null ? OptionalInt.empty() : OptionalInt.of(productCargoId);
    }
}
