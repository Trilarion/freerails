/*
 * Created on 21-Jul-2005
 *
 */
package freerails.util;

import java.util.ArrayList;

/**
 *
 * @param <T>
 */
public class List1DImpl<T> implements List1D<T> {

    private static final long serialVersionUID = 8285123045287237133L;
    private final ArrayList<T> elementData;

    /**
     *
     */
    public List1DImpl() {
        elementData = new ArrayList<>();
    }

    /**
     *
     * @param initialSize
     */
    public List1DImpl(int initialSize) {
        elementData = new ArrayList<>();
        for (int i = 0; i < initialSize; i++) {
            elementData.add(null);
        }
    }

    /**
     *
     * @return
     */
    public int size() {
        return elementData.size();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof List1D && Lists.equals(this, (List1D) obj);
    }

    @Override
    public int hashCode() {
        return size();
    }

    /**
     *
     * @param i
     * @return
     */
    public T get(int i) {
        return elementData.get(i);
    }

    /**
     *
     * @return
     */
    public T removeLast() {
        int last = elementData.size() - 1;
        return elementData.remove(last);
    }

    /**
     *
     * @param element
     * @return
     */
    public int add(T element) {
        elementData.add(element);
        return elementData.size() - 1;
    }

    /**
     *
     * @param i
     * @param element
     */
    public void set(int i, T element) {
        elementData.set(i, element);
    }

}
