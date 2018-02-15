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

package freerails.model.terrain;

import java.io.Serializable;

/**
 * Represents the demand for a certain cargo for consumption.
 */
public class TileConsumption implements Serializable {

    private static final long serialVersionUID = 3258133565631051064L;
    // TODO meaning of cargotype why not class
    private final int cargoType;

    // TODO put requisite limit outside, here only nominal consumption
    /**
     * The number of tiles that must be within the station radius before the
     * station demands the cargo.
     */
    private final int prerequisite;

    /**
     * @param cargoType
     * @param pq
     */
    public TileConsumption(int cargoType, int pq) {
        this.cargoType = cargoType;
        prerequisite = pq; // default value.
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TileConsumption)) return false;

        final TileConsumption tileConsumption = (TileConsumption) obj;

        if (cargoType != tileConsumption.cargoType) return false;
        return prerequisite == tileConsumption.prerequisite;
    }

    @Override
    public int hashCode() {
        int result;
        result = cargoType;
        result = 29 * result + prerequisite;
        return result;
    }

    /**
     * @return
     */
    public int getCargoType() {
        return cargoType;
    }

    /**
     * @return
     */
    public int getPrerequisite() {
        return prerequisite;
    }
}