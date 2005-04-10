/*
 * Created on 03-Feb-2005
 *
 */
package jfreerails.world.train;

import java.awt.Point;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.IntLine;
import jfreerails.world.common.OneTileMoveVector;
import static jfreerails.world.common.OneTileMoveVector.TILE_DIAMETER;

/**
 * An immutable class that stores a path made up of OneTileMoveVectors.
 * 
 * @author Luke
 * 
 */
public class PathOnTiles implements FreerailsSerializable {

	private static final long serialVersionUID = 3544386994122536753L;

	private final Point start;

	private final OneTileMoveVector[] vectors;

	/**
	 * @throws NullPointerException
	 *             if null == start
	 * @throws NullPointerException
	 *             if null == vectorsList
	 * @throws NullPointerException
	 *             if null == vectorsList.get(i) for any i;
	 */
	public PathOnTiles(Point start, List<OneTileMoveVector> vectorsList) {
		if (null == start)
			throw new NullPointerException();
		vectors = new OneTileMoveVector[vectorsList.size()];
		for (int i = 0; i < vectorsList.size(); i++) {
			if (null == vectorsList.get(i))
				throw new NullPointerException();
			vectors[i] = vectorsList.get(i);
		}
		this.start = new Point(start);
	}

	/**
	 * @throws NullPointerException
	 *             if null == start
	 * @throws NullPointerException
	 *             if null == vectors
	 * @throws NullPointerException
	 *             if null == vectors[i] for any i;
	 */
	public PathOnTiles(Point start, OneTileMoveVector[] vectors) {
		if (null == start)
			throw new NullPointerException();
		for (int i = 0; i < vectors.length; i++) {
			if (null == vectors[i])
				throw new NullPointerException();
		}
		this.start = new Point(start);
		this.vectors = vectors;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof PathOnTiles))
			return false;

		final PathOnTiles pathOnTiles = (PathOnTiles) o;

		if (!start.equals(pathOnTiles.start))
			return false;
		if (!Arrays.equals(vectors, pathOnTiles.vectors))
			return false;

		return true;
	}

	/**
	 * Returns the distance you would travel if you walked the all the way along
	 * the path.
	 */
	public int getLength() {		
		return getDistance(vectors.length);
	}

	public int getDistance(int steps) {
		int distanceSoFar = 0;
		for (int i = 0; i < steps; i++) {
			OneTileMoveVector v = vectors[i];
			distanceSoFar += v.getLength();
		}
		return distanceSoFar;
	}

	/**
	 * Returns the coordinates of the point you would be standing at if you
	 * walked the specified distance along the path from the start point.
	 * 
	 * @throws IllegalArgumentException
	 *             if distance < 0
	 * @throws IllegalArgumentException
	 *             if distance > getLength()
	 */
	public Point getPoint(int distance) {
		if (0 > distance)
			throw new IllegalArgumentException("distance < 0");

		int x = start.x * TILE_DIAMETER + TILE_DIAMETER / 2;
		int y = start.y * TILE_DIAMETER + TILE_DIAMETER / 2;
		int distanceSoFar = 0;
		for (int i = 0; i < vectors.length; i++) {
			OneTileMoveVector v = vectors[i];
			distanceSoFar += v.getLength();
			x += v.deltaX * TILE_DIAMETER;
			y += v.deltaY * TILE_DIAMETER;
			if (distanceSoFar == distance) {
				return new Point(x, y);
			}

			if (distanceSoFar > distance) {
				int excess = distanceSoFar - distance;
				x -= v.deltaX * TILE_DIAMETER * excess / v.getLength();
				y -= v.deltaY * TILE_DIAMETER * excess / v.getLength();
				return new Point(x, y);
			}
		}
		throw new IllegalArgumentException("distance > getLength()");
	}

	public Point getStart() {
		return new Point(start);
	}

	public OneTileMoveVector getStep(int i) {
		return vectors[i];
	}

	/**
	 * Returns the index of the step that takes the distance travelled over the
	 * specified distance.
	 * 
	 * @throws IllegalArgumentException
	 *             if distance < 0
	 * @throws IllegalArgumentException
	 *             if distance > getLength()
	 */
	public int getStepIndex(int distance) {
		if (0 > distance)
			throw new IllegalArgumentException("distance < 0");
		int distanceSoFar = 0;
		for (int i = 0; i < vectors.length; i++) {
			OneTileMoveVector v = vectors[i];
			distanceSoFar += v.getLength();
			if (distanceSoFar >= distance)
				return i;
		}
		throw new IllegalArgumentException("distance > getLength()");
	}

	public int hashCode() {
		return start.hashCode();
	}

	public int steps() {
		return vectors.length;
	}

	/**
	 * Returns a FreerailsPathIterator that exposes a sub section of the path
	 * this object represents.
	 * 
	 * @throws IllegalArgumentException
	 *             if offset < 0
	 * @throws IllegalArgumentException
	 *             if length <= 0
	 * @throws IllegalArgumentException
	 *             if offset + length > getLength()
	 * 
	 */
	public FreerailsPathIterator subPath(int offset, int length) {
		if (offset < 0)
			throw new IllegalArgumentException();
		if (length <= 0)
			throw new IllegalArgumentException();
		if ((offset + length) > getLength())
			throw new IllegalArgumentException();

		final LinkedList<Point> points = new LinkedList<Point>();
		Point tile = getStart();
		int distanceSoFar = 0;
		for (int i = 0; i < vectors.length; i++) {

			if (distanceSoFar > offset + length) {
				break;
			}
			if (distanceSoFar >= offset) {
				int x = TILE_DIAMETER / 2 + TILE_DIAMETER * tile.x;
				int y = TILE_DIAMETER / 2 + TILE_DIAMETER * tile.y;
				points.add(new Point(x, y));
			}

			OneTileMoveVector v = vectors[i];
			tile.x += v.deltaX;
			tile.y += v.deltaY;
			distanceSoFar += v.getLength();

		}

		Point first = getPoint(offset);
		if(points.size() ==0){
			points.addFirst(first);
		}else if(!points.getFirst().equals(first)) {
			points.addFirst(first);
		}

		Point last = getPoint(offset + length);
		if (!points.getLast().equals(last)) {
			points.addLast(last);
		}

		return new FreerailsPathIterator() {
			private static final long serialVersionUID = 1L;

			int distanceTravelled = 0;

			boolean hasNext = true;

			int index = 0;

			public boolean hasNext() {
				return (index + 1) < points.size();
			}

			public void nextSegment(IntLine line) {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				Point a = points.get(index);
				line.x1 = a.x;
				line.y1 = a.y;

				Point b = points.get(index + 1);
				line.x2 = b.x;
				line.y2 = b.y;

				index++;
			}

		};
	}

}
