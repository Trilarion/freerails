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
 * Created on 04-Jul-2005
 *
 */
package freerails.util;

import java.awt.*;
import java.io.Serializable;

/**
 * An immutable point.
 */
@Immutable
public final class ImPoint implements Serializable,
        Comparable<ImPoint> {

    private static final long serialVersionUID = -3053020239886388576L;

    /**
     *
     */
    public final int x,

    /**
     *
     */
    y;

    /**
     *
     */
    public ImPoint() {
        x = 0;
        y = 0;
    }

    /**
     * @param p
     */
    public ImPoint(Point p) {
        x = p.x;
        y = p.y;
    }

    /**
     * @param x
     * @param y
     */
    public ImPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ImPoint))
            return false;

        final ImPoint imPoint = (ImPoint) o;

        if (x != imPoint.x)
            return false;
        return y == imPoint.y;
    }

    /**
     * @return
     */
    public Point toPoint() {
        return new Point(x, y);
    }

    @Override
    public int hashCode() {
        return x * 1000 + y;
    }

    @Override
    public String toString() {
        return "ImPoint{" + x + ", " + y + "}";
    }

    public int compareTo(ImPoint o) {
        if (o.y != y)
            return y - o.y;
        else
            return x - o.x;
    }
}
