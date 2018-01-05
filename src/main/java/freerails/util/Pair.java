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
 * @param <A>
 * @param <B>
 */
public class Pair<A, B> {

    private final A a;
    private final B b;

    /**
     * @param a
     * @param b
     */
    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    /**
     * @param other
     * @return
     */
    @SuppressWarnings("unused")
    public boolean equals(Pair<A, B> other) {
        return this == other || null != other && (a.equals(other.a) && b.equals(other.b));
    }

    public String toString() {
        return '(' + a.toString() + ", " + b.toString() + ')';
    }

    /**
     * @return
     */
    public A getA() {
        return a;
    }

    /**
     * @return
     */
    public B getB() {
        return b;
    }

}