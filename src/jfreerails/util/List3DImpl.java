/*
 * Created on 21-Jul-2005
 *
 */
package jfreerails.util;

import java.util.ArrayList;
import java.util.List;

public class List3DImpl<T> implements List3D<T> {

    private static final long serialVersionUID = 1353309875727204066L;

    private ArrayList<ArrayList<ArrayList<T>>> elementData = new ArrayList<ArrayList<ArrayList<T>>>();

    public List3DImpl(int d1, int d2) {
        for (int i = 0; i < d1; i++) {
            ArrayList<ArrayList<T>> dim2 = new ArrayList<ArrayList<T>>();
            elementData.add(dim2);
            for (int j = 0; j < d2; j++) {
                dim2.add(new ArrayList<T>());
            }
        }
    }

    public int sizeD1() {
        return elementData.size();
    }

    public int sizeD2(int d1) {
        return elementData.get(d1).size();
    }

    public int sizeD3(int d1, int d2) {
        return elementData.get(d1).get(d2).size();
    }

    public T get(int d1, int d2, int d3) {
        return elementData.get(d1).get(d2).get(d3);
    }

    public T removeLastD3(int d1, int d2) {
        ArrayList<T> dim3 = elementData.get(d1).get(d2);
        int last = dim3.size() - 1;
        T element = dim3.get(last);
        dim3.remove(last);
        return element;
    }

    public void removeLastD1() {
        int last = elementData.size() - 1;
        if (elementData.get(last).size() > 0)
            throw new IllegalStateException(String.valueOf(last));
        elementData.remove(last);
    }

    public void removeLastD2(int d1) {
        ArrayList<ArrayList<T>> dim2 = elementData.get(d1);
        int last = dim2.size() - 1;
        ArrayList<T> dim3 = dim2.get(last);
        if (dim3.size() > 0)
            throw new IllegalStateException(String.valueOf(d1));
        dim2.remove(last);
    }

    public int addD1() {
        ArrayList<ArrayList<T>> dim2 = new ArrayList<ArrayList<T>>();
        elementData.add(dim2);
        return elementData.size() - 1;
    }

    public int addD2(int d1) {
        ArrayList<ArrayList<T>> dim2 = elementData.get(d1);
        dim2.add(new ArrayList<T>());
        return dim2.size() - 1;
    }

    public int addD3(int d1, int d2, T element) {
        ArrayList<T> dim3 = elementData.get(d1).get(d2);
        dim3.add(element);
        return dim3.size() - 1;
    }

    public void set(int d1, int d2, int d3, T element) {
        ArrayList<T> dim3 = elementData.get(d1).get(d2);
        dim3.set(d3, element);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof List3D))
            return false;
        return Lists.equals(this, (List3D) obj);
    }

    @Override
    public int hashCode() {
        return sizeD1();
    }

    public List<T> get(int d1, int d2) {
        return elementData.get(d1).get(d2);
    }

}
