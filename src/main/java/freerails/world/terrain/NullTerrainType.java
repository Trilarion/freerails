/*
 * Created on 04-Jul-2005
 *
 */
package freerails.world.terrain;

import freerails.util.InstanceControlled;
import freerails.world.common.ImList;
import freerails.world.common.Money;

import java.io.ObjectStreamException;

@InstanceControlled
public class NullTerrainType implements TerrainType {

    public static final TerrainType INSTANCE = new NullTerrainType();

    private NullTerrainType() {

    }

    private static final long serialVersionUID = 3834874680581369912L;

    public ImList<Production> getProduction() {
        return new ImList<>();
    }

    public ImList<Consumption> getConsumption() {
        return new ImList<>();
    }

    public ImList<Conversion> getConversion() {
        return new ImList<>();
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
