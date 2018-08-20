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

package freerails.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * All objects derived from Identifiable must not implement their own equals() or hashCode() functions. The idea is that
 * objects with the same id are equal. Objects of that type are typically used in some sort of set and can be identified
 * by their id from outside.
 */
public class Identifiable implements Comparable<Identifiable>, Serializable {

    private final int id;

    /**
     * Parameter-less constructor used for deserialization.
     */
    public Identifiable() {
        this(-1);
    }

    /**
     *
     * @param id
     */
    public Identifiable(int id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Identifiable)) {
            return false;
        }
        Identifiable o = (Identifiable) obj;
        return this.compareTo(o) == 0;
    }

    /**
     *
     * @return
     */
    @Override
    public final int hashCode() {
        return id;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(@NotNull Identifiable o) {
        return Integer.compare(id, o.id);
    }

    /**
     *
     * @param id
     * @param list
     * @param <T>
     * @return
     */
    public static <T extends Identifiable> T getById(int id, Iterable<T> list) {
        for (T t: list) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }
}

