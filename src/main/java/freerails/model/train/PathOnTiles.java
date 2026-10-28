/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
package freerails.model.train;

import freerails.util.*;
import freerails.model.ModelConstants;
import freerails.model.terrain.TileTransition;
import freerails.model.track.PathIterator;

import java.io.Serializable;
import java.util.*;

/**
 * Immutable sequence of tile transitions beginning at a specified tile.
 */
public class PathOnTiles implements Serializable {

    private static final long serialVersionUID = 3544386994122536753L;
    private final Vec2D start;
    private final List<TileTransition> vectors;

    /**
     * @param start starting tile coordinates
     * @param tileTransitions transitions from the start tile, in traversal order
     * @throws NullPointerException if the start, transition list, or any transition is null
     */
    public PathOnTiles(Vec2D start, List<TileTransition> tileTransitions) {
        vectors = new ArrayList<>(tileTransitions);
        Utils.verifyNoneNull(vectors);
        this.start = Utils.verifyNotNull(start);
    }

    // TODO remove this constructor only used from tests
    /**
     * Creates a path from a variable number of transitions.
     *
     * @param start starting tile coordinates
     * @param tileTransitions transitions from the start tile, in traversal order
     * @throws NullPointerException if the start, transition array, or any transition is null
     */
    public PathOnTiles(Vec2D start, TileTransition... tileTransitions) {
        vectors = new ArrayList<>(Arrays.asList(tileTransitions));
        Utils.verifyNoneNull(vectors);
        this.start = Utils.verifyNotNull(start);
    }

    /** Compares the starting tile and ordered transitions. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PathOnTiles)) return false;
        final PathOnTiles other = (PathOnTiles) obj;
        if (!start.equals(other.start)) return false;
        return vectors.equals(other.vectors);
    }

    /**
     * Returns the distance you would travel if you walked the all the way along
     * the path.
     */
    public double getTotalDistance() {
        return getDistance(vectors.size());
    }

    /**
     * Returns the distance covered by the first {@code steps} transitions.
     *
     * @param steps number of transitions to measure
     * @return cumulative transition length
     * @throws IndexOutOfBoundsException if {@code steps} exceeds the number of transitions
     */
    public double getDistance(int steps) {
        double distanceSoFar = 0;
        for (int i = 0; i < steps; i++) {
            TileTransition v = vectors.get(i);
            distanceSoFar += v.getLength();
        }
        return distanceSoFar;
    }

    /**
     * Returns the display-coordinate point at the specified distance from the
     * start of the path.
     *
     * @param distance distance along the path
     * @return point at that distance
     *
     * @throws IllegalArgumentException if distance &lt; 0
     * @throws IllegalArgumentException if distance exceeds the path length
     */
    public Vec2D getPoint(double distance) {
        if (0 > distance) throw new IllegalArgumentException("distance:" + distance + " < 0");

        int x = start.x;
        int y = start.y;
        double distanceSoFar = 0;
        for (TileTransition v : vectors) {
            distanceSoFar += v.getLength();
            x += v.deltaX;
            y += v.deltaY;
            if (distanceSoFar == distance) {
                return new Vec2D(x * ModelConstants.TILE_SIZE + ModelConstants.TILE_SIZE / 2, y * ModelConstants.TILE_SIZE + ModelConstants.TILE_SIZE / 2);
            }
            if (distanceSoFar > distance) {
                int excess = (int) (ModelConstants.TILE_SIZE * (distanceSoFar - distance) / v.getLength());
                x = x * ModelConstants.TILE_SIZE - v.deltaX * excess;
                y = y * ModelConstants.TILE_SIZE - v.deltaY * excess;
                return new Vec2D(x + ModelConstants.TILE_SIZE / 2, y + ModelConstants.TILE_SIZE / 2);
            }
        }
        throw new IllegalArgumentException("distance:" + distance + " > getLength():" + vectors.size() + " distanceSoFar:" + distanceSoFar);
    }

