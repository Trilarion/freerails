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
	
	enum Category {Urban , River , Ocean , Hill , Country , Special , Industry , Resource};
	
    String getTerrainTypeName();

    Category getCategory();

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
                return NULL;
            }

            public Money getBuildCost() {
                return null;
            }
        });
}