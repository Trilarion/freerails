package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;

/**
 * This class represents a wagon type, for example 'goods wagon'.  It encapsulates 
 * the properties of a wagon that are common to all wagons of the same type.
 * 
 * @author Luke
 *
 */

public class WagonType implements FreerailsSerializable {

	public static final int NUMBER_OF_CATEGORIES = 6;

	public static final int MAIL = 0;

	public static final int PASSENGER = 1;

	public static final int FAST_FREIGHT = 2;

	public static final int SLOW_FREIGHT = 3;

	public static final int BULK_FREIGHT = 4;

	public static final int ENGINE = 5;

	private final String typeName;

	private final int typeCategory;

	public WagonType(String name, int category) {
		typeName = name;
		typeCategory = category;
	}

	public String getName() {
		return typeName;
	}

	public int getCategory() {
		return typeCategory;
	}

}
