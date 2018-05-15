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
