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
