/*
 * Copyright (C) 2004 Robert Tuck
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

package org.railz.world.train;

import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.HashMap;

import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.track.*;

/**
 * Describes a continuous sequence of straight-line segments.
 * @author rtuck99@users.berlios.de
 */
public final class TrainPath implements FreerailsSerializable {
    /**
     * A linked list of type IntLine
     */
    private LinkedList segments = new LinkedList();

    /**
     * This is the intended length of the path that we attempt to maintain
     * when moving the head or tail.
     * we store the path length in order to avoid changes in total length
     * caused by rounding errors when summing calculated diagonals
     */
    private double length;

    /**
     * This is the length of the path.
     */
    private PathLength actualLength;

    public TrainPath (Point[] points) {
	actualLength = new PathLength();
	for (int i = 1; i < points.length; i++) {
	    if (points[i - 1].equals(points[i]) && i < points.length - 1)
		continue;
	    IntLine l = new IntLine(points[i - 1], points[i]);
	    segments.add(l);
	    actualLength.add(l.getLength());
	}
	if (segments.size() < 1) {
	    throw new IllegalArgumentException();
	}
	this.length = actualLength.getLength();
    }

    public TrainPath (IntLine[] lines) {
	actualLength = new PathLength();
	for (int i = 0; i < lines.length; i++) {
	    if (lines[i].getLength().getLength() == 0.0 &&
		    i < lines.length - 1)
		continue;
	    segments.add(lines[i]);
	    actualLength.add(lines[i].getLength());
	}
	if (segments.size() < 1)
	    throw new IllegalArgumentException();

	length = actualLength.getLength();
    }

    /**
     * Reverses the head and tail of the TrainPath
     */
    public void reverse() {
	LinkedList newSegments = new LinkedList();
	while (! segments.isEmpty()) {
	    IntLine il = (IntLine) segments.removeFirst();
	    newSegments.addFirst(new IntLine(il.x2, il.y2, il.x1, il.y1));
	}
	segments = newSegments;
    }

    public TrainPath(TrainPath p) {
	length = p.length;
	actualLength = new PathLength(p.actualLength);
	ListIterator i = p.segments.listIterator(0);
	while (i.hasNext()) {
	    segments.add(new IntLine((IntLine) i.next()));
	}
    }

    /**
     * @return the intended length of the train path
     */
    public int getLength() {
	return (int) length;
    }

    /**
     * @return the actual length of the train
     */
    public PathLength getActualLength() {
	return actualLength;
    }

    public void getHead(Point p) {
	IntLine l = (IntLine) segments.getFirst();
	p.x = l.x1;
	p.y = l.y1;
    }

    public void getTail(Point p) {
	IntLine l = (IntLine) segments.getLast();
	p.x = l.x2;
	p.y = l.y2;
    }

    public IntLine getFirstSegment() {
	return (IntLine) segments.getFirst();
    }

    public IntLine getLastSegment() {
	return (IntLine) segments.getLast();
    }

    /**
     * Append the specified path to the tail. Actual total lengths is
     * preserved and intended length is calculated.
     */
    public void append(TrainPath tp) {
	IntLine tpHead = (IntLine) tp.segments.getFirst();
	IntLine tail = (IntLine) segments.getLast();
	if (tpHead.x1 != tail.x2 ||
		tpHead.y1 != tail.y2)
	    throw new IllegalArgumentException();
	int headDir = tpHead.getDirection();
	if (headDir == 0 || headDir == tail.getDirection()) {
	    tail.append(tpHead);
	} else {
	    segments.add(tpHead);
	}
	ListIterator i = tp.segments.listIterator(1);
	while (i.hasNext()) {
	    segments.add(i.next());
	}
	actualLength.add(tp.actualLength);
	length = actualLength.getLength();
    }