    /**
     * Returns the two display-coordinate points delimiting the specified path interval.
     *
     * @param firstdistance interval start measured from the path start
     * @param lastdistance interval end measured from the path start
     * @return the points at the requested distances
     * @throws IllegalArgumentException if either distance is negative, the start
     *         exceeds the end, or the end lies beyond the path
     */
    public Pair<Vec2D, Vec2D> getPoint(double firstdistance, double lastdistance) {
        if (0 > firstdistance) {
            throw new IllegalArgumentException("firstdistance:" + firstdistance + " < 0");
        }
        if (0 > lastdistance) {
            throw new IllegalArgumentException("lastdistance:" + lastdistance + " < 0");
        }
        if (firstdistance > lastdistance) {
            throw new IllegalArgumentException("firstdistance:" + firstdistance + " > lastdistance:" + lastdistance);
        }
        int x = start.x;
        int y = start.y;
        double distanceSoFar = 0;
        Vec2D firstPoint = null;
        int i;
        TileTransition v = null;
        final int vectorsSize = vectors.size();
        for (i = 0; i < vectorsSize; i++) {
            v = vectors.get(i);
            distanceSoFar += v.getLength();
            x += v.deltaX;
            y += v.deltaY;
            if (distanceSoFar == firstdistance) {
                firstPoint = new Vec2D(x * ModelConstants.TILE_SIZE + ModelConstants.TILE_SIZE / 2, y * ModelConstants.TILE_SIZE + ModelConstants.TILE_SIZE / 2);
                break;
            }
            if (distanceSoFar > firstdistance) {
                int excess = (int) (ModelConstants.TILE_SIZE * (distanceSoFar - firstdistance) / v.getLength());
                int nx = x * ModelConstants.TILE_SIZE - v.deltaX * excess + ModelConstants.TILE_SIZE / 2;
                int ny = y * ModelConstants.TILE_SIZE - v.deltaY * excess + ModelConstants.TILE_SIZE / 2;
                firstPoint = new Vec2D(nx, ny);
                break;
            }
        }
        if (firstPoint == null) {
            throw new IllegalArgumentException("firstdistance:" + firstdistance + " > getLength():" + vectorsSize + " distanceSoFar:" + distanceSoFar);
        }
        if (firstdistance == lastdistance) {
            return new Pair<>(firstPoint, firstPoint);
        }
        Vec2D secondPoint = null;

        do {

            if (distanceSoFar == lastdistance) {
                secondPoint = new Vec2D(x * ModelConstants.TILE_SIZE + ModelConstants.TILE_SIZE / 2, y * ModelConstants.TILE_SIZE + ModelConstants.TILE_SIZE / 2);
                break;
            }
            if (distanceSoFar > lastdistance) {
                int excess = (int) (ModelConstants.TILE_SIZE * (distanceSoFar - lastdistance) / v.getLength());
                int nx = x * ModelConstants.TILE_SIZE - v.deltaX * excess + ModelConstants.TILE_SIZE / 2;
                int ny = y * ModelConstants.TILE_SIZE - v.deltaY * excess + ModelConstants.TILE_SIZE / 2;
                secondPoint = new Vec2D(nx, ny);
                break;
            }
            i++;
            if (i >= vectorsSize) {
                break;
            }
            v = vectors.get(i);
            distanceSoFar += v.getLength();
            x += v.deltaX;
            y += v.deltaY;
        } while (true);

        if (secondPoint == null) {
            throw new IllegalArgumentException("lastdistance:" + lastdistance + " > getLength():" + vectorsSize + " distanceSoFar:" + distanceSoFar);
        }

        return new Pair<>(firstPoint, secondPoint);
    }

    /** Returns the starting tile coordinates. */
    public Vec2D getStart() {
        return start;
    }

    /**
     * Returns the transition at the specified index.
     *
     * @param i zero-based transition index
     * @return transition at {@code i}
     * @throws IndexOutOfBoundsException if {@code i} is outside the path
     */
    public TileTransition getStep(int i) {
        return vectors.get(i);
    }

    /**
     * Returns the position reached after traversing the final transition.
     *
     * @return final tile and direction of entry
     * @throws IndexOutOfBoundsException if this path has no transitions
     */
    public PositionOnTrack getFinalPosition() {
        int x = start.x;
        int y = start.y;
        for (TileTransition v : vectors) {
            x += v.deltaX;
            y += v.deltaY;
        }
        int i = vectors.size() - 1;
        TileTransition finalTileTransition = vectors.get(i);
        return new PositionOnTrack(new Vec2D(x, y), finalTileTransition.getOpposite());
    }

    /**
     * Returns the index of the transition that reaches or passes the distance.
     *
     * @param distance distance from the path start
     * @return index of the transition crossing the distance
     * @throws IllegalArgumentException if distance &lt; 0
     * @throws IllegalArgumentException if distance exceeds the path length
     */
    public int getStepIndex(int distance) {
        if (0 > distance) throw new IllegalArgumentException("distance < 0");
        int distanceSoFar = 0;
        for (int i = 0; i < vectors.size(); i++) {
            TileTransition v = vectors.get(i);
            distanceSoFar += v.getLength();
            if (distanceSoFar >= distance) return i;
        }
        throw new IllegalArgumentException("distance > getLength()");
    }

    /** Returns a hash code based on the path's starting tile. */
    @Override
    public int hashCode() {
        return start.hashCode();
    }

    /** Returns the number of tile transitions in this path. */
    public int steps() {
        return vectors.size();
    }

