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
 * Defines a straight line between two points. Units are arbitrary.
 */
public class LineSegment implements Serializable {

    private static final long serialVersionUID = 3257853198755705393L;
    private static final int MAX_SQUAREROOTS = 64 * 256;
    private static final double squareRoots[];

    static {
        squareRoots = new double[MAX_SQUAREROOTS];
        for (int i = 0; i < MAX_SQUAREROOTS; i++) {
            squareRoots[i] = Math.sqrt(i);
        }
    }

    // TODO mutable use getter and setter
    private int x1;
    private int x2;
    private int y1;
    private int y2;

    /**
     * @param x1 x of the first point
     * @param y1 y of the first point
     * @param x2 x of the second point
     * @param y2 y of the second point
     */
    public LineSegment(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * Default constructor - defines a dot at 0,0.
     */
    public LineSegment() {
    }

    @Override
    public int hashCode() {
        int result;
        result = x1;
        result = 29 * result + x2;
        result = 29 * result + y1;
        result = 29 * result + y2;

        return result;
    }

    /**
     * @return the length of the line
     */
    public double getLength() {
        int sumOfSquares = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
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
        if (obj instanceof LineSegment) {
            LineSegment line = (LineSegment) obj;
            return line.x1 == x1 && line.x2 == x2 && line.y1 == y1 && line.y2 == y2;
        }
        return false;
    }

    @Override
    public String toString() {
        return "(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ')';
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }
}