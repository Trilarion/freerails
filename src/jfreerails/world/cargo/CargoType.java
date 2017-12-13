package jfreerails.world.cargo;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.train.TransportCategory;


/** This class represents a type of cargo */
final public class CargoType implements FreerailsSerializable {
    private final int unitWeight;
    private final String name;
    private final TransportCategory category;

    public int getUnitWeight() {
        return unitWeight;
    }

    public String getName() {
        return name;
    }

    /** Returns the name, replacing any underscores with spaces. */
    public String getDisplayName() {
        return this.name.replace('_', ' ');
    }

    public CargoType(int weight, String name, TransportCategory category) {
        this.unitWeight = weight;
        this.category = category;
        this.name = name;
    }

    public String toString() {
       return "CargoType: weight=" + unitWeight + ", category=" + category +
       ", name=" + name;
    }

    public TransportCategory getCategory() {
        return category;
    }
}
