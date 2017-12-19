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

/*
 * Created on 24-Jul-2005
 *
 */
package freerails.util;

import java.io.Serializable;
import java.util.SortedMap;

/**
 *
 * @param <T>
 */
public abstract class ListXDDiffs<T> implements Serializable {

    private static final long serialVersionUID = 127789045793369316L;
    private final SortedMap<ListKey, Object> diffs;
    private final Enum listID;

    /**
     *
     * @param diffs
     * @param listID
     */
    public ListXDDiffs(SortedMap<ListKey, Object> diffs, Enum listID) {
        this.diffs = diffs;
        this.listID = listID;
    }

    static int[] add2Array(int[] dim, int last) {
        int[] array = new int[dim.length + 1];
        System.arraycopy(dim, 0, array, 0, dim.length);
        array[array.length - 1] = last;
        return array;
    }

    static int[] removeFromArray(int[] dim) {
        int[] array = new int[dim.length - 1];
        System.arraycopy(dim, 0, array, 0, dim.length - 1);
        return array;
    }

    /**
     *
     * @param dim
     * @return
     */
    public int addDimension(int... dim) {
        int i = size(dim);
        ListKey sizeKeyA = new ListKey(ListKey.Type.EndPoint, listID, dim);

        int[] subArray = add2Array(dim, i);
        ListKey sizeKeyB = new ListKey(ListKey.Type.EndPoint, listID, subArray);

        diffs.put(sizeKeyA, i + 1);
        diffs.put(sizeKeyB, 0);
        return i;
    }

    /**
     *
     * @param element
     * @param dim
     * @return
     */
    public int addElement(T element, int... dim) {

        int sizeBefore = size(dim);
        int[] index = add2Array(dim, sizeBefore);

        setElementDiff:
        {
            if (getUnderlyingSize(dim) > sizeBefore) {
                T uElement = uGet(index);
                if (Utils.equal(uElement, element)) {
                    // We are readding an element that was removed, in which
                    // case we don't store a diff.
                    break setElementDiff;
                }
            }
            ListKey elementKey = new ListKey(ListKey.Type.Element, listID,
                    index);
            diffs.put(elementKey, element);
        }

        setSize(sizeBefore + 1, dim);
        return sizeBefore;
    }

    /**
     *
     * @param i
     * @return
     */
    @SuppressWarnings("unchecked")
    public T get(int... i) {
        checkBounds(i);
        ListKey elementKey = new ListKey(ListKey.Type.Element, listID, i);
        if (diffs.containsKey(elementKey)) {
            return (T) diffs.get(elementKey);
        }
        return uGet(i);
    }

    abstract Object getUnderlyingList();

    /**
     * Returns the size of the underlying list at the specified dimension or -1
     * if the underlying list does not have the specified dimension.
     */
    abstract int getUnderlyingSize(int... dim);

    /**
     *
     * @param dim
     * @return
     */
    @SuppressWarnings("unchecked")
    public T removeLast(int... dim) {

        T toRemove;
        int last = size(dim) - 1;
        int[] array = add2Array(dim, last);
        ListKey elementKey = new ListKey(ListKey.Type.Element, listID, array);
        if (diffs.containsKey(elementKey)) {
            toRemove = (T) diffs.remove(elementKey);
        } else {

            toRemove = uGet(array);
        }
        setSize(last, dim);

        return toRemove;
    }

    int removeLastList(int... dim) {
        int last = size(dim) - 1;
        // Check that the list we are removing is empty.
        int[] array = add2Array(dim, last);
        if (0 != size(array))
            throw new IllegalStateException();

        ListKey sizeKeyB = new ListKey(ListKey.Type.EndPoint, listID, array);
        diffs.remove(sizeKeyB);
        setSize(last, dim);
        return last;
    }

    /**
     *
     * @param element
     * @param i
     */
    public void set(T element, int... i) {
        // Check bounds..
        checkBounds(i);
        int last = i[i.length - 1];
        int[] dim = checkBounds(i);

        ListKey elementKey = new ListKey(ListKey.Type.Element, listID, i);
        boolean b = getUnderlyingSize(dim) > last;
        if (b && Utils.equal(uGet(i), element)) {
            if (diffs.containsKey(elementKey))
                diffs.remove(elementKey);
        } else {
            diffs.put(elementKey, element);
        }
    }

    private int[] checkBounds(int... i) {
        int[] dim = removeFromArray(i);
        int last = i[i.length - 1];
        if (last >= size(dim))
            throw new IndexOutOfBoundsException(String.valueOf(last));
        return dim;
    }

    private void setSize(int size, int... dim) {
        ListKey sizeKey = new ListKey(ListKey.Type.EndPoint, listID, dim);

        if (getUnderlyingSize(dim) == size) {
            diffs.remove(sizeKey);
        } else {
            diffs.put(sizeKey, size);
        }
    }

    /**
     *
     * @param i
     * @return
     */
    public int size(int... i) {
        ListKey sizeKey = new ListKey(ListKey.Type.EndPoint, listID, i);
        if (diffs.containsKey(sizeKey)) {
            return (Integer) diffs.get(sizeKey);
        }
        return getUnderlyingSize(i);
    }

    abstract T uGet(int... i);

    // abstract int uSize(int... i);
}
