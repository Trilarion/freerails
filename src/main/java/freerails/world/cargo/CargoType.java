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

package freerails.world.cargo;

import java.io.Serializable;

/**
 * Represents a type of cargo which consists of a category, a weight (per unit) and a name.
 */
public class CargoType implements Serializable {

    private static final long serialVersionUID = 3834874680581369912L;
    private final CargoCategory category;
    private final String name;
    private final int unitWeight;

    /**
     * @param unitWeight
     * @param name
     * @param category
     */
    public CargoType(int unitWeight, String name, CargoCategory category) {
        this.unitWeight = unitWeight;
        this.category = category;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CargoType)) return false;
        CargoType other = (CargoType) obj;
        return other.unitWeight == unitWeight && other.name.equals(name) && other.category == category;
    }

    /**
     * @return The category.
     */
    public CargoCategory getCategory() {
        return category;
    }

    /**
     * @return The name, replacing any underscores with spaces.
     */
    public String getDisplayName() {
        return name.replace('_', ' ');
    }

    /**
     * @return The name.
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        int result = unitWeight;
        result = 29 * result + category.hashCode();
        result = 29 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return name;
    }

}