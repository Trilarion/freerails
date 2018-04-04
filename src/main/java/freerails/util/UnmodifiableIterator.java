package freerails.util;

import java.util.Iterator;

/**
 * Decorates an iterator such that it cannot be modified.
 *
 * Attempts to modify it will result in an UnsupportedOperationException.
 *
 * Similar to org.apache.commons.collections4.Unmodifiable.
 *
 * @param <E>
 */
public final class UnmodifiableIterator<E> implements Iterator<E> {

    /** The iterator being decorated */
    private final Iterator<? extends E> iterator;

    /**
     * @param iterator the iterator to decorate
     */
    private UnmodifiableIterator(final Iterator<? extends E> iterator) {
        super();
        Utils.verifyNotNull(iterator);
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public E next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove() from an UnmodifiableIterator.");
    }

}
