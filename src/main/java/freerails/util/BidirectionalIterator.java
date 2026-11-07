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

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Traverses a list forwards and backwards using a cursor between elements.
 *
 * <p>The cursor starts before the first element, as with a standard iterator.
 * The list is retained rather than copied, so subsequent changes to it may
 * affect traversal. Traversal follows the order of the supplied list.
 *
 * @param <E> element type
 */
public class BidirectionalIterator<E> implements Iterator<E> {

    private final List<E> list;
    private int cursor;

    /**
     * Creates an iterator over a list.
     *
     * @param list list to traverse
     * @throws NullPointerException if {@code list} is null
     */
    public BidirectionalIterator(@NotNull List<E> list) {
        this.list = list;
        cursor = 0;
    }

    /**
     * Indicates whether there is an element after the cursor.
     *
     * @return {@code true} if {@link #next()} can return another element
     */
    @Override
    public boolean hasNext() {
        return cursor < list.size();
    }

    /**
     * Returns the next element and advances the cursor.
     *
     * @return the next element
     * @throws NoSuchElementException if there is no next element
     */
    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return list.get(cursor++);
    }

    /**
     * Moves the cursor after the last element.
     *
     * @throws IndexOutOfBoundsException if the list is empty
     */
    public void gotoLast() {
        if (list.isEmpty()) {
            throw new IndexOutOfBoundsException("Cannot move to the end of an empty list.");
        }
        cursor = list.size();
    }

    /**
     * Indicates whether there is an element before the cursor.
     *
     * @return {@code true} if {@link #previous()} can return another element
     */
    public boolean hasPrevious() {
        return cursor > 0;
    }

    /**
     * Returns the previous element and moves the cursor backwards.
     *
     * @return the previous element
     * @throws NoSuchElementException if there is no previous element
     */
    public E previous() throws NoSuchElementException {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        return list.get(--cursor);
    }

}
