/*
 * Created on 21-Jul-2005
 *
 */
package freerails.util;

import java.io.Serializable;

public interface List2D<T> extends Serializable {

    int sizeD1();

    int sizeD2(int d1);

    T get(int d1, int d2);

    T removeLastD2(int d1);

    int removeLastD1();

    int addD1();

    int addD2(int d1, T element);

    void set(int d1, int d2, T element);

}
