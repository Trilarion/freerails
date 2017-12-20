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
 *
 */
package freerails.util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * An immutable set.
 *
 * @param <E>
 */
@Immutable
public final class ImSet<E extends Serializable> implements
        Serializable {

    private static final long serialVersionUID = -8075637749158447780L;

    private final HashSet<E> hashSet;

    /**
     * @param data
     */
    public ImSet(Set<E> data) {
        hashSet = new HashSet<>(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ImSet))
            return false;

        final ImSet imSet = (ImSet) o;

        return hashSet.equals(imSet.hashSet);
    }

    @Override
    public int hashCode() {
        return hashSet.hashCode();
    }

    /**
     * @param element
     * @return
     */
    public boolean contains(E element) {
        return hashSet.contains(element);
    }

}
