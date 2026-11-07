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

import java.util.Arrays;

/**
 * A fixed-size, row-major two-dimensional array of double values.
 *
 * <p>Coordinates start at {@code (0, 0)}; the first coordinate is the column
 * and the second is the row.
 */
public class DoubleArray2D {

    private final int width;
    private final int height;
    private final double[] values;

    /**
     * Creates an array with the given dimensions. All values initially equal
     * zero.
     *
     * @param width number of columns
     * @param height number of rows
     * @throws IllegalArgumentException if either dimension is negative or the
     *                                  total number of cells overflows
     */
    public DoubleArray2D(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Width and height must be non-negative.");
        }
        this.width = width;
        this.height = height;
        try {
            values = new double[Math.multiplyExact(width, height)];
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Array dimensions are too large.", e);
        }
    }

    /**
     * Stores a value at the given coordinates.
     *
     * @param x column, from zero to {@code width - 1}
     * @param y row, from zero to {@code height - 1}
     * @param value value to store
     * @throws IndexOutOfBoundsException if either coordinate is outside the array
     */
    public void set(int x, int y, double value) {
        values[index(x, y)] = value;
    }

    /**
     * Returns the value at the given coordinates.
     *
     * @param x column, from zero to {@code width - 1}
     * @param y row, from zero to {@code height - 1}
     * @return the value at {@code (x, y)}
     * @throws IndexOutOfBoundsException if either coordinate is outside the array
     */
    public double get(int x, int y) {
        return values[index(x, y)];
    }

    /**
     * Converts a coordinate pair to its row-major array index.
     *
     * @param x column
     * @param y row
     * @return the corresponding index in {@link #values}
     * @throws IndexOutOfBoundsException if either coordinate is outside the array
     */
    private int index(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("Coordinates are outside the array.");
        }
        return x + y * width;
    }

    /**
     * Returns the number of columns.
     *
     * @return the array width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the number of rows.
     *
     * @return the array height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the dimensions as a point whose {@code x} is the width and
     * {@code y} is the height.
     *
     * @return the array dimensions
     */
    public IntPoint2D getSize() {
        return new IntPoint2D(width, height);
    }

    /**
     * Tests whether another object has the same dimensions and cell values.
     *
     * @param obj object to compare
     * @return {@code true} if {@code obj} is an equal {@code DoubleArray2D}
     */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DoubleArray2D other) {
            return width == other.width && height == other.height && Arrays.equals(values, other.values);
        }
        return false;
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     *
     * @return this array's hash code
     */
    @Override
    public final int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }
}
