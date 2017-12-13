/*
 * Copyright (C) Robert Tuck
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
package org.railz.controller;

import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.railz.world.common.*;
import org.railz.world.top.*;
import org.railz.world.track.*;
import org.railz.world.train.*;

public class TrainPathFinder {
    private ReadOnlyWorld world;

    private LinkedList bestPath;
    private int bestCost;

    private LinkedList currentPath;
    /**
     * track layout at the starting tile
     */
    private byte startTrackLayout;
    /**
     * current direction from the start
     */
    private byte startDirection;

    /**
     * A HashSet of Point instances which describe the track tiles which have
     * already been explored.
     */
    private HashSet exploredTiles;

    private void dumpState(LinkedList l) {
	Iterator i = l.iterator();
	while (i.hasNext()) { 
	    System.out.println(((PathElement) i.next()).toString());
	}
    }

    public TrainPathFinder(ReadOnlyWorld w) {
	world = w;
	exploredTiles = new HashSet();
    }

    private void nextElement(Point currentPosition) {
	PathElement pe = (PathElement) currentPath.getLast();
	while (!pe.nextDirection()) {
	    exploredTiles.add(new Point(pe.x, pe.y));
	    currentPath.removeLast();
	    if (currentPath.size() == 1)
		break;
	    pe = (PathElement) currentPath.getLast();
	}
	currentPosition.x = pe.x;
	currentPosition.y = pe.y;
    }

    private void fixUpPathEnds(LinkedList p, Point newStart, Point newEnd) {
	IntLine oldStartSegment = (IntLine) p.getFirst();
	IntLine oldEndSegment = (IntLine) p.getLast();
	Point oldStart = new Point(oldStartSegment.x1, oldStartSegment.y1);
	Point oldEnd = new Point(oldEndSegment.x2, oldEndSegment.y2);
	IntLine newStartSegment = new IntLine(newStart, oldStart);
	IntLine newEndSegment = new IntLine(oldEnd, newEnd);
	/* If direction of old & new lines is 180deg out, then substitute
	 * truncated path */
	if (newStartSegment.getDirection() ==
		CompassPoints.invert(oldStartSegment.getDirection())) {
	    p.removeFirst();
	    p.addFirst(new IntLine(newStart.x, newStart.y, oldStartSegment.x2,
			    oldStartSegment.y2));
	} else {
	    p.addFirst(newStartSegment);
	}
	if (newEndSegment.getDirection() ==
		CompassPoints.invert(oldEndSegment.getDirection())) {
	    p.removeLast();
	    p.addLast(new IntLine(oldEndSegment.x1, oldEndSegment.y1,
			newEnd.x, newEnd.y));
	} else {
	    p.addLast(newEndSegment);
	}
    }

    /**
     * @param start start location measured in Deltas from map origin.
     * @param dest end location measured in Deltas from map origin.
     * TODO calculate cost from terrain and track layout.
     * TODO improve efficiency of this algorithm (discard paths when the
     * possible minimum is longer than current best, compare current best
     * against theoretical minimum and accept non-optimal solutions if they
     * are within a certain %age. Also it may be more efficient to scan from
     * 45deg A/C from target, rather than 180..)
     * @return the found path, or null if no path could be found.
     */
    public TrainPath findPath(Point start, Point dest) {
	Point target = new Point(dest);
	TrackTile.deltasToTileCoords(target);
	startDirection = CompassPoints.NORTH;
	startTrackLayout = world.getTile(start.x / TrackTile.DELTAS_PER_TILE,
		start.y / TrackTile.DELTAS_PER_TILE).getTrackConfiguration();
	currentPath = new LinkedList();
	bestPath = new LinkedList();
	bestCost = Integer.MAX_VALUE;
	Point startCentre = new Point (start);
	TrackTile.deltasToTileCoords(startCentre);
	
	/* "Stupidity" filter... */
	if (startCentre.equals(target))
	    return new TrainPath(new Point[]{new Point(start.x, start.y),
		    TrackTile.tileCoordsToDeltas(startCentre),
		    new Point(dest.x, dest.y)});

	startCentre = TrackTile.tileCoordsToDeltas(startCentre);
	do {
	    startDirection = CompassPoints.rotateClockwise(startDirection);
	    if ((startDirection & world.getTile(startCentre.x /
			TrackTile.DELTAS_PER_TILE, startCentre.y /
			TrackTile.DELTAS_PER_TILE).getTrackConfiguration()) ==
			0)
		    continue;

	    currentPath.clear();
			
	    currentPath.add(new PathElement
		    (startCentre.x / TrackTile.DELTAS_PER_TILE,
		    startCentre.y / TrackTile.DELTAS_PER_TILE,
		    startDirection, startTrackLayout, 1));
	    PathElement currentElement = (PathElement) currentPath.getFirst();
	    Point currentPosition = new Point(currentElement.x,
		    currentElement.y);
	    Point oldPosition = new Point();
	    exploredTiles.clear();
	    exploredTiles.add(currentPosition);
	    do {
		/* move to next tile along current direction */
		oldPosition.x = currentPosition.x;
		oldPosition.y = currentPosition.y;
		currentElement = (PathElement) currentPath.getLast();
		advanceOneTile(currentPosition, currentElement.direction);

		/* is this tile already explored ? */
		if (exploredTiles.contains(currentPosition)) {
		    /* go back to old tile */
		    currentPosition.x = oldPosition.x;
		    currentPosition.y = oldPosition.y;
		    nextElement(currentPosition);
		    continue;
		}

		/* have we doubled back on ourselves ? */
		Iterator i = currentPath.iterator();
		boolean flag = false;
		do {
		    PathElement p = (PathElement) i.next();
		    if (currentPosition.x == p.x &&
			    currentPosition.y == p.y) {
			currentPosition.x = oldPosition.x;
			currentPosition.y = oldPosition.y;
			flag = true;
			break;
		    }
		} while (i.hasNext());
		if (flag) {
		    nextElement(currentPosition);
		    continue;
		}

		/* add the current position to the current path */
		TrackTile tt = world.getTile(currentPosition.x,
			currentPosition.y).getTrackTile();
		if (tt == null) {
		    nextElement(currentPosition);
		    continue;
		}
		byte trackLayout = tt.getTrackConfiguration();
		byte initialDirection =
		    CompassPoints.invert(currentElement.direction);

		currentElement = new PathElement(currentPosition.x,
			currentPosition.y, initialDirection, trackLayout, 1);
		currentPath.add(currentElement);

		/* have we reached the target ? */
		if (currentPosition.equals(target)) {
		    i = currentPath.iterator();
		    int cost = 0;
		    do {
			cost += ((PathElement) i.next()).getCost();
		    } while (i.hasNext());
		    if (cost < bestCost) {
			/* this is the new best route */
			bestCost = cost;
			bestPath.clear();
		       	i = currentPath.iterator();
			do {
			    bestPath.add(new PathElement((PathElement)
					i.next()));
		       	} while (i.hasNext());
		    }
		}
		nextElement(currentPosition);
	    } while (currentPath.size() > 1);
	} while (startDirection != CompassPoints.NORTH);
	/* all possible paths have been explored */
	if (bestPath.isEmpty())
	    return null;

	/* convert the ArrayList to a TrainPath. bestPath always contains the
	 * target tile and the start tile */
	Point oldp = new Point();
	Point newp = new Point();
	byte oldDirection;
	PathElement p = (PathElement) bestPath.removeFirst();
	LinkedList intLines = new LinkedList();
	boolean firstP = true;
	while (!bestPath.isEmpty()) {
	    oldp.x = p.x;
	    oldp.y = p.y;
	    oldDirection = p.direction;
	    do {
		p = (PathElement) bestPath.removeFirst();
		newp.x = p.x;
		newp.y = p.y;
	    } while (p.direction == oldDirection);
	    if (firstP) {
		oldp.x = startCentre.x;
		oldp.y = startCentre.y;
		firstP = false;
	    } else {
		oldp = TrackTile.tileCoordsToDeltas(oldp);
	    }
	    newp = TrackTile.tileCoordsToDeltas(newp);
	    IntLine l = new IntLine(oldp.x, oldp.y, newp.x, newp.y);
	    intLines.add(l);
	}
	fixUpPathEnds(intLines, start, dest);
	TrainPath retVal = new TrainPath((IntLine[]) intLines.toArray(new
		    IntLine[intLines.size()]));
	return retVal;
    }

    private static class PathElement {
	public int x;
	public int y;
	/**
	 * Current direction to advance from this map location
	 */
	public  byte direction;
	private byte initialDirection;
	/**
	 * copy of the track layout at this point
	 */
	private byte trackLayout;

	private int cost;

	public String toString() {
	    return "x = " + x + ", y = " + y + ", direction = " +
		CompassPoints.toString(direction) + ", initDir = " +
		CompassPoints.toString(initialDirection) + ", trackLayout = "
		+ CompassPoints.toString(trackLayout) + ", cost = " + cost;
	}

	public PathElement(PathElement p) {
	    x = p.x;
	    y = p.y;
	    direction = p.direction;
	    initialDirection = p.initialDirection;
	    trackLayout = p.trackLayout;
	    cost = p.cost;
	}

	/**
	 * @param direction direction to explore
	 * @param cost cost factor to traverse this tile
	 * @param x x coord of tile in tiles
	 * @param y y coord of tile in tiles
	 * @param trackLayout CompassPoints mask representing layout of track
	 * at this point
	 */
	public PathElement(int x, int y, byte direction, byte trackLayout, int
		cost) {
	    this.x = x;
	    this.y = y;
	    this.cost = cost;
	    this.trackLayout = trackLayout;
	    initialDirection = direction;
	    this.direction = direction;
	}

	/**
	 * @return true if there is another direction to explore
	 */
	public boolean nextDirection() {
	    do {
		direction = CompassPoints.rotateClockwise(direction);
		if (direction == initialDirection)
		    return false;
	    } while ((direction & trackLayout) == 0);
	    return true;
	}

	/** TODO change this method to take into account the terrain */
	public int getCost() {
	    return cost * CompassPoints.getLength(direction);
	}
    }

    private static void advanceOneTile(Point p, byte direction) {
	switch(direction) {
	    case CompassPoints.NORTH:
		p.y--;
		return;
	    case CompassPoints.NORTHEAST:
		p.y--;
	    case CompassPoints.EAST:
		p.x++;
		return;
	    case CompassPoints.SOUTHEAST:
		p.y++;
		p.x++;
		return;
	    case CompassPoints.SOUTH:
		p.y++;
		return;
	    case CompassPoints.SOUTHWEST:
		p.x--;
		p.y++;
		return;
	    case CompassPoints.WEST:
		p.x--;
		return;
	    case CompassPoints.NORTHWEST:
		p.x--;
		p.y--;
		return;
	    default:
		throw new IllegalArgumentException();
	}
    }
}
