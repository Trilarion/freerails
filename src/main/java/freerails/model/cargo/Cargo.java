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

import freerails.model.Identifiable;

/**
 * Represents a certain type of cargo.
 */
public class Cargo extends Identifiable {

    private final String name;
    private final CargoCategory category;
    private final int unitWeight;

    /**
     *
     * @param id
     * @param name
     * @param category
     * @param unitWeight
     */
    public Cargo(int id, String name, CargoCategory category, int unitWeight) {
        super(id);
        this.name = name;
        this.category = category;
        this.unitWeight = unitWeight;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public CargoCategory getCategory() {
        return category;
    }

    /**
     *
     * @return
     */
    public int getUnitWeight() {
        return unitWeight;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return String.format("%s (%s)", name, category);
    }
}
