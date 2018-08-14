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
public final class CargoProductionOrConsumption implements Serializable {

    private final int cargoId;
    private final double rate;

    public CargoProductionOrConsumption(int cargoId, double rate) {
        if (rate < 0) {
            throw new RuntimeException("rate cannot be negative.");
        }
        this.cargoId = cargoId;
        this.rate = rate;
    }

    public int getCargoId() {
        return cargoId;
    }

    public double getRate() {
        return rate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CargoProductionOrConsumption) {
            CargoProductionOrConsumption other = (CargoProductionOrConsumption) obj;
            return cargoId == other.cargoId && rate == other.rate;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return cargoId + 31 * Double.hashCode(rate);
    }
}
