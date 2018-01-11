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

import java.util.ArrayList;
import java.util.List;

/**
 * @param <T>
 */
public class List1DImpl<T> implements List1D<T> {

    private static final long serialVersionUID = 8285123045287237133L;
    private final List<T> elementData;

    /**
     *
     */
    public List1DImpl() {
        elementData = new ArrayList<>();
    }

    /**
     * @param initialSize
     */
    public List1DImpl(int initialSize) {
        elementData = new ArrayList<>();
        for (int i = 0; i < initialSize; i++) {
            elementData.add(null);
        }
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(List1D a, List1D b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!Utils.equal(a.get(i), b.get(i))) return false;
        }
        return true;
    }

    /**
     * @return
     */
    public int size() {
        return elementData.size();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof List1D && equals(this, (List1D) obj);
    }

    @Override
    public int hashCode() {
        return size();
    }

    /**
     * @param i
     * @return
     */
    public T get(int i) {
        return elementData.get(i);
    }

    /**
     * @return
     */
    public T removeLast() {
        int last = elementData.size() - 1;
        return elementData.remove(last);
    }

    /**
     * @param element
     * @return
     */
    public int add(T element) {
        elementData.add(element);
        return elementData.size() - 1;
    }

    /**
     * @param i
     * @param element
     */
    public void set(int i, T element) {
        elementData.set(i, element);
    }

}
