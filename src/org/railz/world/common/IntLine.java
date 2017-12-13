/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.world.common;

import java.awt.Point;

/**
 * This class defines a straight line between two points. Units are arbitrary.
 */
public class IntLine implements FreerailsSerializable {
    /**
     * x coordinate of head
     */
    public int x1;
    /**
     * x coordinate of tail
     */
    public int x2;
    /**
     * y coordinate of head
     */
    public int y1;
    /**
     * y coordinate of tail
     */
    public int y2;

    private PathLength length;

    /**
     * return a CompassPoint defining the direction from head to tail
     */
    public byte getDirection() {
	int dx = x2 - x1;
	int dy = y2 - y1;
	if (dx > 0) {
	    return (dy < 0) ? CompassPoints.NORTHEAST :
		((dy > 0) ? CompassPoints.SOUTHEAST : CompassPoints.EAST);
	} else if (dx < 0) {
	    return (dy < 0) ? CompassPoints.NORTHWEST
		: ((dy > 0) ? CompassPoints.SOUTHWEST :
			CompassPoints.WEST);
	}
	return (dy < 0) ? CompassPoints.NORTH :
	    ((dy > 0) ? CompassPoints.SOUTH : 0x0);
    }

    /**
     * @return the length of the line
     */
    public PathLength getLength() {
	return length;
    }

    public IntLine(IntLine l) {
	x1 = l.x1;
	y1 = l.y1;
	x2 = l.x2;
	y2 = l.y2;
	length = new PathLength(l.x1, l.y1, l.x2, l.y2);
    }

    public IntLine(Point p1, Point p2) {
	this(p1.x, p1.y, p2.x, p2.y);
    }

    /**
     * @param xx1 x of the first point
     * @param yy1 y of the first point
     * @param xx2 x of the second point
     * @param yy2 y of the second point
     */
    public IntLine(int xx1, int yy1, int xx2, int yy2) {
        x1 = xx1;
        y1 = yy1;
        x2 = xx2;
        y2 = yy2;
	length = new PathLength(x1, y1, x2, y2);
    }

    /**
     * Default constructor - defines a dot at 0,0
     */
    public IntLine() {
	x1 = y1 = x2 = y2 = 0;
	length = new PathLength();
    }

    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }

        if (o == this) {
            return true;
        }

        if (o instanceof IntLine) {
            IntLine line = (IntLine)o;

            if (line.x1 == this.x1 && line.x2 == this.x2 && line.y1 == this.y1 &&
                    line.y2 == this.y2) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String toString() {
        return "(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")";
    }

    /**
     * shorten so that the tail is the specified distance from the head by
     * moving the head.
     */
    public void setLengthFromTail(PathLength length) {
	int xx = x1;
	int yy = y1;
	x1 = x2;
	y1 = y2;
	x2 = xx;
	y2 = yy;
	setLength(length);
	xx = x1;
	yy = y1;
	x1 = x2;
	y1 = y2;
	x2 = xx;
	y2 = yy;
    }

    /**
     * shorten so that the tail is the specified distance from the head by
     * moving the tail
     */
    public void setLength(PathLength length) {
	int delta;
	this.length = new PathLength(length);
	switch (getDirection()) {
	    case CompassPoints.NORTH:
		y2 = y1 - length.straightLength;
		break;
	    case CompassPoints.NORTHEAST:
		x2 = x1 + length.diagLength;
		y2 = y1 - length.diagLength;
		break;
	    case CompassPoints.EAST:
		x2 = x1 + length.straightLength;
		break;
	    case CompassPoints.SOUTHEAST:
		x2 = x1 + length.diagLength;
		y2 = y1 + length.diagLength;
		break;
	    case CompassPoints.SOUTH:
		y2 = y1 + length.straightLength;
		break;
	    case CompassPoints.SOUTHWEST:
		x2 = x1 - length.diagLength;
		y2 = y1 + length.diagLength;
		break;
	    case CompassPoints.WEST:
		x2 = x1 - length.straightLength;
		break;
	    case CompassPoints.NORTHWEST:
		x2 = x1 - length.diagLength;
		y2 = y1 - length.diagLength;
		break;
	}
    }

    public void append(IntLine l) {
	assert (l.getDirection() == getDirection() ||
		(l.x1 == l.x2 && l.y1 == l.y2)) &&
	    (l.x1 == x2 && l.y1 == y2);
	x2 = l.x2;
	y2 = l.y2;
	length.add(l.length);
    }

    public void prepend(IntLine l) {
	if (!((l.getDirection() == getDirection()) ||
		(l.x1 == l.x2 && l.y1 == l.y2)) &&
	    (l.x2 == x1 && l.y2 == y1)) {
		System.out.println("adding " + l + " to " + this);
		assert false;
	}
	x1 = l.x1;
	y1 = l.y1;
	length.add(l.length);
    }
}
