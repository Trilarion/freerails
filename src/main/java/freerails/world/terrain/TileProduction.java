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

package freerails.world.terrain;

import java.io.Serializable;

/**
 * Represents the production of a raw material on a tile.
 */
public class TileProduction implements Serializable {

    private static final long serialVersionUID = 3258125847641536052L;
    // TODO why not class
    private final int cargoType;
    /**
     * The number of units per year (40 units = 1 car load).
     */
    private final int rate;

    /**
     * @param type
     * @param rate
     */
    public TileProduction(int type, int rate) {
        cargoType = type;
        this.rate = rate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TileProduction))
            return false;

        final TileProduction tileProduction = (TileProduction) o;

        if (cargoType != tileProduction.cargoType)
            return false;
        return rate == tileProduction.rate;
    }

    @Override
    public int hashCode() {
        int result;
        result = cargoType;
        result = 29 * result + rate;
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
    public int getRate() {
        return rate;
    }
}