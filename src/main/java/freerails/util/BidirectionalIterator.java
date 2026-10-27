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

import java.util.List;
import java.util.NoSuchElementException;

// TODO to follow the iterator interface more closely make next and previous return the elements and not get (removes the need for current)
// TODO implement Iterator interface
// TODO if not has previous and hasNext is used at the same time, what about Iterator on standard reversed list?
/**
 * Traverses a list forwards and backwards while exposing the current element.
 *
 * <p>The iterator starts on the first element (or has no current element for
 * an empty list). The list is retained rather than copied, so subsequent
 * changes to it may affect traversal.
 *
 * @param <E> element type
 */
public class BidirectionalIterator<E> {

    private final List<E> list;
    private int currentIndex = -1;
    private E current = null;

    // TODO defensive copy of list?
    /**
     * Creates an iterator over a list.
     *
     * @param list list to traverse
     * @throws NullPointerException if {@code list} is null
     */
    public BidirectionalIterator(@NotNull List<E> list) {
        this.list = list;
        if (!this.list.isEmpty()) {
            currentIndex = 0;
            current = list.get(currentIndex);
        }
    }

    /**
     * Returns the current element.
     *
     * @return the current element, or {@code null} if the list is empty
     */
    public E get() {
        return current;
    }

    /**
     * Indicates whether there is an element after the current one.
     *
     * @return {@code true} if advancing would move to another element
     */
    public boolean hasNext() {
        return currentIndex + 1 < list.size();
    }

    /**
     * Advances to the next element.
     *
     * @throws NoSuchElementException if there is no next element
     */
    public void next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        currentIndex++;
        current = list.get(currentIndex);
    }

    /**
     * Moves directly to the last element.
     *
     * @throws IndexOutOfBoundsException if the list is empty
     */
    public void gotoLast() {
        currentIndex = list.size() - 1;
        current = list.get(currentIndex);
    }

    /**
     * Indicates whether there is an element before the current one.
     *
     * @return {@code true} if moving backwards would reach another element
     */
    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    /**
     * Moves to the previous element.
     *
     * @throws NoSuchElementException if there is no previous element
     */
    public void previous() throws NoSuchElementException {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        currentIndex--;
        current = list.get(currentIndex);
    }

}
