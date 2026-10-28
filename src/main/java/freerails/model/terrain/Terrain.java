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

package freerails.model.terrain;

import freerails.util.Identifiable;
import freerails.model.cargo.CargoConversion;
import freerails.model.cargo.CargoProductionOrConsumption;
import freerails.model.finance.Money;
import freerails.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Describes a terrain type, including its category, construction costs, and
 * cargo production, conversion, and consumption behavior.
 */
public class Terrain extends Identifiable {

    private final String name;
    private final TerrainCategory category;
    private final Money rightOfWayCost;
    private final Money buildCost;
    private final List<CargoProductionOrConsumption> productions;
    private final List<CargoConversion> conversions;
    private final List<CargoProductionOrConsumption> consumptions;

    /**
     * Creates a terrain definition with the supplied cargo rules.
     *
     * @param id stable terrain identifier
     * @param name display name
     * @param category category used by map and city logic
     * @param rightOfWayCost cost of acquiring a right of way over this terrain
     * @param buildCost optional cost of building on this terrain
     * @param productions cargo production rules
     * @param conversions cargo conversion rules
     * @param consumptions cargo consumption rules
     */
    public Terrain(int id, String name, TerrainCategory category, Money rightOfWayCost, @Nullable Money buildCost, List<CargoProductionOrConsumption> productions, List<CargoConversion> conversions, List<CargoProductionOrConsumption> consumptions) {
        super(id);
        this.name = Utils.verifyNotNull(name);
        this.category = Utils.verifyNotNull(category);
        this.rightOfWayCost = Utils.verifyNotNull(rightOfWayCost);
        this.buildCost = buildCost;
        this.productions = Utils.verifyUnmodifiable(productions);
        this.conversions = Utils.verifyUnmodifiable(conversions);
        this.consumptions = Utils.verifyUnmodifiable(consumptions);
    }

    /** Returns the display name of this terrain type. */
    public String getName() {
        return name;
    }

    /** Returns the category that classifies this terrain type. */
    public TerrainCategory getCategory() {
        return category;
    }

    /** Returns the cost of obtaining a right of way across this terrain. */
    public Money getRightOfWayCost() {
        return rightOfWayCost;
    }

    /**
     * Returns the cost of building on this terrain, or {@code null} if no
     * separate build cost is defined.
     */
    public Money getBuildCost() {
        return buildCost;
    }

    /** Returns the immutable cargo production rules for this terrain. */
    public List<CargoProductionOrConsumption> getProductions() {
        return productions;
    }

    /** Returns the immutable cargo conversion rules for this terrain. */
    public List<CargoConversion> getConversions() {
        return conversions;
    }

    /** Returns the immutable cargo consumption rules for this terrain. */
    public List<CargoProductionOrConsumption> getConsumptions() {
        return consumptions;
    }
}
