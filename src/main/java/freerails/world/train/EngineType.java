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

package freerails.world.train;

import freerails.world.finances.Money;

import java.io.Serializable;

/**
 * Represents an engine type, for example 'Grass Hopper'. It
 * encapsulates the properties that are common to all engines of the same type.
 */
public class EngineType implements Serializable {

    private static final long serialVersionUID = 3617014130905592630L;
    private final String engineTypeName;
    private final Money maintenance;
    private final int maxSpeed; // speed in mph
    private final int powerAtDrawbar;
    private final Money price;

    /**
     * @param name
     * @param power
     * @param price
     * @param speed
     * @param maintenance
     */
    public EngineType(String name, int power, Money price, int speed, Money maintenance) {
        engineTypeName = name;
        powerAtDrawbar = power;
        this.price = price;
        maxSpeed = speed;
        this.maintenance = maintenance;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EngineType)) return false;
        EngineType other = (EngineType) obj;
        return engineTypeName.equals(other.engineTypeName)

                && powerAtDrawbar == other.powerAtDrawbar && price.equals(other.price) && maintenance.equals(other.maintenance) && maxSpeed == other.maxSpeed;
    }

    /**
     * @return
     */
    public String getEngineTypeName() {
        return engineTypeName;
    }

    /**
     * @return
     */
    public int getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * @return
     */
    public int getPowerAtDrawbar() {
        return powerAtDrawbar;
    }

    /**
     * @return
     */
    public Money getPrice() {
        return price;
    }

    @Override
    public int hashCode() {

        int result;
        result = powerAtDrawbar;
        result = 29 * result + engineTypeName.hashCode();
        result = 29 * result + price.hashCode();
        result = 29 * result + maintenance.hashCode();
        result = 29 * result + maxSpeed;
        return result;
    }

    @Override
    public String toString() {
        return engineTypeName;
    }
}