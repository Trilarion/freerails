package freerails.world.common;

import freerails.util.Immutable;
import freerails.world.FreerailsSerializable;

import java.util.Arrays;
import java.util.List;

// TODO replace this with Javas Collections.unmodifiableList

/**
 * An immutable List
 *
 * @param <E>
 */
@Immutable
public final class ImList<E extends FreerailsSerializable> implements
        FreerailsSerializable {

    private static final long serialVersionUID = 2669191159273299313L;

    private final E[] elementData;

    /**
     *
     * @param items
     */
    @SuppressWarnings("unchecked")
    public ImList(E... items) {
        elementData = items.clone();
//        elementData = (E[]) new FreerailsSerializable[items.length];
//        for (int i = 0; i < items.length; i++) {
//            elementData[i] = items[i];
//        }
    }

    /**
     *
     * @param list
     */
    @SuppressWarnings("unchecked")
    public ImList(List<E> list) {
        elementData = list.toArray((E[]) new FreerailsSerializable[list.size()]);
//        elementData = (E[]) new FreerailsSerializable[list.size()];
//        for (int i = 0; i < list.size(); i++) {
//            elementData[i] = list.get(i);
//        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ImList))
            return false;

        final ImList imList = (ImList) o;

        return Arrays.equals(elementData, imList.elementData);
    }

    @Override
    public int hashCode() {
        return elementData.length;
    }

    /**
     *
     * @throws NullPointerException
     */
    public void checkForNulls() throws NullPointerException {
        for (E anElementData : elementData) {
            if (null == anElementData)
                throw new NullPointerException();
        }
    }

    /**
     *
     * @return
     */
    public int size() {
        return elementData.length;
    }

    /**
     *
     * @param i
     * @return
     */
    public E get(int i) {
        return elementData[i];
    }

}
