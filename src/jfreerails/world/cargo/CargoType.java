package jfreerails.world.cargo;

import java.util.HashSet;
import jfreerails.world.common.FreerailsSerializable;


/** This class represents a type of cargo */
final public class CargoType implements FreerailsSerializable {
    private final int unitWeight;
    private final String category;
    private final String name;
    private static HashSet categories = new HashSet();

    static {
        categories.add("Mail");
        categories.add("Passengers");
        categories.add("Fast_Freight");
        categories.add("Slow_Freight");
        categories.add("Bulk_Freight");
    }

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

    public CargoType(int weight, String name, String category) {
        if (!categories.contains(category)) {
            throw new IllegalArgumentException(category);
        }

        this.unitWeight = weight;
        this.category = category;
        this.name = name;
    }

    public String getCategory() {
        return category;
    }
}