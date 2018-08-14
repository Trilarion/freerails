/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
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
