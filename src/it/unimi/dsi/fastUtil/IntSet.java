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


/** A collection of fixed type values that contains no duplicate elements;
 * provides some additional methods that use polymorphism to reduce type juggling.
 *
 * @see Set
 */
public interface IntSet extends Set {
    /**
          * @see Set#contains(Object)
     */
    boolean contains(int key);

    /** Returns a primitive type array containing the elements of this set.
     * @return a primitive type array containing the elements of this set.
     * @see Set#toArray()
     */
    int[] toIntArray();

    /** Returns a primitive type array containing the elements of this set.
     * @param a if this array is big enough, it will be used to store the set.
     * @return a primitive type array containing the elements of this set.
     * @see Set#toArray(Object[])
     */
    int[] toArray(int[] a);

    /**
          * @see Set#add(Object)
     */
    boolean add(int key);

    /**
          * @see Set#remove(Object)
     */
    boolean remove(int key);
}

// Local Variables:
// mode: java
// End:
