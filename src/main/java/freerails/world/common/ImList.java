/*
 * Created on 04-Jul-2005
 *
 */
package freerails.world.common;

import freerails.util.Immutable;

import java.util.Arrays;
import java.util.List;

/**
 * An immutable List
 *
 * @author Luke
 */
@Immutable
public final class ImList<E extends FreerailsSerializable> implements
        FreerailsSerializable {

    private static final long serialVersionUID = 2669191159273299313L;

    private final E[] elementData;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ImList))
            return false;

        final ImList imList = (ImList) o;

        if (!Arrays.equals(elementData, imList.elementData))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return elementData.length;
    }

    @SuppressWarnings("unchecked")
    public ImList(E... items) {
        elementData = items.clone();
//        elementData = (E[]) new FreerailsSerializable[items.length];
//        for (int i = 0; i < items.length; i++) {
//            elementData[i] = items[i];
//        }
    }

    @SuppressWarnings("unchecked")
    public ImList(List<E> list) {
        elementData = list.toArray((E[]) new FreerailsSerializable[list.size()]);
//        elementData = (E[]) new FreerailsSerializable[list.size()];
//        for (int i = 0; i < list.size(); i++) {
//            elementData[i] = list.get(i);
//        }
    }

    public void checkForNulls() throws NullPointerException {
        for (int i = 0; i < elementData.length; i++) {
            if (null == elementData[i])
                throw new NullPointerException();
        }
    }

    public int size() {
        return elementData.length;
    }

    public E get(int i) {
        return elementData[i];
    }

}
