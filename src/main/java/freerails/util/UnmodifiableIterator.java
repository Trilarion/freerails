/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.util;

import java.util.Iterator;

/**
 * A read-only view of another iterator.
 *
 * <p>Traversal delegates to the wrapped iterator, but removal is not
 * supported.
 *
 * Similar to org.apache.commons.collections4.Unmodifiable.
 *
 * @param <E> element type
 */
public final class UnmodifiableIterator<E> implements Iterator<E> {

    /** The iterator being decorated. */
    private final Iterator<? extends E> iterator;

    /**
     * Creates a read-only wrapper around an iterator.
     *
     * @param iterator iterator to decorate
     * @throws NullPointerException if {@code iterator} is null
     */
    private UnmodifiableIterator(final Iterator<? extends E> iterator) {
        super();
        Utils.verifyNotNull(iterator);
        this.iterator = iterator;
    }

    /**
     * Creates a read-only wrapper around an iterator.
     *
     * @param iterator iterator to decorate
     * @param <E> element type
     * @return an iterator that delegates traversal and rejects removal
     */
    public static <E> UnmodifiableIterator<E> of(Iterator<? extends E> iterator) {
        return new UnmodifiableIterator<>(iterator);
    }

    /**
     * Indicates whether the wrapped iterator has another element.
     *
     * @return {@code true} if another element is available
     */
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Returns the next element from the wrapped iterator.
     *
     * @return the next element
     * @throws java.util.NoSuchElementException if no element remains
     */
    @Override
    public E next() {
        return iterator.next();
    }

    /**
     * Always throws because this iterator does not support removal.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove() from an UnmodifiableIterator.");
    }

}
