/*
 * Copyright (C) Dennis M. Sosnoski
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * Copyright (c) 2000-2001 Sosnoski Software Solutions, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package org.railz.util;

import java.lang.reflect.Array;


/**
 * Base class for various types of collections based on type-specific
 * growable arrays. The underlying array used for storage of items doubles
 * in size each time more space is required, up to an optional maximum
 * growth increment specified by the user.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */
public abstract class GrowableBase {
    /** Default initial array size. */
    public static final int DEFAULT_SIZE = 8;

    /** Size of the current array. */
    protected int m_countLimit;

    /** Maximum size increment for growing array. */
    protected int m_maximumGrowth;

    /**
     * Constructor with full specification.
     *
     * @param size number of elements in initial array
     * @param growth maximum size increment for growing array
     * @param type array element type
     */
    public GrowableBase(int size, int growth, Class type) {
        Object array = Array.newInstance(type, size);
        m_countLimit = size;
        m_maximumGrowth = growth;
        setArray(array);
    }

    /**
     * Constructor with partial specification.
     *
     * @param size number of elements initially allowed in array
     * @param type array element type
     */
    public GrowableBase(int size, Class type) {
        this(size, Integer.MAX_VALUE, type);
    }

    /**
     * Copy (clone) constructor.
     *
     * @param base instance being copied
     */
    public GrowableBase(GrowableBase base) {
        this(base.m_countLimit, base.m_maximumGrowth,
            base.getArray().getClass().getComponentType());
    }

    /**
     * Get the backing array. This method is used by the type-agnostic base
     * class code to access the array used for type-specific storage by the
     * child class.
     *
     * @return backing array object
     */
    protected abstract Object getArray();

    /**
     * Set the backing array. This method is used by the type-agnostic base
     * class code to set the array used for type-specific storage by the
     * child class.
     *
     * @param backing array object
     */
    protected abstract void setArray(Object array);

    /**
     * Copy data after array resize. This default implementation just copies
     * the entire contents of the old array to the start of the new array. It
     * should be overridden in cases where data needs to be rearranged in the
     * array after a resize.
     *
     * @param base original array containing data
     * @param grown resized array for data
     */
    protected void resizeCopy(Object base, Object grown) {
        System.arraycopy(base, 0, grown, 0, Array.getLength(base));
    }

    /**
     * Discards values for a range of indices in the array. Checks if the
     * values stored in the array are object references, and if so clears
     * them. If the values are primitives, this method does nothing.
     *
     * @param from index of first value to be discarded
     * @param to index past last value to be discarded
     */
    protected void discardValues(int from, int to) {
        Object values = getArray();

        if (!values.getClass().getComponentType().isPrimitive()) {
            Object[] objects = (Object[])values;

            for (int i = from; i < to; i++) {
                objects[i] = null;
            }
        }
    }

    /**
     * Increase the size of the array to at least a specified size. The array
     * will normally be at least doubled in size, but if a maximum size
     * increment was specified in the constructor and the value is less than
     * the current size of the array, the maximum increment will be used
     * instead. If the requested size requires more than the default growth,
     * the requested size overrides the normal growth and determines the size
     * of the replacement array.
     *
     * @param required new minimum size required
     */
    protected void growArray(int required) {
        Object base = getArray();
        int size = Math.max(required,
                m_countLimit + Math.min(m_countLimit, m_maximumGrowth));
        Class type = base.getClass().getComponentType();
        Object grown = Array.newInstance(type, size);
        resizeCopy(base, grown);
        m_countLimit = size;
        setArray(grown);
    }

    /**
     * Ensure that the array has the capacity for at least the specified
     * number of values.
     *
     * @param min minimum capacity to be guaranteed
     */
    public final void ensureCapacity(int min) {
        if (min > m_countLimit) {
            growArray(min);
        }
    }

    /**
     * Constructs and returns a simple array containing the same data as held
     * in a portion of this growable array.
     *
     * @param type element type for constructed array
     * @param offset start offset in array
     * @param length number of characters to use
     * @return array containing a copy of the data
     */
    protected Object buildArray(Class type, int offset, int length) {
        Object copy = Array.newInstance(type, length);
        System.arraycopy(getArray(), offset, copy, 0, length);

        return copy;
    }
}
