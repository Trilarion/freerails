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
 * A directed straight-line segment between two two-dimensional integer
 * points. Coordinate units are arbitrary.
 */
public class Segment implements Serializable {

    private static final long serialVersionUID = 3257853198755705393L;
    private final Vec2D a;
    private final Vec2D b;

    /**
     * Creates a segment from endpoint {@code a} to endpoint {@code b}.
     *
     * @param a starting endpoint
     * @param b ending endpoint
     * @throws NullPointerException if either endpoint is null
     */
    public Segment(Vec2D a, Vec2D b) {
        this.a = Objects.requireNonNull(a, "a");
        this.b = Objects.requireNonNull(b, "b");
    }

    /**
     * Returns a hash code based on the ordered endpoints.
     *
     * @return this segment's hash code
     */
    @Override
    public int hashCode() {
        return a.hashCode() * 29 + b.hashCode();
    }

    /**
     * Returns the Euclidean distance between the endpoints.
     *
     * @return the segment length
     */
    public double getLength() {
        return Math.hypot((double) a.x - b.x, (double) a.y - b.y);
    }

    /**
     * Tests whether another object is a segment with the same ordered
     * endpoints.
     *
     * @param obj object to compare
     * @return {@code true} if {@code obj} is an equal segment
     */
    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof Segment) {
            Segment line = (Segment) obj;
            return a.equals(line.a) && b.equals(line.b);
        }
        return false;
    }

    /**
     * Returns a string containing the two endpoints.
     *
     * @return this segment's textual representation
     */
    @Override
    public String toString() {
        return "(" + a + ", " + b + ')';
    }

    /**
     * Returns the starting endpoint.
     *
     * @return endpoint {@code a}
     */
    public Vec2D getA() {
        return a;
    }

    /**
     * Returns the ending endpoint.
     *
     * @return endpoint {@code b}
     */
    public Vec2D getB() {
        return b;
    }
}