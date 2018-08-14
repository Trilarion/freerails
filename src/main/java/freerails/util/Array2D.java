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

import java.util.Arrays;

// TODO what about double, boolean, objects
/**
 * 2D Int array with get and set methods.
 */
public class Array2D {

    private final int width;
    private final int height;
    private final int[] values;

    /**
     *
     * @param width
     * @param height
     */
    public Array2D(int width, int height) {
        if (width < 0 | height < 0) {
            throw new RuntimeException("Width and height must be non-negative.");
        }
        this.width = width;
        this.height = height;
        values = new int[width * height];
    }

    /**
     *
     * @param x
     * @param y
     * @param v
     */
    public void set(int x, int y, int v) {
        values[index(x, y)] = v;
    }

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public int get(int x, int y) {
        return values[index(x, y)];
    }

    /**
     *
     * Note: Currently, internal order is width first.
     *
     * @param x
     * @param y
     * @return
     */
    private int index(int x, int y) {
        if (x < 0 | x >= width | y < 0 | y >= height) {
            throw new RuntimeException("Index out of range.");
        }
        return x + y * width;
    }

    /**
     *
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     *
     * @return
     */
    public int getHeight() {
        return height;
    }

    /**
     *
     * @return
     */
    public Vec2D getSize() {
        return new Vec2D(width, height);
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Array2D) {
            Array2D other = (Array2D) obj;
            return width == other.width && height == other.height && Arrays.equals(values, other.values);
        }
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public final int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }
}
