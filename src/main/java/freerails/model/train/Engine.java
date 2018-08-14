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

package freerails.model.train;

import freerails.model.Identifiable;
import freerails.model.finances.Money;

// TODO doesn't upkeep/maintenance increase over age of the engine?
/**
 * Represents an engine type, for example 'Grass Hopper'.
 * It encapsulates the properties that are common to all engines of the same type.
 */
public class Engine extends Identifiable {

    private final String name;
    private final Money price;
    private final Money upkeep;
    private final int maximumSpeed;
    private final int maximumThrust;

    public Engine(int id, String name, Money price, Money upkeep, int maximumSpeed, int maximumThrust) {
        super(id);
        this.name = name;
        this.price = price;
        this.upkeep = upkeep;
        this.maximumSpeed = maximumSpeed;
        this.maximumThrust = maximumThrust;
    }

    public String getName() {
        return name;
    }

    public Money getPrice() {
        return price;
    }

    public Money getUpkeep() {
        return upkeep;
    }

    public int getMaximumSpeed() {
        return maximumSpeed;
    }

    public int getMaximumThrust() {
        return maximumThrust;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name, price);
    }
}
