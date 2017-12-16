package freerails.world.terrain;

import freerails.world.common.FreerailsSerializable;
import freerails.world.common.ImList;
import freerails.world.common.Money;

/**
 * Defines the methods to access the properties of a type of terrains.
 *
 * @author Luke
 */
public interface TerrainType extends FreerailsSerializable {

    enum Category implements FreerailsSerializable {
        Urban, River, Ocean, Hill, Country, Special, Industry, Resource
    }

    String getTerrainTypeName();

    Category getCategory();

    Money getBuildCost();

    int getRightOfWay();

    int getRGB();

    ImList<Production> getProduction();

    ImList<Consumption> getConsumption();

    ImList<Conversion> getConversion();

    String getDisplayName();
}