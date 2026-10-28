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

package freerails.model.cargo;

import java.io.Serializable;

// TODO equals, hashcode

/**
 * Describes a cargo type and its nonnegative production or consumption rate.
 */
public record CargoProductionOrConsumption(int cargoId, double rate) implements Serializable {

    /**
     * Creates a production or consumption rate for a cargo type.
     *
     * @param cargoId identifier of the affected cargo type
     * @param rate    quantity produced or consumed per applicable time unit
     * @throws IllegalArgumentException if the rate is negative or non-finite
     */
    public CargoProductionOrConsumption(int cargoId, double rate) {
        if (!Double.isFinite(rate) || rate < 0) {
            throw new IllegalArgumentException("rate must be finite and non-negative.");
        }
        this.cargoId = cargoId;
        this.rate = rate == 0 ? 0 : rate;
    }

    /**
     * Returns the identifier of the cargo type affected by this rate.
     *
     * @return cargo identifier
     */
    @Override
    public int cargoId() {
        return cargoId;
    }

    /**
     * Returns the configured production or consumption rate.
     *
     * @return configured rate
     */
    @Override
    public double rate() {
        return rate;
    }

    /**
     * Tests equality of the cargo identifier and rate.
     *
     * @param obj object to compare
     * @return {@code true} if both fields match
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CargoProductionOrConsumption other) {
            return cargoId == other.cargoId && rate == other.rate;
        }
        return false;
    }

    /**
     * Returns a hash code consistent with the cargo identifier and rate.
     *
     * @return hash code for this rate
     */
    @Override
    public int hashCode() {
        return cargoId + 31 * Double.hashCode(rate);
    }
}
