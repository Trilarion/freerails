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

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * A directed straight-line segment between two two-dimensional integer
 * points. Coordinate units are arbitrary.
 *
 * @param a starting endpoint
 * @param b ending endpoint
 */
public record Segment(Vector2D a, Vector2D b) implements Serializable {

    @Serial
    private static final long serialVersionUID = 3257853198755705393L;

    /**
     * Creates a segment from endpoint {@code a} to endpoint {@code b}.
     *
     * @param a starting endpoint
     * @param b ending endpoint
     * @throws NullPointerException if either endpoint is null
     */
    public Segment(Vector2D a, Vector2D b) {
        this.a = Objects.requireNonNull(a, "a");
        this.b = Objects.requireNonNull(b, "b");
    }

    /**
     * Returns the Euclidean distance between the endpoints.
     *
     * @return the segment length
     */
    public double getLength() {
        return Math.hypot((double) a.x() - b.x(), (double) a.y() - b.y());
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

}