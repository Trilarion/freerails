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
import java.util.Collection;
import java.util.Set;

/**
 * Represents the demand for cargo at a station.
 */
public record StationDemand(Set<Integer> demandedCargoIds) implements Serializable {

    public StationDemand {
        demandedCargoIds = Set.copyOf(demandedCargoIds);
    }

    /**
     * Creates demand settings for the specified cargo IDs.
     *
     * @param demandedCargoIds stable IDs of demanded cargo types
     */
    public StationDemand(Collection<Integer> demandedCargoIds) {
        this(Set.copyOf(demandedCargoIds));
    }

    /**
     * Tests whether a cargo type is demanded at this station.
     *
     * @param cargoId stable cargo identifier
     * @return {@code true} if the station demands that cargo type
     */
    public boolean isCargoDemanded(int cargoId) {
        return demandedCargoIds.contains(cargoId);
    }
}