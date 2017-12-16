package freerails.world.train;

import freerails.world.common.FreerailsSerializable;

/**
 * This class represents a wagon type, for example 'goods wagon'. It
 * encapsulates the properties of a wagon that are common to all wagons of the
 * same type.
 * 
 * @author Luke
 * 
 */
public class WagonType implements FreerailsSerializable {
    private static final long serialVersionUID = 3906368233710826292L;

    public static final int BULK_FREIGHT = 4;

    public static final int ENGINE = 5;

    public static final int FAST_FREIGHT = 2;

    public static final int MAIL = 0;

    public static final int NUMBER_OF_CATEGORIES = 6;

    public static final int PASSENGER = 1;

    public static final int SLOW_FREIGHT = 3;

    public static final int UNITS_OF_CARGO_PER_WAGON = 40;

    private final int typeCategory;

    private final String typeName;

    public WagonType(String name, int category) {
        typeName = name;
        typeCategory = category;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WagonType))
            return false;
        WagonType other = (WagonType) obj;
        return other.typeCategory == this.typeCategory
                && other.typeName.equals(typeName);
    }

    public int getCategory() {
        return typeCategory;
    }

    public String getName() {
        return typeName;
    }

    @Override
    public int hashCode() {

        int result;
        result = typeCategory;
        result = 29 * result + typeName.hashCode();

        return result;

    }

    @Override
    public String toString() {
        return typeName;
    }
}