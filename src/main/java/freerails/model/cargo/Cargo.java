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

import freerails.util.Identifiable;

/** Describes a cargo type, including its category and per-unit weight. */
public class Cargo extends Identifiable {

    private final String name;
    private final CargoCategory category;
    private final int unitWeight;

    /**
     * Creates a cargo type definition.
     *
     * @param id unique cargo identifier
     * @param name display name
     * @param category accounting and service category
     * @param unitWeight weight of one cargo unit
     */
    public Cargo(int id, String name, CargoCategory category, int unitWeight) {
        super(id);
        this.name = name;
        this.category = category;
        this.unitWeight = unitWeight;
    }

    /**
     * Returns the display name of this cargo type.
     *
     * @return cargo name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the category assigned to this cargo type.
     *
     * @return cargo category
     */
    public CargoCategory getCategory() {
        return category;
    }

    /**
     * Returns the weight of one cargo unit.
     *
     * @return unit weight
     */
    public int getUnitWeight() {
        return unitWeight;
    }

    /**
     * Returns the cargo name followed by its category.
     *
     * @return formatted cargo description
     */
    @Override
    public String toString() {
        return String.format("%s (%s)", name, category);
    }
}