    /**
     * Returns a new path with the supplied transition appended.
     *
     * @param newTileTransition transition to append
     * @return a path containing all current transitions followed by the new one
     */
    public PathOnTiles addStep(TileTransition newTileTransition) {
        int oldLength = vectors.size();
        TileTransition[] newPath = new TileTransition[oldLength + 1];
        for (int i = 0; i < oldLength; i++) {
            newPath[i] = vectors.get(i);
        }
        newPath[oldLength] = newTileTransition;
        return new PathOnTiles(start, Arrays.asList(newPath));
    }

    /**
     * Returns an iterator over the portion of this path between the given distances.
     *
     * @param offset distance from the path start to the subpath start
     * @param length length of the subpath
     * @return the subpath iterator and its number of points
     * @throws IllegalArgumentException if offset &lt; 0
     * @throws IllegalArgumentException if length &le; 0
     * @throws IllegalArgumentException if offset + length &gt; getLength()
     */
    public Pair<PathIterator, Integer> subPath(double offset, double length) {
        if (offset < 0) throw new IllegalArgumentException();
        if (length <= 0) throw new IllegalArgumentException();
        if (offset + length > getTotalDistance())
            throw new IllegalArgumentException(offset + " + " + length + " > " + getTotalDistance());

        final LinkedList<Vec2D> points = new LinkedList<>();
        Vec2D tile = start;
        int tileX = tile.x;
        int tileY = tile.y;
        int distanceSoFar = 0;
        for (TileTransition vector : vectors) {

            if (distanceSoFar > offset + length) {
                break;
            }
            if (distanceSoFar >= offset) {
                int x = ModelConstants.TILE_SIZE / 2 + ModelConstants.TILE_SIZE * tileX;
                int y = ModelConstants.TILE_SIZE / 2 + ModelConstants.TILE_SIZE * tileY;
                points.add(new Vec2D(x, y));
            }

            tileX += vector.deltaX;
            tileY += vector.deltaY;
            distanceSoFar += vector.getLength();
        }

        Pair<Vec2D, Vec2D> point = getPoint(offset, offset + length);

        Vec2D first = point.getA();

        if (points.isEmpty()) {
            points.addFirst(first);
        } else if (!points.getFirst().equals(first)) {
            points.addFirst(first);
        }

        Vec2D last = point.getB();

        if (!points.getLast().equals(last)) {
            points.addLast(last);
        }

        return new Pair<>(new MyPathIterator(points), points.size());
    }

    /**
    /**
     * Returns an iterator over the tile coordinates visited, including the start
     * tile and the tile reached by each transition.
     *
     * @return iterator over visited tile coordinates
     */
    public Iterator<Vec2D> tilesIterator() {
        // TODO no anonymous class here
        return new Iterator<>() {
            private int index = 0;

            private Vec2D next = start;

            /** Returns whether another visited tile remains. */
            @Override
            public boolean hasNext() {
                return next != null;
            }

            /**
             * Returns the next visited tile.
             *
             * @return next tile coordinate
             * @throws NoSuchElementException if all tiles have been returned
             */
            @Override
            public Vec2D next() {
                if (next == null) throw new NoSuchElementException();

                Vec2D returnValue = next;
                int x = next.x;
                int y = next.y;
                if (index < vectors.size()) {
                    TileTransition s = vectors.get(index);
                    x += s.deltaX;
                    y += s.deltaY;
                    next = new Vec2D(x, y);
                } else {
                    next = null;
                }
                index++;

                return returnValue;
            }

            /** Removal is unsupported because this iterator does not modify the path. */
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /** Returns a textual representation of the start tile and transitions. */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append('{');
        sb.append(start.x);
        sb.append(", ");
        sb.append(start.y);
        for (TileTransition vector : vectors) {
            sb.append(", ");
            sb.append(vector);
        }
        sb.append('}');
        return sb.toString();
    }

    /** Path iterator backed by an ordered list of points. */
    private static class MyPathIterator implements PathIterator {

        private static final long serialVersionUID = -4128415959622019625L;
        private final LinkedList<Vec2D> points;
        private int index;

        /** Creates an iterator over the supplied points. */
        private MyPathIterator(LinkedList<Vec2D> points) {
            this.points = points;
            index = 0;
        }

        /** Returns whether another segment can be formed from the remaining points. */
        @Override
        public boolean hasNext() {
            return index + 1 < points.size();
        }

        /**
         * Returns the segment between the next pair of points.
         *
         * @return next path segment
         * @throws NoSuchElementException if fewer than two points remain
         */
        @Override
        public Segment nextSegment() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Vec2D a = points.get(index);
            Vec2D b = points.get(index + 1);
            index++;

            return new Segment(a, b);
        }
    }
}
