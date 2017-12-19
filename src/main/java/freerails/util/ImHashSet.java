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

/*
 * Created on 12-Jul-2005
 *
 */
package freerails.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * @param <E>
 */
@Immutable
public class ImHashSet<E extends Serializable> implements
        Serializable {

    private static final long serialVersionUID = -4098862905501171517L;

    private final HashSet<E> hashSet;

    /**
     * @param hashSet
     */
    public ImHashSet(HashSet<E> hashSet) {
        this.hashSet = new HashSet<>(hashSet);
    }

    /**
     * @param values
     */
    public ImHashSet(E... values) {
        this.hashSet = new HashSet<>();
        Collections.addAll(hashSet, values);
    }

    /**
     * @param values
     */
    public ImHashSet(List<E> values) {
        this.hashSet = new HashSet<>();
        hashSet.addAll(values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ImHashSet))
            return false;

        final ImHashSet imHashSet = (ImHashSet) o;

        return hashSet.equals(imHashSet.hashSet);
    }

    @Override
    public int hashCode() {
        return hashSet.hashCode();
    }

    /**
     * @param e
     * @return
     */
    public boolean contains(E e) {
        return hashSet.contains(e);
    }

    /**
     * @return
     */
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            final Iterator<E> it = hashSet.iterator();

            public boolean hasNext() {
                return it.hasNext();
            }

            public E next() {
                return it.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();

            }

        };
    }

}
