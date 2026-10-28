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

import java.io.Serializable;
import java.util.Objects;

/**
 * Generic immutable pair for cases that need two values without additional
 * domain-specific behavior.
 * <p>
 * In all examples where semantics would help coding it might be better to use
 * a meaningful naming convention. However for the cases, where the use is simple
 * and limited, Pair might be a good option.
 * <p>
 * Should be HashMap/Comparable/Iterable save.
 * <p>
 * Inspired by https://stackoverflow.com/questions/156275/what-is-the-equivalent-of-the-c-pairl-r-in-java
 *
 * @param <A> Type of first element.
 * @param <B> Type of second element.
 * @param a   First element.
 * @param b   Second element.
 */
public record Pair<A, B>(A a, B b) implements Serializable {

    /**
     * Constructor setting the values for both elements.
     *
     * @param a First element.
     * @param b Second element.
     */
    public Pair {
    }

    /**
     * @return First element.
     */
    @Override
    public A a() {
        return a;
    }

    /**
     * @return Second element.
     */
    @Override
    public B b() {
        return b;
    }

    /**
     * Compares this pair with another pair.
     *
     * <p>Both elements are compared with {@link Objects#equals(Object, Object)}.
     *
     * @param obj object to compare
     * @return {@code true} if {@code obj} is a pair of the same runtime class
     * with equal elements
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            @SuppressWarnings("unchecked") Pair<A, B> o = (Pair<A, B>) obj;
            return Objects.equals(a, o.a) && Objects.equals(b, o.b);
        }
        return false;
    }

    /**
     * @return Combines the toString() methods of both elements.
     */
    @Override
    public String toString() {
        return "Pair(" + a + ", " + b + ")";
    }
}