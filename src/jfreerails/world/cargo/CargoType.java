package jfreerails.world.cargo;

/** This class represents a type of cargo */

final public class CargoType {
	
	public static final String MAIL = "Mail";
	public static final String PASSENGERS = "Passengers";
	public static final String FAST_FREIGHT = "Fast_Freight";
	public static final String SLOW_FREIGHT = "Slow_Freight";
	public static final String BULK_FREIGHT = "Bulk_Freight";
	
	private final int unitWeight;
	
	private final String category;

	private final String name;

	public int getUnitWeight() {
		return unitWeight;
	}

	public String getName() {
		return name;
	}

	public CargoType(int weight, String name, String category) {
		this.unitWeight = weight;
		this.category = category;
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

}