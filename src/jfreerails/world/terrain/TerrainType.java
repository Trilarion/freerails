package jfreerails.world.terrain;

import java.io.ObjectStreamException;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.Money;


/** Defines the methods to access the properties of a type of terrains.
 * Note, this interface has been annotated for use with ConstJava.
 *
 * @author Luke
 */
public interface TerrainType extends FreerailsSerializable {
    String getTerrainTypeName();

    String getTerrainCategory();

    Money getBuildCost();

    int getRightOfWay();

    int getRGB();

    /*=const*/ Production[] getProduction();

    /*=const*/ Consumption[] getConsumption();

    /*=const*/ Conversion[] getConversion();

    String getDisplayName();

    static final TerrainType NULL = (new TerrainType() {
            public /*=const*/ Production[] getProduction() {
                return new Production[0];
            }

            public /*=const*/ Consumption[] getConsumption() {
                return new Consumption[0];
            }

            public /*=const*/ Conversion[] getConversion() {
                return new Conversion[0];
            }

            public String getTerrainTypeName() {
                return null;
            }

            public String getTerrainCategory() {
                return "TerrainType NULL";
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
                return NULL;
            }

            public Money getBuildCost() {
                return null;
            }
        });
}