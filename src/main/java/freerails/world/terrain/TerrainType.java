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

    /**
     *
     * @return
     */
    String getTerrainTypeName();

    /**
     *
     * @return
     */
    Category getCategory();

    /**
     *
     * @return
     */
    Money getBuildCost();

    /**
     *
     * @return
     */
    int getRightOfWay();

    /**
     *
     * @return
     */
    int getRGB();

    /**
     *
     * @return
     */
    ImList<Production> getProduction();

    /**
     *
     * @return
     */
    ImList<Consumption> getConsumption();

    /**
     *
     * @return
     */
    ImList<Conversion> getConversion();

    /**
     *
     * @return
     */
    String getDisplayName();

    /**
     *
     */
    enum Category implements FreerailsSerializable {

        /**
         *
         */
        Urban,

        /**
         *
         */
        River,

        /**
         *
         */
        Ocean,

        /**
         *
         */
        Hill,

        /**
         *
         */
        Country,

        /**
         *
         */
        Special,

        /**
         *
         */
        Industry,

        /**
         *
         */
        Resource
    }
}