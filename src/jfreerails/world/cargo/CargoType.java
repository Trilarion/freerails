package jfreerails.world.cargo;

import jfreerails.world.common.FreerailsSerializable;


/** Represents a type of cargo.
 * @author luke
 */
final public class CargoType implements FreerailsSerializable {
    private final int unitWeight;
    private final String category;
    private final String name;
    private static final String[] categories = new String[] {
            "Mail", "Passengers", "Fast_Freight", "Slow_Freight", "Bulk_Freight"
        };

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

    public CargoType(int weight, String s, String cat) {
        getCategoryNumber(cat); //Check for invalid category
        unitWeight = weight;
        category = cat;
        name = s;
    }

    public String getCategory() {
        return category;
    }

    public int getCategoryNumber() {
        return getCategoryNumber(this.category);
    }

    public static int getNumberOfCategories() {
        return categories.length;
    }

    public static int getCategoryNumber(String categoryName) {
        for (int i = 0; i < categories.length; i++) {
            if (categoryName.equals(categories[i])) {
                return i;
            }
        }

        throw new IllegalArgumentException(categoryName);
    }
}