/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.util;

import java.awt.Rectangle;
import java.io.ObjectInputFilter;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * Deserialization filter shared by network connections and save-game loading. It applies size and
 * complexity limits and allows only FreeRails classes plus standard value and container classes.
 *
 * @param maxDepth maximum object graph depth
 * @param maxReferences maximum number of object references
 * @param maxStreamBytes maximum number of bytes read from the stream
 * @param maxArrayLength maximum length of any single array
 */
public record SerializationFilter(long maxDepth, long maxReferences, long maxStreamBytes, long maxArrayLength)
        implements ObjectInputFilter {

    @Override
    public Status checkInput(FilterInfo info) {
        if (info.depth() > maxDepth || info.references() > maxReferences
                || info.streamBytes() > maxStreamBytes
                || info.arrayLength() > maxArrayLength) {
            return Status.REJECTED;
        }

        Class<?> type = info.serialClass();
        if (type == null) {
            return Status.UNDECIDED;
        }
        return isAllowedClass(type) ? Status.ALLOWED : Status.REJECTED;
    }

    /**
     * Determines whether a serialized class belongs to the supported type set.
     *
     * @param type class being checked
     * @return {@code true} if the class is permitted by the filter
     */
    public static boolean isAllowedClass(Class<?> type) {
        if (type.isArray()) {
            Class<?> component = type.getComponentType();
            return component == Object.class || component == Map.Entry.class || component.isPrimitive()
                    || isAllowedClass(component);
        }
        if (Proxy.isProxyClass(type)) {
            return false;
        }
        String name = type.getName();
        if (name.startsWith("freerails.")) {
            return true;
        }
        if (type == String.class || type == Boolean.class || type == Byte.class
                || type == Short.class || type == Integer.class || type == Long.class
                || type == Float.class || type == Double.class || type == Character.class
                || type == Enum.class || type == Number.class || type == Rectangle.class
                || type == BigInteger.class
                || type == BigDecimal.class) {
            return true;
        }
        if (type == java.util.ArrayList.class || type == java.util.LinkedList.class
                || type == java.util.HashMap.class || type == java.util.LinkedHashMap.class
                || type == java.util.TreeMap.class || type == java.util.HashSet.class
                || type == java.util.LinkedHashSet.class || type == java.util.TreeSet.class
                || type == java.util.Vector.class || type == java.util.Hashtable.class
                || name.equals("java.util.Arrays$ArrayList")) {
            return true;
        }
        return name.startsWith("java.util.Collections$")
                || name.startsWith("java.util.ImmutableCollections$")
                || name.equals("java.util.CollSer")
                || name.equals("java.util.HashMap$Node")
                || name.equals("java.util.LinkedHashMap$Entry")
                || name.equals("java.util.TreeMap$Entry");
    }
}
