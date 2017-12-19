package freerails.world.cargo;

import freerails.world.FreerailsSerializable;

/**
 * Represents a type of cargo.
 *
 */
final public class CargoType implements FreerailsSerializable {
    private static final long serialVersionUID = 3834874680581369912L;
    private final Categories category;
    private final String name;
    private final int unitWeight;

    /**
     *
     * @param weight
     * @param s
     * @param cat
     */
    public CargoType(int weight, String s, Categories cat) {
        unitWeight = weight;
        category = cat;
        name = s;
    }

    /**
     *
     * @return
     */
    public static int getNumberOfCategories() {
        return Categories.values().length;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CargoType))
            return false;
        CargoType other = (CargoType) obj;
        return other.unitWeight == this.unitWeight && other.name.equals(name)
                && other.category.equals(category);
    }

    /**
     *
     * @return
     */
    public Categories getCategory() {
        return category;
    }

    /**
     * Returns the name, replacing any underscores with spaces.
     * @return 
     */
    public String getDisplayName() {
        return this.name.replace('_', ' ');
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
    public int getUnitWeight() {
        return unitWeight;
    }

    @Override
    public int hashCode() {

        int result;
        result = unitWeight;
        result = 29 * result + category.hashCode();
        result = 29 * result + name.hashCode();
        return result;

    }

    @Override
    public String toString() {
        return name;
    }

    /**
     *
     */
    public enum Categories {

        /**
         *
         */
        Mail(0),

        /**
         *
         */
        Passengers(1),

        /**
         *
         */
        Fast_Freight(2),

        /**
         *
         */
        Slow_Freight(3),

        /**
         *
         */
        Bulk_Freight(
                4);
        private final int nr;

        Categories(int nr) {
            this.nr = nr;
        }

        /**
         *
         * @param cat
         * @return
         */
        public static Categories getCategory(String cat) {
            for (Categories cmp : values()) {
                if (cmp.toString().equals(cat)) {
                    return cmp;
                }
            }
            throw new IllegalArgumentException("Category:" + cat + " unknown.");
        }

        /**
         *
         * @return
         */
        public int getNumber() {
            return nr;
        }
    }
}