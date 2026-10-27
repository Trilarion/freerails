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

import java.awt.*;
import java.io.Serializable;

// TODO double version of it?
/**
 * Immutable vector with two integer coordinates and arithmetic convenience
 * functions.
 *
 * <p>It is used for both locations and differences between locations.
 */
public final class Vec2D implements Serializable, Comparable<Vec2D> {

    private static final long serialVersionUID = -3053020239886388576L;
    /** Static zero vector. */
    public static final Vec2D ZERO = new Vec2D(0, 0);
    /** The horizontal coordinate. */
    public final int x;
    /** The vertical coordinate. */
    public final int y;


    /**
     * Creates a vector with the given coordinates.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     */
    public Vec2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates the zero vector.
     */
    public Vec2D() {
        this(0,0);
    }

    /**
     * Creates a copy of another vector.
     *
     * @param o vector to copy
     * @throws NullPointerException if {@code o} is null
     */
    public Vec2D(Vec2D o) {
        this(o.x, o.y);
    }

    /**
     * Tests whether another object has the same coordinates.
     *
     * @param obj object to compare
     * @return {@code true} if {@code obj} is a {@code Vec2D} with matching
     * coordinates
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vec2D)) return false;

        final Vec2D other = (Vec2D) obj;

        return x == other.x && y == other.y;
    }

    /**
     * Returns a hash code based on the coordinates.
     *
     * @return this vector's hash code
     */
    @Override
    public int hashCode() {
        return x * 29 + y;
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
     * Returns the coordinates in {@code (x, y)} form.
     *
     * @return this vector's textual representation
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    /**
     * Adds two vectors coordinate by coordinate.
     *
     * @param a first vector
     * @param b second vector
     * @return the sum
     */
    public static Vec2D add(Vec2D a, Vec2D b) {
        return new Vec2D(a.x + b.x, a.y + b.y);
    }

    /**
     * Subtracts the second vector from the first coordinate by coordinate.
     *
     * @param a vector to subtract from
     * @param b vector to subtract
     * @return the difference
     */
    public static Vec2D subtract(Vec2D a, Vec2D b) {
        return new Vec2D(a.x - b.x, a.y - b.y);
    }

    /**
     * Subtracts a scalar from both coordinates.
     *
     * @param a vector to subtract from
     * @param b scalar to subtract from each coordinate
     * @return the resulting vector
     */
    public static Vec2D subtract(Vec2D a, int b) {
        return new Vec2D(a.x - b, a.y - b);
    }

    /**
     * Multiplies two vectors coordinate by coordinate.
     *
     * @param a first vector
     * @param b second vector
     * @return the pointwise product
     */
    public static Vec2D multiply(Vec2D a, Vec2D b) {
        return new Vec2D(a.x * b.x, a.y * b.y);
    }

    /**
     * Multiplies both coordinates by a scalar.
     *
     * @param a vector to multiply
     * @param b scalar multiplier
     * @return the resulting vector
     */
    public static Vec2D multiply(Vec2D a, int b) {
        return new Vec2D(a.x * b, a.y * b);
    }

    /**
     * Divides both coordinates by an integer using Java integer division.
     *
     * @param a vector to divide
     * @param b divisor
     * @return the resulting vector
     * @throws ArithmeticException if {@code b} is zero
     */
    public static Vec2D divide(Vec2D a, int b) {
        return new Vec2D(a.x / b, a.y / b);
    }

    /**
     * Pointwise maximum.
     *
     * @param a first vector
     * @param b second vector
     * @return the vector containing the greater coordinate from each input
     */
    public static Vec2D max(Vec2D a, Vec2D b) {
        return new Vec2D(Math.max(a.x, b.x), Math.max(a.y, b.y));
    }

    /**
     * Pointwise minimum.
     *
     * @param a first vector
     * @param b second vector
     * @return the vector containing the lesser coordinate from each input
     */
    public static Vec2D min(Vec2D a, Vec2D b) {
        return new Vec2D(Math.min(a.x, b.x), Math.min(a.y, b.y));
    }

    /**
     * Compares vectors lexicographically by {@code y}, then by {@code x}.
     *
     * @param o vector to compare with
     * @return a negative integer, zero, or a positive integer as this vector
     * sorts before, equals, or sorts after {@code o}
     * @throws NullPointerException if {@code o} is null
     */
    @Override
    public int compareTo(Vec2D o) {
        int compareY = Integer.compare(y, o.y);
        return compareY != 0 ? compareY : Integer.compare(x, o.x);
    }

    /**
     * Tests whether both coordinates are strictly less than those of another
     * vector.
     *
     * @param o vector to compare with
     * @return {@code true} if both coordinates are less than the corresponding
     * coordinates in {@code o}
     */
    public boolean below(Vec2D o) {
        return x < o.x && y < o.y;
    }

    /**
     * Tests whether both coordinates are less than or equal to those of
     * another vector.
     *
     * @param o vector to compare with
     * @return {@code true} if both coordinates are less than or equal to the
     * corresponding coordinates in {@code o}
     */
    public boolean belowOrEqual(Vec2D o) {
        return x <= o.x && y <= o.y;
    }

    /**
     * Tests whether both coordinates are strictly greater than those of
     * another vector.
     *
     * @param o vector to compare with
     * @return {@code true} if both coordinates are greater than the
     * corresponding coordinates in {@code o}
     */
    public boolean above(Vec2D o) {
        return x > o.x && y > o.y;
    }

    /**
     * Tests whether both coordinates are greater than or equal to those of
     * another vector.
     *
     * @param o vector to compare with
     * @return {@code true} if both coordinates are greater than or equal to
     * the corresponding coordinates in {@code o}
     */
    public boolean aboveOrEqual(Vec2D o) {
        return x >= o.x && y >= o.y;
    }

    /**
     * Converts a vector to an AWT point.
     *
     * @param p vector to convert
     * @return a point with the same coordinates
     */
    public static Point toPoint(Vec2D p) {
        return new Point(p.x, p.y);
    }

    /**
     * Converts a vector to an AWT dimension.
     *
     * @param p vector to convert
     * @return a dimension whose width and height are the vector's
     * {@code x} and {@code y}
     */
    public static Dimension toDimension(Vec2D p) {
        return new Dimension(p.x, p.y);
    }
}
