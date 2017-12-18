/*
 * Created on 04-Jul-2005
 *
 */
package freerails.world.terrain;

import freerails.util.InstanceControlled;
import freerails.world.common.ImList;
import freerails.world.common.Money;

import java.io.ObjectStreamException;

/**
 *
 * @author jkeller1
 */
@InstanceControlled
public class NullTerrainType implements TerrainType {

    /**
     *
     */
    public static final TerrainType INSTANCE = new NullTerrainType();
    private static final long serialVersionUID = 3834874680581369912L;

    private NullTerrainType() {

    }

    /**
     *
     * @return
     */
    public ImList<Production> getProduction() {
        return new ImList<>();
    }

    /**
     *
     * @return
     */
    public ImList<Consumption> getConsumption() {
        return new ImList<>();
    }

    /**
     *
     * @return
     */
    public ImList<Conversion> getConversion() {
        return new ImList<>();
    }

    /**
     *
     * @return
     */
    public String getTerrainTypeName() {
        return "null";
    }

    /**
     *
     * @return
     */
    public Category getCategory() {
        return Category.Country;
    }

    /**
     *
     * @return
     */
    public int getRGB() {
        return 0;
    }

    /**
     *
     * @return
     */
    public int getRightOfWay() {
        return 0;
    }

    /**
     *
     * @return
     */
    public String getDisplayName() {
        return "";
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }

    /**
     *
     * @return
     */
    public Money getBuildCost() {
        return new Money(0);
    }
}
