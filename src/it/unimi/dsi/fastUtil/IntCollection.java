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

import java.util.Collection;


/** A collection of fixed type items; provides some additional methods
 * that use polymorphism to reduce type juggling.
 *
 * <p>Note that iterators provided by classes implementing this interface
 * <em>must</em> be type specific.
 *
 * @see Collection
 */
public interface IntCollection extends Collection {
    /**
          * @see Collection#contains(Object)
     */
    boolean contains(int key);

    /** Returns a primitive type array containing the items of this collection.
     * @return a primitive type array containing the items of this collection.
     * @see Collection#toArray()
     */
    int[] toIntArray();

    /** Builds a primitive type array containing the items of this collection.
     * @param a if this array is big enough, it will be used to store the collection.
     * @return a primitive type array containing the items of this collection.
     * @see Collection#toArray(Object[])
     */
    int[] toIntArray(int[] a);

    /** Builds a primitive type array containing the items of this collection.
     * @param a if this array is big enough, it will be used to store the collection.
     * @return a primitive type array containing the items of this collection.
     * @see Collection#toArray(Object[])
     */
    int[] toArray(int[] a);

    /**
          * @see Collection#add(Object)
     */
    boolean add(int key);

    /**
          * @see Collection#remove(Object)
     */
    boolean remove(int key);
}

// Local Variables:
// mode: java
// End:
