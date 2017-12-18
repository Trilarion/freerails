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
public interface List2D<T> extends Serializable {

    /**
     *
     * @return
     */
    int sizeD1();

    /**
     *
     * @param d1
     * @return
     */
    int sizeD2(int d1);

    /**
     *
     * @param d1
     * @param d2
     * @return
     */
    T get(int d1, int d2);

    /**
     *
     * @param d1
     * @return
     */
    T removeLastD2(int d1);

    /**
     *
     * @return
     */
    int removeLastD1();

    /**
     *
     * @return
     */
    int addD1();

    /**
     *
     * @param d1
     * @param element
     * @return
     */
    int addD2(int d1, T element);

    /**
     *
     * @param d1
     * @param d2
     * @param element
     */
    void set(int d1, int d2, T element);

}
