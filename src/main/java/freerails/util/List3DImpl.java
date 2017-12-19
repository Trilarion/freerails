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
 * Created on 21-Jul-2005
 *
 */
package freerails.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @param <T>
 */
public class List3DImpl<T> implements List3D<T> {

    private static final long serialVersionUID = 1353309875727204066L;

    private final ArrayList<ArrayList<ArrayList<T>>> elementData = new ArrayList<>();

    /**
     *
     * @param d1
     * @param d2
     */
    public List3DImpl(int d1, int d2) {
        for (int i = 0; i < d1; i++) {
            ArrayList<ArrayList<T>> dim2 = new ArrayList<>();
            elementData.add(dim2);
            for (int j = 0; j < d2; j++) {
                dim2.add(new ArrayList<>());
            }
        }
    }

    /**
     *
     * @return
     */
    public int sizeD1() {
        return elementData.size();
    }

    /**
     *
     * @param d1
     * @return
     */
    public int sizeD2(int d1) {
        return elementData.get(d1).size();
    }

    /**
     *
     * @param d1
     * @param d2
     * @return
     */
    public int sizeD3(int d1, int d2) {
        return elementData.get(d1).get(d2).size();
    }

    /**
     *
     * @param d1
     * @param d2
     * @param d3
     * @return
     */
    public T get(int d1, int d2, int d3) {
        return elementData.get(d1).get(d2).get(d3);
    }

    /**
     *
     * @param d1
     * @param d2
     * @return
     */
    public T removeLastD3(int d1, int d2) {
        ArrayList<T> dim3 = elementData.get(d1).get(d2);
        int last = dim3.size() - 1;
        T element = dim3.get(last);
        dim3.remove(last);
        return element;
    }

    /**
     *
     */
    public void removeLastD1() {
        int last = elementData.size() - 1;
        if (elementData.get(last).size() > 0)
            throw new IllegalStateException(String.valueOf(last));
        elementData.remove(last);
    }

    /**
     *
     * @param d1
     */
    public void removeLastD2(int d1) {
        ArrayList<ArrayList<T>> dim2 = elementData.get(d1);
        int last = dim2.size() - 1;
        ArrayList<T> dim3 = dim2.get(last);
        if (dim3.size() > 0)
            throw new IllegalStateException(String.valueOf(d1));
        dim2.remove(last);
    }

    /**
     *
     * @return
     */
    public int addD1() {
        ArrayList<ArrayList<T>> dim2 = new ArrayList<>();
        elementData.add(dim2);
        return elementData.size() - 1;
    }

    /**
     *
     * @param d1
     * @return
     */
    public int addD2(int d1) {
        ArrayList<ArrayList<T>> dim2 = elementData.get(d1);
        dim2.add(new ArrayList<>());
        return dim2.size() - 1;
    }

    /**
     *
     * @param d1
     * @param d2
     * @param element
     * @return
     */
    public int addD3(int d1, int d2, T element) {
        ArrayList<T> dim3 = elementData.get(d1).get(d2);
        dim3.add(element);
        return dim3.size() - 1;
    }

    /**
     *
     * @param d1
     * @param d2
     * @param d3
     * @param element
     */
    public void set(int d1, int d2, int d3, T element) {
        ArrayList<T> dim3 = elementData.get(d1).get(d2);
        dim3.set(d3, element);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof List3D && Lists.equals(this, (List3D) obj);
    }

    @Override
    public int hashCode() {
        return sizeD1();
    }

    /**
     *
     * @param d1
     * @param d2
     * @return
     */
    public List<T> get(int d1, int d2) {
        return elementData.get(d1).get(d2);
    }

}
