/*
 * fastUtil 1.3: Fast & compact specialized hash-based utility classes for Java
 *
 * Copyright (C) 2002 Sebastiano Vigna
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package it.unimi.dsi.fastUtil;

import java.util.Iterator;
import java.util.Map;


/** An abstract class providing basic methods for maps implementing a type-specific interface.
 */
public abstract class Int2IntAbstractMap implements Int2IntMap {
    /** Puts all pairs in the given map.
     * If the map implements the interface of this map,
     * it uses the faster iterators.
     *
     * @param m a map.
     */
    public void putAll(Map m) {
        int n = m.size();
        Iterator i = m.entrySet().iterator();

        if (m instanceof Int2IntMap) {
            Int2IntMap.Entry e;

            while (n-- != 0) {
                e = (Int2IntMap.Entry)i.next();
                put(e.getIntKey(), e.getIntValue());
            }
        } else {
            Map.Entry e;

            while (n-- != 0) {
                e = (Map.Entry)i.next();
                put(e.getKey(), e.getValue());
            }
        }
    }

    /** Returns a hash code for this map.
     *
     * The hash code of a map is computed by summing the hash codes of
     * keys and values of the map. Note that we correctly handle the case
     * of <code>null</code> and that of a map being its own key or value
     * (in both cases, we just sum 0).
     *
     * @return a hash code for this map.
     */
    public int hashCode() {
        int h = 0;
        int n = size();
        Iterator i = keySet().iterator();
        Int2IntMap.Entry e;

        while (n-- != 0) {
            e = (Int2IntMap.Entry)i.next();

            h += ((int)(e.getIntKey()));

            h += ((int)(e.getIntValue()));
        }

        return h;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Map)) {
            return false;
        }

        Map m = (Map)o;

        if (m.size() != size()) {
            return false;
        }

        return keySet().containsAll(m.keySet());
    }

    public String toString() {
        final StringBuffer s = new StringBuffer();
        final Iterator i = entrySet().iterator();
        int n = size();
        Int2IntMap.Entry e;
        boolean first = true;

        s.append("[");

        while (n-- != 0) {
            if (first) {
                first = false;
            } else {
                s.append(", ");
            }

            e = (Int2IntMap.Entry)i.next();

            s.append(String.valueOf(e.getIntKey()));
            s.append("=>");

            s.append(String.valueOf(e.getIntValue()));
        }

        s.append("]");

        return s.toString();
    }
}

// Local Variables:
// mode: java
// End:
