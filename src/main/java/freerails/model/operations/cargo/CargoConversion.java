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

package freerails.model.operations.cargo;

import java.io.Serializable;

/**
 * Describes the conversion of one cargo type into another at a stated rate.

 * @param sourceCargoId the identifier of the cargo consumed during conversion
 * @param productCargoId the identifier of the cargo produced during conversion
 * @param conversionRate the quantity of product produced per unit of source cargo
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
        this.conversionRate = (conversionRate == 0.0d) ? 0.0d : conversionRate;
    }

}
