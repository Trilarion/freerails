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

/*
 *
 */
package freerails.util;

import java.awt.*;
import java.io.Serializable;

/**
 * Immutable vector with two integer coordinates and some arithmetic convenience functions.
 *
 * Mostly used for locations (points) or differences of locations (vectors).
 */
public final class Vector2D implements Serializable, Comparable<Vector2D> {

    private static final long serialVersionUID = -3053020239886388576L;
    public static final Vector2D ZERO = new Vector2D(0, 0);
    public final int x;
    public final int y;


    /**
     * @param x
     * @param y
     */
    public Vector2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     *
     */
    public Vector2D() {
        this(0,0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vector2D)) return false;

        final Vector2D other = (Vector2D) obj;

        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return x * 29 + y;
    }

    public double norm() {
        return Math.sqrt(x * x + y * y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public static Vector2D add(Vector2D a, Vector2D b) {
        return new Vector2D(a.x + b.x, a.y + b.y);
    }

    public static Vector2D subtract(Vector2D a, Vector2D b) {
        return new Vector2D(a.x - b.x, a.y - b.y);
    }

    public int compareTo(Vector2D o) {
        if (o.y != y) return y - o.y;
        else return x - o.x;
    }

    public static Vector2D fromPoint(Point p) {
        return new Vector2D(p.x, p.y);
    }

    public static Point toPoint(Vector2D p) {
        return new Point(p.x, p.y);
    }
}
