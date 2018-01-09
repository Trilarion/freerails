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

// TODO replace this with Javas Collections.unmodifiableList

/**
 * An immutable List
 *
 * @param <E>
 */
// TODO replace by standard Java stuff
@Immutable
public final class ImList<E extends Serializable> implements
        Serializable {

    private static final long serialVersionUID = 2669191159273299313L;

    private final E[] elementData;

    /**
     * @param items
     */
    @SuppressWarnings("unchecked")
    public ImList(E... items) {
        elementData = items.clone();
    }

    /**
     * @param list
     */
    @SuppressWarnings("unchecked")
    public ImList(List<E> list) {
        elementData = list.toArray((E[]) new Serializable[list.size()]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ImList))
            return false;

        final ImList imList = (ImList) o;

        return Arrays.equals(elementData, imList.elementData);
    }

    @Override
    public int hashCode() {
        return elementData.length;
    }

    /**
     * @throws NullPointerException
     */
    public void checkForNulls() throws NullPointerException {
        for (E anElementData : elementData) {
            if (null == anElementData)
                throw new NullPointerException();
        }
    }

    /**
     * @return
     */
    public int size() {
        return elementData.length;
    }

    /**
     * @param i
     * @return
     */
    public E get(int i) {
        return elementData[i];
    }

}
