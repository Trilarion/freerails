/*
 * Created on 21-Jul-2005
 *
 */
package freerails.util;

import java.io.Serializable;

public interface List1D<T> extends Serializable {

    int size();

    T get(int i);

    T removeLast();

    int add(T element);

    void set(int i, T element);

}
