package jfreerails.world.cargo;

final public class CargoType {

	private final int unitWeight;

	private final int unitVolume;

	private final String name;

	public int getUnitWeight() {
		return unitWeight;
	}

	public int getUnitVolume() {
		return unitVolume;
	}

	public String getName() {
		return name;
	}

	public CargoType(int weight, int volume, String s) {
		unitWeight = weight;
		unitVolume = volume;
		name = s;
	}

}