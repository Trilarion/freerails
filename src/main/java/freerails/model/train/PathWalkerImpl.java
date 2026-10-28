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

package freerails.model.train;

import freerails.util.Segment;
import freerails.model.track.PathIterator;
import freerails.util.Vec2D;

import java.util.NoSuchElementException;

/**
 * Walks the segments exposed by a {@link PathIterator} and emits portions of
 * them according to requested distance increments.
 */
public class PathWalkerImpl implements PathWalker {

    private static final long serialVersionUID = 4050204158701155639L;
    private final PathIterator it;

    /**
     * current segment of the path we are on.
     */
    private Segment currentSegment = new Segment(Vec2D.ZERO, Vec2D.ZERO);
    private double distanceAlongCurrentSegment = 0;
    private double distanceOfThisStepRemaining = 0;
    private boolean beforeFirst = true;
    private int lastX;
    private int lastY;

    /**
     * Creates a walker over the supplied path.
     *
     * @param i path segments to traverse
     */
    public PathWalkerImpl(PathIterator i) {
        it = i;
    }

    /** {@inheritDoc} */
    @Override
    public boolean canStepForward() {
        if (currentSegment.getLength() > distanceAlongCurrentSegment) {
            return true;
        } else return it.hasNext();
    }

    /** {@inheritDoc} */
    @Override
    public void stepForward(double distance) {
        distanceOfThisStepRemaining += distance;
    }

    /** Returns whether the current requested advance still has a segment to emit. */
    @Override
    public boolean hasNext() {
        if (0 == distanceOfThisStepRemaining) {
            return false;
        } else if (distanceAlongCurrentSegment < currentSegment.getLength()) {
            return true;
        } else return it.hasNext();
    }

    /**
     * Returns the next segment portion for the pending advance.
     *
     * @return the next traveled segment
     * @throws NoSuchElementException if no portion remains for the current advance
     * @throws IllegalStateException if consecutive emitted segments do not connect
     */
    @Override
    public Segment nextSegment() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        // If we are at the end of the current segment, start a new one.
        Vec2D start;
        if (currentSegment.getLength() <= distanceAlongCurrentSegment) {
            start = startNewSegment();
        } else {
            start = startInMiddleOfSegment();
        }

        double remainingDistanceAlongCurrentSegment = currentSegment.getLength() - distanceAlongCurrentSegment;

        Vec2D stop;
        if (distanceOfThisStepRemaining > remainingDistanceAlongCurrentSegment) {
            stop = endAtSegmentEnd(remainingDistanceAlongCurrentSegment);
        } else {
            stop = endInMiddleOfSegment();
        }

        /*
         * Sanity check: the first point of the last line should equal the
         * second point of the current line.
         *
         */
        if (!beforeFirst) {
            if (start.x != lastX) {
                throw new IllegalStateException();
            }

            if (start.y != lastY) {
                throw new IllegalStateException();
            }
        }

        lastX = stop.x;
        lastY = stop.y;
        beforeFirst = false;

        return new Segment(start, stop);
    }

    /** Computes the endpoint when the pending advance ends within the current segment. */
    private Vec2D endInMiddleOfSegment() {
        distanceAlongCurrentSegment += distanceOfThisStepRemaining;
        distanceOfThisStepRemaining = 0;
        int x = getCoordinateOnSegment(distanceAlongCurrentSegment, currentSegment.getA().x, currentSegment.getB().x);
        int y = getCoordinateOnSegment(distanceAlongCurrentSegment, currentSegment.getA().y, currentSegment.getB().y);
        return new Vec2D(x, y);
    }

    /** Returns the current segment endpoint and carries excess distance to the next segment. */
    private Vec2D endAtSegmentEnd(double remainingDistanceAlongCurrentSegment) {
        Vec2D stop = currentSegment.getB();
        distanceOfThisStepRemaining -= remainingDistanceAlongCurrentSegment;
        distanceAlongCurrentSegment = currentSegment.getLength();
        return stop;
    }

    /** Computes the start point at the current offset within the active segment. */
    private Vec2D startInMiddleOfSegment() {
        int x = getCoordinateOnSegment(distanceAlongCurrentSegment, currentSegment.getA().x, currentSegment.getB().x);
        int y = getCoordinateOnSegment(distanceAlongCurrentSegment, currentSegment.getA().y, currentSegment.getB().y);
        return new Vec2D(x, y);
    }

    /** Loads the next path segment and returns its starting point. */
    private Vec2D startNewSegment() {
        currentSegment = it.nextSegment();
        distanceAlongCurrentSegment = 0;
        return currentSegment.getA();
    }

    /**
     * Interpolates one coordinate along the active segment at the given distance.
     *
     * @param distanceAlongSegment distance from the segment start
     * @param coordinate1 coordinate at the segment start
     * @param coordinate2 coordinate at the segment end
     * @return the interpolated coordinate, truncated to an integer
     */
    private int getCoordinateOnSegment(double distanceAlongSegment, int coordinate1, int coordinate2) {
        double segmentLength = currentSegment.getLength();
        double delta = 0;

        if (0 != segmentLength) {
            delta = (coordinate2 - coordinate1) * distanceAlongSegment / segmentLength;
        }

        return coordinate1 + (int) delta;
    }
}