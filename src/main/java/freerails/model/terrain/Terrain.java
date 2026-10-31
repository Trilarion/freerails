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

import com.google.gson.annotations.SerializedName;
import freerails.model.operations.cargo.CargoConversion;
import freerails.model.operations.cargo.CargoProductionOrConsumption;
import freerails.model.economy.Money;
import freerails.util.Identifiable;
import freerails.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.util.List;

/**
 * Describes a terrain type, including its category, construction costs, and
 * cargo production, conversion, and consumption behavior.
 */
public final class Terrain extends Identifiable {

    @Serial
    private static final long serialVersionUID = 4402566762869522453L;

    private final String name;
    private final TerrainCategory category;
    @SerializedName(value = "trackConstructionCostPerMile", alternate = {"rightOfWayCost"})
    private final Money rightOfWayCost;
    @SerializedName(value = "industryBuildCost", alternate = {"buildCost"})
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
     * @param trackConstructionCostPerMile cost of constructing track across this terrain per mile
     * @param industryBuildCost optional cost of constructing an industry on this terrain
     * @param productions cargo production rules
     * @param conversions cargo conversion rules
     * @param consumptions cargo consumption rules
     */
    public Terrain(int id, String name, TerrainCategory category, Money trackConstructionCostPerMile, @Nullable Money industryBuildCost, List<CargoProductionOrConsumption> productions, List<CargoConversion> conversions, List<CargoProductionOrConsumption> consumptions) {
        super(id);
        this.name = Utils.verifyNotNull(name);
        this.category = Utils.verifyNotNull(category);
        this.rightOfWayCost = Utils.verifyNotNull(trackConstructionCostPerMile);
        this.buildCost = industryBuildCost;
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

    /** Returns the cost of constructing track across this terrain per mile. */
    public Money getTrackConstructionCostPerMile() {
        return rightOfWayCost;
    }

    /**
     * Returns the cost of constructing an industry on this terrain, or
     * {@code null} if industries cannot be built here.
     */
    public Money getIndustryBuildCost() {
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
