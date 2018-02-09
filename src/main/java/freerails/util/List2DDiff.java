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
 *
 */
package freerails.util;

import java.util.SortedMap;

/**
 * @param <T>
 */
public class List2DDiff<T> extends ListXDDiffs<T> implements List2D<T> {

    private static final long serialVersionUID = 4323585276281406244L;
    private final List2D<T> underlyingList;

    /**
     * @param diffs
     * @param list
     * @param listID
     */
    public List2DDiff(SortedMap<ListKey, Object> diffs, List2D<T> list, Enum listID) {
        super(diffs, listID);
        underlyingList = list;
    }

    /**
     * @return
     */
    public int sizeD1() {
        return super.size();
    }

    /**
     * @param d1
     * @return
     */
    public int sizeD2(int d1) {
        return super.size(d1);
    }

    /**
     * @param d1
     * @param d2
     * @return
     */
    public T get(int d1, int d2) {
        return super.get(d1, d2);
    }

    /**
     * @param d1
     * @return
     */
    public T removeLastD2(int d1) {
        return super.removeLast(d1);
    }

    /**
     * @return
     */
    public int removeLastD1() {
        return super.removeLastList();
    }

    /**
     * @return
     */
    public int addD1() {
        return super.addDimension();
    }

    /**
     * @param d1
     * @param element
     * @return
     */
    public int addD2(int d1, T element) {
        return super.addElement(element, d1);
    }

    /**
     * @param d1
     * @param d2
     * @param element
     */
    public void set(int d1, int d2, T element) {
        super.set(element, d1, d2);
    }

    @Override
    T uGet(int... i) {
        if (i.length != 2) throw new IllegalArgumentException(String.valueOf(i.length));
        return underlyingList.get(i[0], i[1]);
    }

    @Override
    int getUnderlyingSize(int... dim) {
        if (dim.length == 0) return underlyingList.sizeD1();
        if (dim.length == 1) {
            if (underlyingList.sizeD1() <= dim[0]) return -1;

            return underlyingList.sizeD2(dim[0]);
        }
        throw new IllegalArgumentException(String.valueOf(dim.length));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof List2D && List2DImpl.equals(this, (List2D) obj);
    }

    @Override
    public int hashCode() {
        return sizeD1();
    }

}
