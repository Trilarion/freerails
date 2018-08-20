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

// TODO should this be used more often?

import org.jetbrains.annotations.NotNull;

/**
 * Stores the type and quantity of cargo in a wagon.
 */
public class WagonLoad implements Comparable<WagonLoad> {

    private final int quantity;
    private final int cargoType;

    public WagonLoad(int quantity, int cargoType) {
        this.quantity = quantity;
        this.cargoType = cargoType;
    }

    public int compareTo(@NotNull WagonLoad o) {
        return quantity - o.quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getCargoType() {
        return cargoType;
    }
}
