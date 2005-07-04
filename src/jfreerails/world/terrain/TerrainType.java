package jfreerails.world.terrain;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.Money;

/**
 * Defines the methods to access the properties of a type of terrains.
 * 
 * 
 * @author Luke
 */
public interface TerrainType extends FreerailsSerializable {

	enum Category {
		Urban, River, Ocean, Hill, Country, Special, Industry, Resource
	};

	String getTerrainTypeName();

	Category getCategory();

	Money getBuildCost();

	int getRightOfWay();

	int getRGB();

	Production[] getProduction();

	Consumption[] getConsumption();

	Conversion[] getConversion();

	String getDisplayName();
}