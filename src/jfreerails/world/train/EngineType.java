package jfreerails.world.train;

import jfreerails.world.misc.Money;

final public class EngineType {

	private final String engineTypeName;

	private final int powerAtDrawbar;

	private final Money price;

	public String getEngineTypeName() {
		return engineTypeName;
	}

	public int getPowerAtDrawbar() {
		return powerAtDrawbar;
	}

	public Money getPrice() {
		return price;
	}

	public void getRatedTrainSpeedAtGrade(int speed, int grade) {
	}

	public EngineType(String name, int power, Money m) {
		engineTypeName = name;
		powerAtDrawbar = power;
		price = m;

	}

}