package freerails.model.cargo;

import freerails.model.Identifiable;
import freerails.model.world.UnmodifiableWorld;

public class CargoType extends Identifiable {

    private final String name;
    private final CargoCategory category;
    private final int unitWeight;

    public CargoType(int id, String name, CargoCategory category, int unitWeight) {
        super(id);
        this.name = name;
        this.category = category;
        this.unitWeight = unitWeight;
    }

    public String getName() {
        return name;
    }

    public CargoCategory getCategory() {
        return category;
    }

    public int getUnitWeight() {
        return unitWeight;
    }

    // TODO delete this
    public static CargoType fromCargoType(CargoType ct, UnmodifiableWorld world) {
        for (CargoType ct2: world.getCargoTypes()) {
            if (ct2.getName().equals(ct.getName())) {
                return ct2;
            }
        }
        throw new RuntimeException();
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name, category);
    }
}
