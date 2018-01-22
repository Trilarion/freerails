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
 * An immutable point.
 */
public final class Point2D implements Serializable, Comparable<Point2D> {

    private static final long serialVersionUID = -3053020239886388576L;
    public final int x, y;

    public Point2D() {
        x = 0;
        y = 0;
    }

    /**
     * @param x
     * @param y
     */
    public Point2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point2D(Point2D p) {
        x = p.x;
        y = p.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Point2D)) return false;

        final Point2D point = (Point2D) obj;

        if (x != point.x) return false;
        return y == point.y;
    }

    /**
     * @return
     */
    public Point toPoint() {
        return new Point(x, y);
    }

    @Override
    public int hashCode() {
        return x * 29 + y;
    }

    @Override
    public String toString() {
        return "Point2D{" + x + ", " + y + '}';
    }

    public int compareTo(Point2D o) {
        if (o.y != y) return y - o.y;
        else return x - o.x;
    }
}
