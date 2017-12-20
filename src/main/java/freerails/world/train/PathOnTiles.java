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
package freerails.world.train;

import freerails.util.ImList;
import freerails.util.ImPoint;
import freerails.util.IntLine;
import freerails.util.Pair;
import freerails.world.FreerailsPathIterator;
import freerails.world.PositionOnTrack;
import freerails.world.TileTransition;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import static freerails.world.TileTransition.TILE_DIAMETER;

/**
 * An immutable class that stores a path made up of OneTileMoveVectors.
 */
strictfp public class PathOnTiles implements Serializable {

    private static final long serialVersionUID = 3544386994122536753L;

    private final ImPoint start;

    private final ImList<TileTransition> vectors;

    /**
     * @param start
     * @param vectorsList
     * @throws NullPointerException if null == start
     * @throws NullPointerException if null == vectorsList
     * @throws NullPointerException if null == vectorsList.get(i) for any i;
     */
    public PathOnTiles(ImPoint start, List<TileTransition> vectorsList) {
        if (null == start)
            throw new NullPointerException();
        vectors = new ImList<>(vectorsList);
        vectors.checkForNulls();
        this.start = start;
    }

    /**
     * @param start
     * @param vectors
     * @throws NullPointerException if null == start
     * @throws NullPointerException if null == vectors
     * @throws NullPointerException if null == vectors[i] for any i;
     */
    public PathOnTiles(ImPoint start, TileTransition... vectors) {
        if (null == start)
            throw new NullPointerException();
        this.vectors = new ImList<>(vectors);
        this.vectors.checkForNulls();
        this.start = start;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PathOnTiles))
            return false;

        final PathOnTiles pathOnTiles = (PathOnTiles) o;

        if (!start.equals(pathOnTiles.start))
            return false;
        return vectors.equals(pathOnTiles.vectors);
    }

    /**
     * Returns the distance you would travel if you walked the all the way along
     * the path.
     *
     * @return
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
     * @param distance
     * @return
     * @throws IllegalArgumentException if distance < 0
     * @throws IllegalArgumentException if distance > getLength()
     */
    public ImPoint getPoint(double distance) {
        if (0 > distance)
            throw new IllegalArgumentException("distance:" + distance + " < 0");

        int x = start.x;
        int y = start.y;
        double distanceSoFar = 0;
        for (int i = 0; i < vectors.size(); i++) {
            TileTransition v = vectors.get(i);
            distanceSoFar += v.getLength();
            x += v.deltaX;
            y += v.deltaY;
            if (distanceSoFar == distance) {
                return new ImPoint(x * TILE_DIAMETER + TILE_DIAMETER / 2, y
                        * TILE_DIAMETER + TILE_DIAMETER / 2);
            }
            if (distanceSoFar > distance) {
                int excess = (int) (TILE_DIAMETER * (distanceSoFar - distance) / v
                        .getLength());
                x = x * TILE_DIAMETER - v.deltaX * excess;
                y = y * TILE_DIAMETER - v.deltaY * excess;
                return new ImPoint(x + TILE_DIAMETER / 2, y + TILE_DIAMETER / 2);
            }
        }
        throw new IllegalArgumentException("distance:" + distance
                + " > getLength():" + vectors.size() + " distanceSoFar:"
                + distanceSoFar);
    }

    /**
     * Returns the coordinates of the point you would be standing at if you
     * walked the specified distance along the path from the start point.
     *
     * @param firstdistance
     * @param lastdistance
     * @return
     * @throws IllegalArgumentException if distance < 0
     * @throws IllegalArgumentException if distance > getLength()
     */
    public Pair<ImPoint, ImPoint> getPoint(double firstdistance,
                                           double lastdistance) {
        if (0 > firstdistance) {
            throw new IllegalArgumentException("firstdistance:" + firstdistance
                    + " < 0");
        }
        if (0 > lastdistance) {
            throw new IllegalArgumentException("lastdistance:" + lastdistance
                    + " < 0");
        }
        if (firstdistance > lastdistance) {
            throw new IllegalArgumentException("firstdistance:" + firstdistance
                    + " > lastdistance:" + lastdistance);
        }
        int x = start.x;
        int y = start.y;
        double distanceSoFar = 0;
        ImPoint firstPoint = null;
        int i;
        TileTransition v = null;
        final int vectorsSize = vectors.size();
        for (i = 0; i < vectorsSize; i++) {
            v = vectors.get(i);
            distanceSoFar += v.getLength();
            x += v.deltaX;
            y += v.deltaY;
            if (distanceSoFar == firstdistance) {
                firstPoint = new ImPoint(x * TILE_DIAMETER + TILE_DIAMETER / 2,
                        y * TILE_DIAMETER + TILE_DIAMETER / 2);
                break;
            }
            if (distanceSoFar > firstdistance) {
                int excess = (int) (TILE_DIAMETER
                        * (distanceSoFar - firstdistance) / v.getLength());
                int nx = x * TILE_DIAMETER - v.deltaX * excess + TILE_DIAMETER
                        / 2;
                int ny = y * TILE_DIAMETER - v.deltaY * excess + TILE_DIAMETER
                        / 2;
                firstPoint = new ImPoint(nx, ny);
                break;
            }
        }
        if (firstPoint == null) {
            throw new IllegalArgumentException("firstdistance:" + firstdistance
                    + " > getLength():" + vectorsSize + " distanceSoFar:"
                    + distanceSoFar);
        }
        if (firstdistance == lastdistance) {
            return new Pair<>(firstPoint, firstPoint);
        }
        ImPoint secondPoint = null;

        do {

            if (distanceSoFar == lastdistance) {
                secondPoint = new ImPoint(
                        x * TILE_DIAMETER + TILE_DIAMETER / 2, y
                        * TILE_DIAMETER + TILE_DIAMETER / 2);
                break;
            }
            if (distanceSoFar > lastdistance) {
                int excess = (int) (TILE_DIAMETER
                        * (distanceSoFar - lastdistance) / v.getLength());
                int nx = x * TILE_DIAMETER - v.deltaX * excess + TILE_DIAMETER
                        / 2;
                int ny = y * TILE_DIAMETER - v.deltaY * excess + TILE_DIAMETER
                        / 2;
                secondPoint = new ImPoint(nx, ny);
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
            throw new IllegalArgumentException("lastdistance:" + lastdistance
                    + " > getLength():" + vectorsSize + " distanceSoFar:"
                    + distanceSoFar);
        }

        return new Pair<>(firstPoint, secondPoint);
    }

    /**
     * @return
     */
    public ImPoint getStart() {
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
        for (int i = 0; i < vectors.size(); i++) {
            TileTransition v = vectors.get(i);
            x += v.deltaX;
            y += v.deltaY;
        }
        int i = vectors.size() - 1;
        TileTransition finalTileTransition = vectors.get(i);
        return PositionOnTrack.createFacing(x, y, finalTileTransition);
    }

    /**
     * Returns the index of the step that takes the distance travelled over the
     * specified distance.
     *
     * @param distance
     * @return
     * @throws IllegalArgumentException if distance < 0
     * @throws IllegalArgumentException if distance > getLength()
     */
    public int getStepIndex(int distance) {
        if (0 > distance)
            throw new IllegalArgumentException("distance < 0");
        int distanceSoFar = 0;
        for (int i = 0; i < vectors.size(); i++) {
            TileTransition v = vectors.get(i);
            distanceSoFar += v.getLength();
            if (distanceSoFar >= distance)
                return i;
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
     * @param newTileTransitions
     * @return
     */
    public PathOnTiles addSteps(TileTransition... newTileTransitions) {
        int oldLength = vectors.size();
        TileTransition[] newPath = new TileTransition[oldLength + newTileTransitions.length];
        for (int i = 0; i < oldLength; i++) {
            newPath[i] = vectors.get(i);
        }
        System.arraycopy(newTileTransitions, 0, newPath, oldLength, newTileTransitions.length);
        return new PathOnTiles(start, newPath);
    }

    /**
     * Returns a FreerailsPathIterator that exposes a sub section of the path
     * this object represents.
     *
     * @param offset
     * @param length
     * @return
     * @throws IllegalArgumentException if offset < 0
     * @throws IllegalArgumentException if length <= 0
     * @throws IllegalArgumentException if offset + length > getLength()
     */
    public Pair<FreerailsPathIterator, Integer> subPath(double offset,
                                                        double length) {
        if (offset < 0)
            throw new IllegalArgumentException();
        if (length <= 0)
            throw new IllegalArgumentException();
        if ((offset + length) > getTotalDistance())
            throw new IllegalArgumentException(offset + " + " + length + " > "
                    + getTotalDistance());

        final LinkedList<ImPoint> points = new LinkedList<>();
        ImPoint tile = getStart();
        int tileX = tile.x;
        int tileY = tile.y;
        int distanceSoFar = 0;
        for (int i = 0; i < vectors.size(); i++) {

            if (distanceSoFar > offset + length) {
                break;
            }
            if (distanceSoFar >= offset) {
                int x = TILE_DIAMETER / 2 + TILE_DIAMETER * tileX;
                int y = TILE_DIAMETER / 2 + TILE_DIAMETER * tileY;
                points.add(new ImPoint(x, y));
            }

            TileTransition v = vectors.get(i);
            tileX += v.deltaX;
            tileY += v.deltaY;
            distanceSoFar += v.getLength();

        }

        Pair<ImPoint, ImPoint> point = getPoint(offset, offset + length);

        ImPoint first = point.getA();

        if (points.size() == 0) {
            points.addFirst(first);
        } else if (!points.getFirst().equals(first)) {
            points.addFirst(first);
        }

        ImPoint last = point.getB();

        if (!points.getLast().equals(last)) {
            points.addLast(last);
        }

        return new Pair<>(
                new FreerailsPathIterator() {
                    private static final long serialVersionUID = 1L;

                    int index = 0;

                    public boolean hasNext() {
                        return (index + 1) < points.size();
                    }

                    public void nextSegment(IntLine line) {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        ImPoint a = points.get(index);
                        line.x1 = a.x;
                        line.y1 = a.y;

                        ImPoint b = points.get(index + 1);
                        line.x2 = b.x;
                        line.y2 = b.y;

                        index++;
                    }

                }, points.size());
    }

    /**
     * @return
     */
    public Iterator<ImPoint> tiles() {
        return new Iterator<ImPoint>() {
            int index = 0;

            ImPoint next = start;

            public boolean hasNext() {
                return next != null;
            }

            public ImPoint next() {
                if (next == null)
                    throw new NoSuchElementException();

                ImPoint returnValue = next;
                int x = next.x;
                int y = next.y;
                if (index < vectors.size()) {
                    TileTransition s = vectors.get(index);
                    x += s.deltaX;
                    y += s.deltaY;
                    next = new ImPoint(x, y);
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
        sb.append("{");
        sb.append(start.x);
        sb.append(", ");
        sb.append(start.y);
        for (int i = 0; i < vectors.size(); i++) {
            sb.append(", ");
            sb.append(vectors.get(i));
        }
        sb.append("}");
        return sb.toString();
    }

}
