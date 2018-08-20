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

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NoSuchElementException;

// TODO to follow the iterator interface more closely make next and previous return the elements and not get (removes the need for current)
// TODO implement Iterator interface
// TODO if not has previous and hasNext is used at the same time, what about Iterator on standard reversed list?
/**
 *
 */
public class BidirectionalIterator<E> {

    private final List<E> list;
    private int currentIndex = -1;
    private E current = null;

    // TODO defensive copy of list?
    /**
     */
    public BidirectionalIterator(@NotNull List<E> list) {
        this.list = list;
        if (!this.list.isEmpty()) {
            currentIndex = 0;
            current = list.get(currentIndex);
        }
    }

    /**
     * @return
     */
    public E get() {
        return current;
    }

    /**
     * @return
     */
    public boolean hasNext() {
        return (currentIndex + 1) < list.size();
    }

    /**
     *
     */
    public void next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        currentIndex++;
        current = list.get(currentIndex);
    }

    /**
     *
     */
    public void gotoLast() {
        currentIndex = list.size() - 1;
        current = list.get(currentIndex);
    }

    /**
     * @return
     */
    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    /**
     * @throws NoSuchElementException
     */
    public void previous() throws NoSuchElementException {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        currentIndex--;
        current = list.get(currentIndex);
    }

}
