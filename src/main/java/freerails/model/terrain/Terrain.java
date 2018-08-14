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

package freerails.model.terrain;

import freerails.model.Identifiable;
import freerails.model.cargo.CargoConversion;
import freerails.model.cargo.CargoProductionOrConsumption;
import freerails.model.finances.Money;
import freerails.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 *
 */
public class Terrain extends Identifiable {

    private final String name;
    private final TerrainCategory category;
    private final Money rightOfWayCost;
    private final Money buildCost;
    private final List<CargoProductionOrConsumption> productions;
    private final List<CargoConversion> conversions;
    private final List<CargoProductionOrConsumption> consumptions;

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

    public String getName() {
        return name;
    }

    public TerrainCategory getCategory() {
        return category;
    }

    public Money getRightOfWayCost() {
        return rightOfWayCost;
    }

    public Money getBuildCost() {
        return buildCost;
    }

    public List<CargoProductionOrConsumption> getProductions() {
        return productions;
    }

    public List<CargoConversion> getConversions() {
        return conversions;
    }

    public List<CargoProductionOrConsumption> getConsumptions() {
        return consumptions;
    }
}
