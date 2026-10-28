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

package freerails.model.train;

import freerails.util.Identifiable;
import freerails.model.finance.Money;

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

    /**
     * Creates an engine type with its purchase cost, upkeep, and performance limits.
     *
     * @param id unique engine type identifier
     * @param name display name
     * @param price purchase price
     * @param upkeep upkeep cost
     * @param maximumSpeed maximum speed
     * @param maximumThrust maximum tractive force
     */
    public Engine(int id, String name, Money price, Money upkeep, int maximumSpeed, int maximumThrust) {
        super(id);
        this.name = name;
        this.price = price;
        this.upkeep = upkeep;
        this.maximumSpeed = maximumSpeed;
        this.maximumThrust = maximumThrust;
    }

    /** Returns the engine's display name. */
    public String getName() {
        return name;
    }

    /** Returns the purchase price of this engine type. */
    public Money getPrice() {
        return price;
    }

    /** Returns the upkeep cost of this engine type. */
    public Money getUpkeep() {
        return upkeep;
    }

    /** Returns the maximum speed of this engine type. */
    public int getMaximumSpeed() {
        return maximumSpeed;
    }

    /** Returns the maximum tractive force of this engine type. */
    public int getMaximumThrust() {
        return maximumThrust;
    }

    /** Returns the engine name and purchase price for display. */
    @Override
    public String toString() {
        return String.format("%s (%s)", name, price);
    }
}
