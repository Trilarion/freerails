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
 * Growable {@code int} array with type specific access methods. This
 * implementation is unsynchronized in order to provide the best possible
 * performance for typical usage scenarios, so explicit synchronization must be
 * implemented by a wrapper class or directly by the application in cases where
 * instances are modified in a multi-threaded environment. See the base classes
 * for other details of the implementation.
 */
@SuppressWarnings("unused")
public class IntArray extends ArrayBase implements Serializable {

    private static final long serialVersionUID = 3258408426391418681L;
    /**
     * The underlying array used for storing the data.
     */
    protected int[] baseArray;

    /**
     * Constructor with full specification.
     *
     * @param size   number of {@code int} values initially allowed in array
     * @param growth maximum size increment for growing array
     */
    public IntArray(int size, int growth) {
        super(size, growth, int.class);
    }

    /**
     * Constructor with only initial size specified.
     *
     * @param size number of {@code int} values initially allowed in array
     */
    public IntArray(int size) {
        super(size, int.class);
    }

    /**
     * Default constructor.
     */
    public IntArray() {
        this(DEFAULT_SIZE);
    }

    /**
     * Copy (clone) constructor.
     *
     * @param base instance being copied
     */
    public IntArray(IntArray base) {
        super(base);
    }

    /**
     * Get the backing array. This method is used by the type-agnostic base
     * class code to access the array used for type-specific storage.
     *
     * @return backing array object
     */
    @Override
    protected final Object getArray() {
        return baseArray;
    }

    /**
     * Set the backing array. This method is used by the type-agnostic base
     * class code to set the array used for type-specific storage.
     *
     * @param array
     */
    @Override
    protected final void setArray(Object array) {
        baseArray = (int[]) array;
    }

    /**
     * Add a value to the array, appending it after the current values.
     *
     * @param value value to be added
     * @return index number of added element
     */
    public final int add(int value) {
        int index = getAddIndex();
        baseArray[index] = value;

        return index;
    }

    /**
     * Add a value at a specified index in the array.
     *
     * @param index index position at which to insert element
     * @param value value to be inserted into array
     */
    public void add(int index, int value) {
        makeInsertSpace(index);
        baseArray[index] = value;
    }

    /**
     * Retrieve the value present at an index position in the array.
     *
     * @param index index position for value to be retrieved
     * @return value from position in the array
     */
    public final int get(int index) {
        if (index < countPresent) {
            return baseArray[index];
        }
        throw new ArrayIndexOutOfBoundsException("Invalid index value");
    }

    /**
     * Set the value at an index position in the array.
     *
     * @param index index position to be set
     * @param value value to be set
     */
    public final void set(int index, int value) {
        if (index < countPresent) {
            baseArray[index] = value;
        } else {
            throw new ArrayIndexOutOfBoundsException("Invalid index value");
        }
    }

    /**
     * Constructs and returns a simple array containing the same data as held in
     * this growable array.
     *
     * @return array containing a copy of the data
     */
    public int[] toArray() {
        return (int[]) buildArray(int.class, 0, countPresent);
    }

    /**
     * Duplicates the object with the generic call.
     *
     * @return a copy of the object
     */
    @Override
    public Object clone() {
        return new IntArray(this);
    }
}