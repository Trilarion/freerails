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


/** An object that iterates through a collection whose items have a
 * fixed primitive type; provides an additional method to reduce type juggling.
 *
 * @see Iterator
 */
public interface IntIterator extends Iterator {
    /**
          * Returns the next element as a primitive type.
          *
          * @return the next element from the collection.
     * @see Iterator#next()
          */
    int nextInt();
}

// Local Variables:
// mode: java
// End:
