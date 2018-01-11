/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.world.train;

import java.io.Serializable;

/**
 * Represents a wagon type, for example 'goods wagon'. It
 * encapsulates the properties of a wagon that are common to all wagons of the
 * same type.
 */
public class WagonType implements Serializable {

    private static final long serialVersionUID = 3906368233710826292L;

    // TODO instead of type category use enum
    public static final int BULK_FREIGHT = 4;
    public static final int ENGINE = 5;
    public static final int FAST_FREIGHT = 2;
    public static final int MAIL = 0;
    public static final int PASSENGER = 1;
    public static final int SLOW_FREIGHT = 3;
    public static final int UNITS_OF_CARGO_PER_WAGON = 40;

    private final int typeCategory;
    private final String typeName;

    /**
     * @param name
     * @param category
     */
    public WagonType(String name, int category) {
        typeName = name;
        typeCategory = category;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WagonType))
            return false;
        WagonType other = (WagonType) obj;
        return other.typeCategory == typeCategory
                && other.typeName.equals(typeName);
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