package jfreerails.world.cargo;

import jfreerails.world.common.FreerailsSerializable;



/** Represents a type of cargo.
 * @author luke
 */
final public class CargoType implements FreerailsSerializable {
    private static final String[] categories = new String[] {
            "Mail", "Passengers", "Fast_Freight", "Slow_Freight", "Bulk_Freight"
        };

    public static int getCategoryNumber(String categoryName) {
        for (int i = 0; i < categories.length; i++) {
            if (categoryName.equals(categories[i])) {
                return i;
            }
        }

        throw new IllegalArgumentException(categoryName);
    }

    public static int getNumberOfCategories() {
        return categories.length;
    }
    private final String category;
    private final String name;
    private final int unitWeight;

    public CargoType(int weight, String s, String cat) {
        getCategoryNumber(cat); //Check for invalid category
        unitWeight = weight;
        category = cat;
        name = s;
    }
    
    public boolean equals(Object obj) {		
		if (!(obj instanceof CargoType)) return false;
		CargoType other = (CargoType)obj;
		return other.unitWeight == this.unitWeight && other.name.equals(name)&& other.category.equals(category);
	}

    public String getCategory() {
        return category;
    }

    public int getCategoryNumber() {
        return getCategoryNumber(this.category);
    }

    /** Returns the name, replacing any underscores with spaces. */
    public String getDisplayName() {
        return this.name.replace('_', ' ');
    }

    public String getName() {
        return name;
    }

    public int getUnitWeight() {
        return unitWeight;
    }
	
	public int hashCode() {
		
	        int result;
	        result = unitWeight;
	        result = 29 * result + category.hashCode();
	        result = 29 * result + name.hashCode();
	        return result;
	   
	}
	
	public String toString() {		
		return name;
	}
}