    /**
     * Truncate this TrainPath by removing a portion from the tail.
     * @param newLength the new length of the path
     * @return the portion which was removed
     */
    public TrainPath truncateTail (double newLength) {
	final Point p = new Point ();
	PathLength l = new PathLength();
	if ((int) actualLength.getLength() <= newLength) {
	    assert newLength - (int) actualLength.getLength() < 3;
	    getTail(p);
	    return new TrainPath(new IntLine[]{new IntLine(p.x, p.y, p.x, p.y)});
	}
	ListIterator i = segments.listIterator(0);
	IntLine line = null;
	LinkedList removedSegments = new LinkedList(); 
	while (i.hasNext() && (int) l.getLength() <= newLength) {
	    line = (IntLine) i.next();
	    l.add(line.getLength());
	}
	while (i.hasNext()) {
	    removedSegments.add(i.next());
	    i.remove();
	}
	if ((int) l.getLength() > newLength) {
	    int oldX2 = line.x2;
	    int oldY2 = line.y2;
	    PathLength segLength = new PathLength(line.getLength());
	    // System.out.println("stub length = " + l.getLength());
	    l.subtract(segLength);
	    segLength.setLength(newLength - l.getLength());
	    // System.out.println("set segLength to " + (newLength -
	//		l.getLength()) + ", is actually=" +
	//	    segLength.getLength());
	    l.add(segLength);
	    line.setLength(segLength);
	    removedSegments.addFirst(new IntLine(line.x2, line.y2, oldX2,
		       oldY2));
	    // System.out.println("new actualLength = " + l.getLength() +
	//	    ", intended length=" + newLength + ", old length=" +
	//	    length);
	}
	length = newLength;
	actualLength = l;
	if (removedSegments.isEmpty()) {
	    getTail(p);
	    removedSegments.add(new IntLine(p.x, p.y, p.x, p.y));
	}
	return new TrainPath((IntLine[]) removedSegments.toArray(new
		    IntLine[removedSegments.size()]));
    }
    
    /**
     * Prepend the specified path to this TrainPath. The tail of
     * additionalPath is equal to the head of this path
     */
    public void prepend(TrainPath additionalPath) {
	PathLength l = additionalPath.actualLength;
	IntLine tail = (IntLine) additionalPath.segments.getLast();
	IntLine head = getFirstSegment();
	int tailDir = tail.getDirection();
	if (tailDir == 0 || (tailDir == head.getDirection())) {
	    head.prepend(tail);
	} else {
	    segments.addFirst(tail);
	}
	/* add the rest of the path */
	ListIterator i =
	    additionalPath.segments.listIterator(additionalPath.segments.size()
		    - 1);
	while (i.hasPrevious()) {
	    segments.addFirst(i.previous());
	}
	actualLength.add(l);
	length = actualLength.getLength();
    }

    /**
     * Moves the head to the corresponding location, and advances the tail by
     * the same amount, keeping the length the same.
     * @param additionalPath the path to add. The tail of additionalPath is
     * equal to the head of this path.
     * @return the portion of the tail removed to maintain constant length
     */
    public TrainPath moveHeadTo(TrainPath additionalPath) {
	// System.out.println(".");
	double l = length;
	prepend(additionalPath);
	TrainPath tp = truncateTail(l);
	double d =  Math.abs(actualLength.getLength() - length);
	assert d < 3;
	return tp;
    }
    
    /**
     * Moves the tail to the corresponding location, and reverses the head by
     * the same amount, keeping the length the same.
     * @param additionalPath the path to add. The head of additionalPath is
     * equal to the tail of this path.
     * @return the position of the head prior to the path being advanced
     */
    public Point moveTailTo(TrainPath additionalPath) {
	ListIterator i = additionalPath.segments.listIterator(0);
	IntLine additionalHead = (IntLine) i.next();
	IntLine tail = getLastSegment();
	Point oldHeadPoint = new Point (additionalHead.x1, additionalHead.y1);
	if (tail.getDirection() == additionalHead.getDirection()) {
	    tail.append(additionalHead);
	} else {
	    segments.add(additionalHead);
	}
	actualLength.add(additionalHead.getLength());
	/* add the rest of the path */
	while (i.hasNext()) {
	    additionalHead = (IntLine) i.next();
	    segments.add(additionalHead);
	    actualLength.add(additionalHead.getLength());
	}
	IntLine head = null;
	while (actualLength.getLength() > length) {
	    head = (IntLine) segments.removeFirst();
	    actualLength.subtract(head.getLength());
	};
	if (actualLength.getLength() < length && head != null) {
	    PathLength headLength = new PathLength(head.getLength());
	    headLength.setLength(length - actualLength.getLength());
	    head.setLengthFromTail(headLength);
	    segments.addFirst(head);
	}
	return oldHeadPoint;
    }

