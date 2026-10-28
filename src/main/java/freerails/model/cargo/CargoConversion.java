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
 * Describes the conversion of one cargo type into another at a stated rate.
 */
public record CargoConversion(int sourceCargoId, int productCargoId, double conversionRate) implements Serializable {

    /**
     * Creates a cargo conversion rule.
     *
     * @param sourceCargoId  identifier of the cargo consumed
     * @param productCargoId identifier of the cargo produced
     * @param conversionRate amount of product per source unit
     * @throws IllegalArgumentException if the rate is negative or non-finite
     */
    public CargoConversion(int sourceCargoId, int productCargoId, double conversionRate) {
        if (!Double.isFinite(conversionRate) || conversionRate < 0) {
            throw new IllegalArgumentException("conversion rate must be finite and non-negative.");
        }
        this.sourceCargoId = sourceCargoId;
        this.productCargoId = productCargoId;
        this.conversionRate = conversionRate == 0 ? 0 : conversionRate;
    }

    /**
     * Returns the identifier of the cargo consumed by this conversion.
     *
     * @return source cargo identifier
     */
    @Override
    public int sourceCargoId() {
        return sourceCargoId;
    }

    /**
     * Returns the identifier of the cargo produced by this conversion.
     *
     * @return product cargo identifier
     */
    @Override
    public int productCargoId() {
        return productCargoId;
    }

    /**
     * Returns the amount of product produced per source unit.
     *
     * @return conversion rate
     */
    @Override
    public double conversionRate() {
        return conversionRate;
    }

    /**
     * Tests equality of the source type, product type, and conversion rate.
     *
     * @param obj object to compare
     * @return {@code true} if it describes the same conversion
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CargoConversion other) {
            return sourceCargoId == other.sourceCargoId && productCargoId == other.productCargoId && conversionRate == other.conversionRate;
        }
        return false;
    }

    /**
     * Returns a hash code consistent with the conversion fields.
     *
     * @return hash code for this conversion
     */
    @Override
    public int hashCode() {
        int result = sourceCargoId;
        result = 31 * result + productCargoId;
        result = 31 * result + Double.hashCode(conversionRate);
        return result;
    }
}
