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
package freerails.util;

import java.io.Serializable;

/**
 * Base class for type-specific growable array classes with any type of values
 * (including primitive types). This class builds on the basic structure
 * provided by {@code GrowableBase}, specializing it for usage as a
 * growable array. See the base class description for details of the
 * implementation.
 *
 * Growable arrays based on this class are unsynchronized in order to provide
 * the best possible performance for typical usage scenarios, so explicit
 * synchronization must be implemented by the subclass or the application in
 * cases where they are to be modified in a multithreaded environment.
 *
 * Subclasses need to implement the abstract methods defined by the base class
 * for working with the data array, as well as the actual data access methods
 * (at least the basic {@code add()}, {@code get()},
 * {@code set()}, and {@code toArray()} methods).
 *
 * @author Dennis M. Sosnoski
 */
public abstract class ArrayBase extends GrowableBase implements Serializable {
    private static final long serialVersionUID = -1899068791184507467L;
    /**
     * The number of values currently present in the array.
     */
    protected int countPresent;

    /**
     * Constructor with full specification.
     *
     * @param size   number of elements initially allowed in array
     * @param growth maximum size increment for growing array
     * @param type   array element type
     */
    public ArrayBase(int size, int growth, Class type) {
        super(size, growth, type);
    }

    /**
     * Constructor with partial specification.
     *
     * @param size number of elements initially allowed in array
     * @param type array element type
     */
    public ArrayBase(int size, Class type) {
        this(size, Integer.MAX_VALUE, type);
    }

    /**
     * Copy (clone) constructor.
     *
     * @param base instance being copied
     */
    public ArrayBase(ArrayBase base) {
        super(base);
        System.arraycopy(base.getArray(), 0, getArray(), 0, base.countPresent);
        countPresent = base.countPresent;
    }

    /**
     * Gets the array offset for appending a value to those in the array. If the
     * underlying array is full, it is grown by the appropriate size increment
     * so that the index value returned is always valid for the array in use by
     * the time of the return.
     *
     * @return index position for added element
     */
    protected final int getAddIndex() {
        int index = countPresent++;

        if (countPresent > countLimit) {
            growArray(countPresent);
        }

        return index;
    }

    /**
     * Makes room to insert a value at a specified index in the array.
     *
     * @param index index position at which to insert element
     */
    protected void makeInsertSpace(int index) {
        if (index >= 0 && index <= countPresent) {
            if (++countPresent > countLimit) {
                growArray(countPresent);
            }

            if (index < countPresent - 1) {
                Object array = getArray();
                System.arraycopy(array, index, array, index + 1, countPresent
                        - index - 1);
            }
        } else {
            throw new ArrayIndexOutOfBoundsException("Invalid index value");
        }
    }

    /**
     * Get the number of values currently present in the array.
     *
     * @return count of values present
     */
    public final int size() {
        return countPresent;
    }

    /**
     * Constructs and returns a simple array containing the same data as held in
     * a portion of this growable array. This override of the base class method
     * checks that the portion specified actually has data present before
     * constructing the returned array.
     *
     * @param type   element type for constructed array
     * @param offset start offset in array
     * @param length number of characters to use
     * @return array containing a copy of the data
     */
    @Override
    protected Object buildArray(Class type, int offset, int length) {
        if (offset + length <= countPresent) {
            return super.buildArray(type, offset, length);
        }
        throw new ArrayIndexOutOfBoundsException();
    }
}