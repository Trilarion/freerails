package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.Money;

/**
 * This class represents an engine type, for example 'Grass Hopper'. It
 * encapsulates the properties that are common to all engines of the same type.
 * 
 * @author Luke
 * 
 */
final public class EngineType implements FreerailsSerializable {
	private static final long serialVersionUID = 3617014130905592630L;

	private final String engineTypeName;

	private final Money maintenance;

	private final int maxSpeed; // speed in mph

	private final int powerAtDrawbar;

	private final Money price;

	public EngineType(String name, int power, Money m, int speed) {
		engineTypeName = name;
		powerAtDrawbar = power;
		price = m;
		maxSpeed = speed;
		maintenance = new Money(0);
	}

	public EngineType(String name, int power, Money m, int speed, Money maint) {
		engineTypeName = name;
		powerAtDrawbar = power;
		price = m;
		maxSpeed = speed;
		maintenance = maint;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof EngineType))
			return false;
		EngineType other = (EngineType) obj;
		return engineTypeName.equals(other.engineTypeName)

		&& powerAtDrawbar == other.powerAtDrawbar && price.equals(other.price)
				&& maintenance.equals(other.maintenance)
				&& maxSpeed == other.maxSpeed;

	}

	public String getEngineTypeName() {
		return engineTypeName;
	}

	public Money getMaintenance() {
		return maintenance;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public int getPowerAtDrawbar() {
		return powerAtDrawbar;
	}

	public Money getPrice() {
		return price;
	}

	public int hashCode() {

		int result;
		result = powerAtDrawbar;
		result = 29 * result + engineTypeName.hashCode();
		result = 29 * result + price.hashCode();
		result = 29 * result + maintenance.hashCode();
		result = 29 * result + maxSpeed;
		return result;

	}

	public String toString() {
		return engineTypeName;
	}
}