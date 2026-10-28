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

// TODO should this be used more often?

import org.jetbrains.annotations.NotNull;

/**
 * Stores the type and quantity of cargo in a wagon.
 */
public record WagonLoad(int quantity, int cargoType) implements Comparable<WagonLoad> {

    /**
     * Creates a description of a wagon's cargo quantity and cargo type.
     *
     * @param quantity  amount of cargo loaded in the wagon
     * @param cargoType cargo type identifier
     */
    public WagonLoad {
    }

    /**
     * Orders loads by quantity.
     *
     * @param o load to compare with
     * @return signed difference between the quantities
     */
    @Override
    public int compareTo(@NotNull WagonLoad o) {
        return quantity - o.quantity;
    }

    /**
     * Returns the cargo quantity represented by this load.
     *
     * @return cargo quantity
     */
    @Override
    public int quantity() {
        return quantity;
    }

    /**
     * Returns the identifier of the loaded cargo type.
     *
     * @return cargo type identifier
     */
    @Override
    public int cargoType() {
        return cargoType;
    }
}
