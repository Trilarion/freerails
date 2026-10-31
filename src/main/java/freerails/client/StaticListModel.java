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

package freerails.client;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Exposes a collection of elements through Swing's list model interface.
 *
 * @param <E> the E type parameter
 */
public class StaticListModel<E> implements ListModel<E> {

    private List<E> list;

    /**
     * Creates a static list model instance.
     * @param list the list
     */
    public StaticListModel(@NotNull List<E> list) {
        this.list = list;
    }

    /**
     * Creates a static list model instance.
     * @param c the c
     */
    public StaticListModel(@NotNull Collection<? extends E> c) {
        list = new ArrayList<>(c);
    }

    /**
     * Returns the size.
     * @return the size
     */
    @Override
    public int getSize() {
        return list.size();
    }

    /**
     * Returns the element at.
     * @param index the index
     * @return the element at
     */
    @Override
    public E getElementAt(int index) {
        return list.get(index);
    }

    /**
     * Adds the list data listener.
     * @param l the l
     */
    @Override
    public void addListDataListener(ListDataListener l) {
    }

    /**
     * Removes the list data listener.
     * @param l the l
     */
    @Override
    public void removeListDataListener(ListDataListener l) {
    }
}