    public boolean equals(Object o) {
	if (o == null || !(o instanceof TrainPath))
	    return false;

	TrainPath tp = (TrainPath) o;
	return (tp.segments.equals(segments));
    }

    public int hashCode() {
	return segments.hashCode();
    }

    /**
     * Determines the map coordinates and directions in which the TrainPath
     * traverses.
     * @param mapCoords an empty HashMap into which the map coordinates are
     * to be put as keys, and the directions as objects.
     */
    public void getMapCoordsAndDirections(HashMap mapCoords) {
	byte direction;
	ListIterator i = segments.listIterator(0);
	Point map = new Point();
	final Point oldMapPoint = new Point();
	while (i.hasNext()) {
	    IntLine l = (IntLine) i.next();
	    int dx = l.x2 - l.x1;
	    int dy = l.y2 - l.y1;
	    map.x = l.x1 / TrackTile.DELTAS_PER_TILE;
	    map.y = l.y1 / TrackTile.DELTAS_PER_TILE;
	    do {
		oldMapPoint.x = map.x;
		oldMapPoint.y = map.y;
		if (dx < 0) {
		    map.x--;
		    direction = (byte) (CompassPoints.NORTHWEST |
			CompassPoints.WEST |
			CompassPoints.SOUTHWEST);
		} else if (dx > 0) {
		    map.x++;
		    direction = (byte) (CompassPoints.NORTHEAST |
			CompassPoints.EAST |
			CompassPoints.SOUTHEAST);
		} else {
		    direction = (byte) (CompassPoints.NORTH |
			CompassPoints.SOUTH);
		}
		if (dy < 0) {
		    map.y--;
		    direction &= (byte) (CompassPoints.NORTHWEST |
			    CompassPoints.NORTH | CompassPoints.NORTHEAST);
		} else if (dy > 0) {
		    map.y++;
		    direction &= (byte) (CompassPoints.SOUTHWEST |
			    CompassPoints.SOUTH |
			    CompassPoints.SOUTHEAST);
		} else {
		    direction &= (byte) (CompassPoints.EAST |
			    CompassPoints.WEST);
		}
		Byte oldDirection = (Byte) mapCoords.get(map);
		if (oldDirection != null)
		    direction |= oldDirection.byteValue();
		mapCoords.put(new Point(oldMapPoint), new Byte(direction));
	    } while (oldMapPoint.x != l.x2 / TrackTile.DELTAS_PER_TILE ||
		    oldMapPoint.y != l.y2 / TrackTile.DELTAS_PER_TILE);
	}
    }

    /**
     * @return a CompassPoint indicating the direction at the specified locus
     * (traversing from head to tail)
     * @param p used to return the absolute position of the locus measured in
     * Deltas from the map origin.
     * @param distance distance of the locus from the head of the TrainPath
     */
    public byte getDirectionAtDistance(Point p, int distance) {
	PathLength d = new PathLength();
	ListIterator li = segments.listIterator(0);
	while (li.hasNext()) {
	    IntLine l = (IntLine) li.next();
	    d.add(l.getLength());
	    if (d.getLength() >= distance) {
		PathLength pl = new PathLength(l.getLength());
		pl.setLength(l.getLength().getLength() - (d.getLength() -
			    distance));
		IntLine il = new IntLine(l);
		il.setLength(pl);
		// locus is within this segment.
		p.x = il.x2;
		p.y = il.y2;
		return l.getDirection();
	    }
	}
	// distance was bigger than length of the TrainPath
	throw new IllegalArgumentException("distance " + distance + " > " +
		this);
    }

    public String toString() {
	String s = "";
	Iterator i = segments.iterator();
	while (i.hasNext()) {
	    if (s.length() > 0)
		s += ", ";
	    s += ((IntLine) i.next()).toString();
	}
	s+= ", length = " + length;
	s += ", actual = " + actualLength;
	return s;
    }
}
