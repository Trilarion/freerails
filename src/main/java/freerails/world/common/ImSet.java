/*
 * Created on 04-Jul-2005
 *
 */
package freerails.world.common;

import freerails.util.Immutable;

import java.util.HashSet;
import java.util.Set;

/**
 * An immutable set.
 *
 * @author Luke
 * @param <E>
 */
@Immutable
public final class ImSet<E extends FreerailsSerializable> implements
        FreerailsSerializable {

    private static final long serialVersionUID = -8075637749158447780L;

    private final HashSet<E> hashSet;

    /**
     *
     * @param data
     */
    public ImSet(Set<E> data) {
        hashSet = new HashSet<>(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ImSet))
            return false;

        final ImSet imSet = (ImSet) o;

        return hashSet.equals(imSet.hashSet);
    }

    @Override
    public int hashCode() {
        return hashSet.hashCode();
    }

    /**
     *
     * @param element
     * @return
     */
    public boolean contains(E element) {
        return hashSet.contains(element);
    }

}
