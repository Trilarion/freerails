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

/**
 * Generic immutable Pair<A, B> implementation to help when you really only need
 * a simple, immutable Pair, nothing else.
 *
 * In all examples where semantics would help coding it might be better to use
 * a meaningful naming convention. However for the cases, where the use is simple
 * and limited, Pair might be a good option.
 *
 * Should be HashMap/Comparable/Iterable save.
 *
 * @param <A> Type of first element.
 * @param <B> Type of second element.
 */
public class Pair<A, B> {

    /**
     * Two final elements of two different types.
     */
    private final A a;
    private final B b;

    /**
     * Constructor setting the values for both elements.
     *
     * @param a First element.
     * @param b Second element.
     */
    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    /**
     * @return First element.
     */
    public A getA() {
        return a;
    }

    /**
     * @return Second element.
     */
    public B getB() {
        return b;
    }

    /**
     * Calculates a combined hash code.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        int hashA = 0;
        if (a != null) {
            hashA = a.hashCode();
        }
        int hashB = 0;
        if (b != null) {
            hashB = b.hashCode();
        }
        return hashA * 31 + hashB;
    }

    /**
     * Implementation of an equals method.
     *
     * @param obj Another object.
     * @return True if they are of the same class and not null and both elements
     * are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && getClass() == obj.getClass() && a != null && b != null) {
            @SuppressWarnings("unchecked") Pair<A, B> o = (Pair<A, B>) obj;
            return a.equals(o.getA()) && b.equals(o.getB());
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