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
import java.util.Arrays;

/**
 *
 */
public class ListKey implements Comparable<ListKey>, Serializable {

    private static final long serialVersionUID = -4939641035786937927L;
    private final Type type;
    private final int[] index;
    private final Enum listID;

    /**
     * @param t
     * @param listID
     * @param i
     */
    public ListKey(Type t, Enum listID, int... i) {
        type = t;
        index = i.clone();
        this.listID = listID;
    }

    /**
     * @return
     */
    public int[] getIndex() {
        return index.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ListKey))
            return false;

        final ListKey listKey = (ListKey) o;

        if (!Arrays.equals(index, listKey.index))
            return false;
        if (!listID.equals(listKey.listID))
            return false;
        return type.equals(listKey.type);
    }

    @Override
    public int hashCode() {
        int result;
        result = type.hashCode();
        result = 29 * result + listID.hashCode();
        return result;
    }

    /**
     * @return
     */
    public Type getType() {
        return type;
    }

    public int compareTo(ListKey o) {

        if (o.listID != listID)
            return o.listID.ordinal() - listID.ordinal();

        if (index.length != o.index.length)
            return index.length - o.index.length;

        for (int i = 0; i < index.length; i++) {
            if (index[i] != o.index[i])
                return index[i] - o.index[i];
        }

        if (o.type != type)
            return o.type.ordinal() - type.ordinal();

        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(listID);
        sb.append(' ');
        sb.append(type);
        sb.append(" index ");
        for (int anIndex : index) {
            sb.append('[');
            sb.append(anIndex);
            sb.append(']');
        }
        return sb.toString();
    }

    /**
     * @return
     */
    public Enum getListID() {
        return listID;
    }

    /**
     *
     */
    public enum Type {

        /**
         *
         */
        Element,

        /**
         *
         */
        EndPoint
    }

}
