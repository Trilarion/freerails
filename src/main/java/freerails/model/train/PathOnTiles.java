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
package freerails.model.train;

import freerails.util.*;
import freerails.model.WorldConstants;
import freerails.model.terrain.TileTransition;
import freerails.model.track.PathIterator;

import java.io.Serializable;
import java.util.*;

/**
 * An immutable class that stores a path made up of OneTileMoveVectors.
 */
public strictfp class PathOnTiles implements Serializable {

    private static final long serialVersionUID = 3544386994122536753L;
    private final Vec2D start;
    private final ImmutableList<TileTransition> vectors;

    /**
     * @throws NullPointerException if null == start
     * @throws NullPointerException if null == vectorsList
     * @throws NullPointerException if null == vectorsList.get(i) for any i;
     */
    public PathOnTiles(Vec2D start, List<TileTransition> tileTransitions) {
        vectors = new ImmutableList<>(tileTransitions);
        Utils.verifyNoneNull(vectors);
        this.start = Utils.verifyNotNull(start);
    }

    /**
     * @throws NullPointerException if null == start
     * @throws NullPointerException if null == vectors
     * @throws NullPointerException if null == vectors[i] for any i;
     */
    // TODO remove this constructor only used from tests
    public PathOnTiles(Vec2D start, TileTransition... tileTransitions) {
        vectors = new ImmutableList<>(tileTransitions);
        Utils.verifyNoneNull(vectors);
        this.start = Utils.verifyNotNull(start);
    }

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
     * @param steps
     * @return
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
     * Returns the coordinates of the point you would be standing at if you
     * walked the specified distance along the path from the start point.
     *
     * @throws IllegalArgumentException if distance &lt; 0
     * @throws IllegalArgumentException if distance &gt; getLength()
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
                return new Vec2D(x * WorldConstants.TILE_SIZE + WorldConstants.TILE_SIZE / 2, y * WorldConstants.TILE_SIZE + WorldConstants.TILE_SIZE / 2);
            }
            if (distanceSoFar > distance) {
                int excess = (int) (WorldConstants.TILE_SIZE * (distanceSoFar - distance) / v.getLength());
                x = x * WorldConstants.TILE_SIZE - v.deltaX * excess;
                y = y * WorldConstants.TILE_SIZE - v.deltaY * excess;
                return new Vec2D(x + WorldConstants.TILE_SIZE / 2, y + WorldConstants.TILE_SIZE / 2);
            }
        }
        throw new IllegalArgumentException("distance:" + distance + " > getLength():" + vectors.size() + " distanceSoFar:" + distanceSoFar);
    }

    /**
     * Returns the coordinates of the point you would be standing at if you
     * walked the specified distance along the path from the start point.
     *
     * @throws IllegalArgumentException if distance &lt; 0
     * @throws IllegalArgumentException if distance &gt; getLength()
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
                firstPoint = new Vec2D(x * WorldConstants.TILE_SIZE + WorldConstants.TILE_SIZE / 2, y * WorldConstants.TILE_SIZE + WorldConstants.TILE_SIZE / 2);
                break;
            }
            if (distanceSoFar > firstdistance) {
                int excess = (int) (WorldConstants.TILE_SIZE * (distanceSoFar - firstdistance) / v.getLength());
                int nx = x * WorldConstants.TILE_SIZE - v.deltaX * excess + WorldConstants.TILE_SIZE / 2;
                int ny = y * WorldConstants.TILE_SIZE - v.deltaY * excess + WorldConstants.TILE_SIZE / 2;
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
                secondPoint = new Vec2D(x * WorldConstants.TILE_SIZE + WorldConstants.TILE_SIZE / 2, y * WorldConstants.TILE_SIZE + WorldConstants.TILE_SIZE / 2);
                break;
            }
            if (distanceSoFar > lastdistance) {
                int excess = (int) (WorldConstants.TILE_SIZE * (distanceSoFar - lastdistance) / v.getLength());
                int nx = x * WorldConstants.TILE_SIZE - v.deltaX * excess + WorldConstants.TILE_SIZE / 2;
                int ny = y * WorldConstants.TILE_SIZE - v.deltaY * excess + WorldConstants.TILE_SIZE / 2;
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

    /**
     * @return
     */
    public Vec2D getStart() {
        return start;
    }

    /**
     * @param i
     * @return
     */
    public TileTransition getStep(int i) {
        return vectors.get(i);
    }

    /**
     * @return
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
        return PositionOnTrack.createFacing(new Vec2D(x, y), finalTileTransition);
    }

    /**
     * Returns the index of the step that takes the distance travelled over the
     * specified distance.
     *
     * @throws IllegalArgumentException if distance &lt; 0
     * @throws IllegalArgumentException if distance &gt; getLength()
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

    @Override
    public int hashCode() {
        return start.hashCode();
    }

    /**
     * @return
     */
    public int steps() {
        return vectors.size();
    }

    /**
     * @param newTileTransition
     * @return
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
     * Returns a PathIterator that exposes a sub section of the path
     * this object represents.
     *
     * @throws IllegalArgumentException if offset &lt; 0
     * @throws IllegalArgumentException if length &le; 0
     * @throws IllegalArgumentException if offset + length &gt; getLength()
     */
    public Pair<PathIterator, Integer> subPath(double offset, double length) {
        if (offset < 0) throw new IllegalArgumentException();
        if (length <= 0) throw new IllegalArgumentException();
        if ((offset + length) > getTotalDistance())
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
                int x = WorldConstants.TILE_SIZE / 2 + WorldConstants.TILE_SIZE * tileX;
                int y = WorldConstants.TILE_SIZE / 2 + WorldConstants.TILE_SIZE * tileY;
                points.add(new Vec2D(x, y));
            }

            TileTransition v = vector;
            tileX += v.deltaX;
            tileY += v.deltaY;
            distanceSoFar += v.getLength();
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
     * @return
     */
    public Iterator<Vec2D> tilesIterator() {
        // TODO no anonymous class here
        return new Iterator<>() {
            private int index = 0;

            private Vec2D next = start;

            public boolean hasNext() {
                return next != null;
            }

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

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

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

    private static class MyPathIterator implements PathIterator {

        private static final long serialVersionUID = -4128415959622019625L;
        private final LinkedList<Vec2D> points;
        private int index;

        private MyPathIterator(LinkedList<Vec2D> points) {
            this.points = points;
            index = 0;
        }

        public boolean hasNext() {
            return (index + 1) < points.size();
        }

        public void nextSegment(LineSegment line) {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Vec2D a = points.get(index);
            line.setX1(a.x);
            line.setY1(a.y);

            Vec2D b = points.get(index + 1);
            line.setX2(b.x);
            line.setY2(b.y);

            index++;
        }
    }
}
