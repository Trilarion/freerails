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

import java.util.Set;


/**  An abstract class providing basic methods for sets implementing a type-specific interface.
 */
public abstract class IntAbstractSet extends IntAbstractCollection
    implements Cloneable, IntSet {
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Set)) {
            return false;
        }

        Set s = (Set)o;

        if (s.size() != size()) {
            return false;
        }

        return containsAll(s);
    }
}

// Local Variables:
// mode: java
// End:
