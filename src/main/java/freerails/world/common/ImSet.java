/*
 * Created on 04-Jul-2005
 *
 */
package freerails.world.common;

import java.util.HashSet;
import java.util.Set;

import freerails.util.Immutable;

/**
 * An immutable set.
 * 
 * @author Luke
 * 
 */
@Immutable
public final class ImSet<E extends FreerailsSerializable> implements
        FreerailsSerializable {

    private static final long serialVersionUID = -8075637749158447780L;

    private final HashSet<E> hashSet;

    public ImSet(Set<E> data) {
        hashSet = new HashSet<E>(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ImSet))
            return false;

        final ImSet imSet = (ImSet) o;

        if (!hashSet.equals(imSet.hashSet))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hashSet.hashCode();
    }

    public boolean contains(E element) {
        return hashSet.contains(element);
    }

}
