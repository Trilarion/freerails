/*
 * Created on 12-Jul-2005
 *
 */
package freerails.world.common;

import freerails.util.Immutable;
import freerails.world.FreerailsSerializable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @param <E>
 */
@Immutable
public class ImHashSet<E extends FreerailsSerializable> implements
        FreerailsSerializable {

    private static final long serialVersionUID = -4098862905501171517L;

    private final HashSet<E> hashSet;

    /**
     *
     * @param hashSet
     */
    public ImHashSet(HashSet<E> hashSet) {
        this.hashSet = new HashSet<>(hashSet);
    }

    /**
     *
     * @param values
     */
    public ImHashSet(E... values) {
        this.hashSet = new HashSet<>();
        Collections.addAll(hashSet, values);
    }

    /**
     *
     * @param values
     */
    public ImHashSet(List<E> values) {
        this.hashSet = new HashSet<>();
        hashSet.addAll(values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ImHashSet))
            return false;

        final ImHashSet imHashSet = (ImHashSet) o;

        return hashSet.equals(imHashSet.hashSet);
    }

    @Override
    public int hashCode() {
        return hashSet.hashCode();
    }

    /**
     *
     * @param e
     * @return
     */
    public boolean contains(E e) {
        return hashSet.contains(e);
    }

    /**
     *
     * @return
     */
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            final Iterator<E> it = hashSet.iterator();

            public boolean hasNext() {
                return it.hasNext();
            }

            public E next() {
                return it.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();

            }

        };
    }

}
