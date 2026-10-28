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

import java.io.Serializable;

/**
 * Base class for objects whose identity and ordering are determined by an
 * integer identifier.
 *
 * <p>Subclasses should not override {@link #equals(Object)} or
 * {@link #hashCode()}, so objects with the same identifier retain the same
 * equality and hashing behavior.
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
     * Creates an object with the given identifier.
     *
     * @param id identifier for this object
     */
    public Identifiable(int id) {
        this.id = id;
    }

    /**
     * Returns this object's identifier.
     *
     * @return the identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Tests whether another identifiable object has the same identifier.
     *
     * @param obj object to compare
     * @return {@code true} if {@code obj} is an {@code Identifiable} with the
     * same identifier
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Identifiable o)) {
            return false;
        }
        return compareTo(o) == 0;
    }

    /**
     * Returns the identifier as this object's hash code.
     *
     * @return the identifier
     */
    @Override
    public final int hashCode() {
        return id;
    }

    /**
     * Compares this object with another by identifier.
     *
     * @param o object to compare with
     * @return a negative integer, zero, or a positive integer as this
     * identifier is less than, equal to, or greater than {@code o}'s
     * @throws NullPointerException if {@code o} is null
     */
    @Override
    public int compareTo(@NotNull Identifiable o) {
        return Integer.compare(id, o.id);
    }

    /**
     * Finds the first object with the specified identifier.
     *
     * @param id identifier to search for
     * @param list objects to search
     * @param <T> identifiable element type
     * @return the first matching object, or {@code null} if none is found
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
