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

/**
 * Immutable vector with two double-precision coordinates and arithmetic
 * convenience functions.
 *
 * @param x the horizontal coordinate
 * @param y the vertical coordinate
 */
public record Vector2DDouble(double x, double y) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Static zero vector.
     */
    public static final Vector2DDouble ZERO = new Vector2DDouble(0.0, 0.0);

    /**
     * Creates a copy of another vector.
     *
     * @param vector vector to copy
     * @throws NullPointerException if {@code vector} is null
     */
    public Vector2DDouble(Vector2DDouble vector) {
        this(vector.x, vector.y);
    }

    /**
     * Returns the Euclidean length of this vector.
     *
     * @return the Euclidean length of this vector
     */
    public double norm() {
        return Math.hypot(x, y);
    }

    /**
     * Adds two vectors coordinate by coordinate.
     *
     * @param a first vector
     * @param b second vector
     * @return the sum
     */
    public static Vector2DDouble add(Vector2DDouble a, Vector2DDouble b) {
        return new Vector2DDouble(a.x + b.x, a.y + b.y);
    }

    /**
     * Subtracts the second vector from the first coordinate by coordinate.
     *
     * @param a vector to subtract from
     * @param b vector to subtract
     * @return the difference
     */
    public static Vector2DDouble subtract(Vector2DDouble a, Vector2DDouble b) {
        return new Vector2DDouble(a.x - b.x, a.y - b.y);
    }

    /**
     * Subtracts a scalar from both coordinates.
     *
     * @param a vector to subtract from
     * @param b scalar to subtract from each coordinate
     * @return the resulting vector
     */
    public static Vector2DDouble subtract(Vector2DDouble a, double b) {
        return new Vector2DDouble(a.x - b, a.y - b);
    }

    /**
     * Multiplies two vectors coordinate by coordinate.
     *
     * @param a first vector
     * @param b second vector
     * @return the pointwise product
     */
    public static Vector2DDouble multiply(Vector2DDouble a, Vector2DDouble b) {
        return new Vector2DDouble(a.x * b.x, a.y * b.y);
    }

    /**
     * Multiplies both coordinates by a scalar.
     *
     * @param a vector to multiply
     * @param b scalar multiplier
     * @return the resulting vector
     */
    public static Vector2DDouble multiply(Vector2DDouble a, double b) {
        return new Vector2DDouble(a.x * b, a.y * b);
    }

    /**
     * Divides both coordinates by a scalar using Java floating-point
     * arithmetic.
     *
     * @param a vector to divide
     * @param b divisor
     * @return the resulting vector
     */
    public static Vector2DDouble divide(Vector2DDouble a, double b) {
        return new Vector2DDouble(a.x / b, a.y / b);
    }

    /**
     * Pointwise maximum.
     *
     * @param a first vector
     * @param b second vector
     * @return the vector containing the greater coordinate from each input
     */
    public static Vector2DDouble max(Vector2DDouble a, Vector2DDouble b) {
        return new Vector2DDouble(Math.max(a.x, b.x), Math.max(a.y, b.y));
    }

    /**
     * Pointwise minimum.
     *
     * @param a first vector
     * @param b second vector
     * @return the vector containing the lesser coordinate from each input
     */
    public static Vector2DDouble min(Vector2DDouble a, Vector2DDouble b) {
        return new Vector2DDouble(Math.min(a.x, b.x), Math.min(a.y, b.y));
    }

    /**
     * Tests whether both coordinates are strictly less than those of another
     * vector.
     *
     * @param other vector to compare with
     * @return {@code true} if both coordinates are less than the corresponding
     * coordinates in {@code other}
     */
    public boolean below(Vector2DDouble other) {
        return x < other.x && y < other.y;
    }

    /**
     * Tests whether both coordinates are less than or equal to those of
     * another vector.
     *
     * @param other vector to compare with
     * @return {@code true} if both coordinates are less than or equal to the
     * corresponding coordinates in {@code other}
     */
    public boolean belowOrEqual(Vector2DDouble other) {
        return x <= other.x && y <= other.y;
    }

    /**
     * Tests whether both coordinates are strictly greater than those of
     * another vector.
     *
     * @param other vector to compare with
     * @return {@code true} if both coordinates are greater than the
     * corresponding coordinates in {@code other}
     */
    public boolean above(Vector2DDouble other) {
        return x > other.x && y > other.y;
    }

    /**
     * Tests whether both coordinates are greater than or equal to those of
     * another vector.
     *
     * @param other vector to compare with
     * @return {@code true} if both coordinates are greater than or equal to
     * the corresponding coordinates in {@code other}
     */
    public boolean aboveOrEqual(Vector2DDouble other) {
        return x >= other.x && y >= other.y;
    }

    /**
     * Returns the coordinates in {@code (x, y)} form.
     *
     * @return this vector's textual representation
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
