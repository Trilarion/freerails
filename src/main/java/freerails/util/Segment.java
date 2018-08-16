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

import java.io.Serializable;

/**
 * Defines a segment of a straightline by defining two points, the starting and the end point. Units are arbitrary.
 */
public class Segment implements Serializable {

    private static final long serialVersionUID = 3257853198755705393L;
    private static final int MAX_SQUAREROOTS = 64 * 256;
    private static final double squareRoots[];

    static {
        squareRoots = new double[MAX_SQUAREROOTS];
        for (int i = 0; i < MAX_SQUAREROOTS; i++) {
            squareRoots[i] = Math.sqrt(i);
        }
    }

    private final Vec2D a;
    private final Vec2D b;

    public Segment(Vec2D a, Vec2D b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public int hashCode() {
        return a.hashCode() * 29 + b.hashCode();
    }

    /**
     * @return the length of the line
     */
    public double getLength() {
        int sumOfSquares = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
        if (sumOfSquares < MAX_SQUAREROOTS) {
            return squareRoots[sumOfSquares];
        }
        return Math.sqrt(sumOfSquares);
    }

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

    @Override
    public String toString() {
        return "(" + a + ", " + b + ')';
    }

    public Vec2D getA() {
        return a;
    }

    public Vec2D getB() {
        return b;
    }
}