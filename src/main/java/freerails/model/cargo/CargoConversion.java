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

package freerails.model.cargo;

import java.io.Serializable;

// TODO equals, hashcode
/**
 *
 */
public class CargoConversion implements Serializable {

    private final int sourceCargoId;
    private final int productCargoId;
    private final double conversionRate;

    public CargoConversion(int sourceCargoId, int productCargoId, double conversionRate) {
        if (conversionRate < 0) {
            throw new RuntimeException("conversion rate cannot be negative.");
        }
        this.sourceCargoId = sourceCargoId;
        this.productCargoId = productCargoId;
        this.conversionRate = conversionRate;
    }

    public int getSourceCargoId() {
        return sourceCargoId;
    }

    public int getProductCargoId() {
        return productCargoId;
    }

    public double getConversionRate() {
        return conversionRate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CargoConversion) {
            CargoConversion other = (CargoConversion) obj;
            return sourceCargoId == other.sourceCargoId && productCargoId == other.productCargoId && conversionRate == other.conversionRate;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = sourceCargoId;
        result = 31 * result + productCargoId;
        result = 31 * result + Double.hashCode(conversionRate);
        return result;
    }
}
