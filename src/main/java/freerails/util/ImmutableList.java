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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Immutable list encapsulation with some convenience methods.
 */
public final class ImmutableList<E extends Serializable> implements Serializable {

    private static final long serialVersionUID = 2669191159273299313L;
    private final E[] values;

    /**
     * @param items
     */
    public ImmutableList(E... items) {
        values = items.clone();
    }

    /**
     * @param list
     */
    public ImmutableList(List<E> list) {
        values = list.toArray((E[]) new Serializable[list.size()]);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ImmutableList)) return false;
        final ImmutableList other = (ImmutableList) obj;
        return Arrays.equals(values, other.values);
    }

    @Override
    public int hashCode() {
        return values.length;
    }

    /**
     * @throws NullPointerException
     */
    public void containsNulls() throws NullPointerException {
        for (E value : values) {
            Utils.verifyNotNull(value);
        }
    }

    /**
     * @return
     */
    public int size() {
        return values.length;
    }

    /**
     * @param i
     * @return
     */
    public E get(int i) {
        return values[i];
    }
}
