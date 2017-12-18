/*
 * Created on 21-Jul-2005
 *
 */
package freerails.util;

import java.io.Serializable;

/**
 *
 * @author jkeller1
 * @param <T>
 */
public interface List1D<T> extends Serializable {

    /**
     *
     * @return
     */
    int size();

    /**
     *
     * @param i
     * @return
     */
    T get(int i);

    /**
     *
     * @return
     */
    T removeLast();

    /**
     *
     * @param element
     * @return
     */
    int add(T element);

    /**
     *
     * @param i
     * @param element
     */
    void set(int i, T element);

}
