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

package freerails.world.common;

import freerails.util.Immutable;
import freerails.world.FreerailsSerializable;

import java.util.Arrays;
import java.util.List;

// TODO replace this with Javas Collections.unmodifiableList

/**
 * An immutable List
 *
 * @param <E>
 */
@Immutable
public final class ImList<E extends FreerailsSerializable> implements
        FreerailsSerializable {

    private static final long serialVersionUID = 2669191159273299313L;

    private final E[] elementData;

    /**
     *
     * @param items
     */
    @SuppressWarnings("unchecked")
    public ImList(E... items) {
        elementData = items.clone();
//        elementData = (E[]) new FreerailsSerializable[items.length];
//        for (int i = 0; i < items.length; i++) {
//            elementData[i] = items[i];
//        }
    }

    /**
     *
     * @param list
     */
    @SuppressWarnings("unchecked")
    public ImList(List<E> list) {
        elementData = list.toArray((E[]) new FreerailsSerializable[list.size()]);
//        elementData = (E[]) new FreerailsSerializable[list.size()];
//        for (int i = 0; i < list.size(); i++) {
//            elementData[i] = list.get(i);
//        }
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
     *
     * @throws NullPointerException
     */
    public void checkForNulls() throws NullPointerException {
        for (E anElementData : elementData) {
            if (null == anElementData)
                throw new NullPointerException();
        }
    }

    /**
     *
     * @return
     */
    public int size() {
        return elementData.length;
    }

    /**
     *
     * @param i
     * @return
     */
    public E get(int i) {
        return elementData[i];
    }

}
