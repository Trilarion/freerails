/*
 * Created on 04-Jul-2005
 *
 */
package jfreerails.world.terrain;

import java.io.ObjectStreamException;

import jfreerails.util.InstanceControlled;
import jfreerails.world.common.Money;

@InstanceControlled
public class NullTerrainType implements TerrainType {

	public static final TerrainType INSTANCE = new NullTerrainType();

	private NullTerrainType() {

	}

	private static final long serialVersionUID = 3834874680581369912L;

	public Production[] getProduction() {
		return new Production[0];
	}

	public Consumption[] getConsumption() {
		return new Consumption[0];
	}

	public Conversion[] getConversion() {
		return new Conversion[0];
	}

	public String getTerrainTypeName() {
		return "null";
	}

	public Category getCategory() {
		return Category.Country;
	}

	public int getRGB() {
		return 0;
	}

	public int getRightOfWay() {
		return 0;
	}

	public String getDisplayName() {
		return "";
	}

	private Object readResolve() throws ObjectStreamException {
		return INSTANCE;
	}

	public Money getBuildCost() {
		return new Money(0);
	}
}
