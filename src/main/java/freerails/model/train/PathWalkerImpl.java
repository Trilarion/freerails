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

package freerails.model.train;

import freerails.util.Segment;
import freerails.model.track.PathIterator;
import freerails.util.Vec2D;

import java.util.NoSuchElementException;

/**
 * PathWalker that walks the path exposed by a PathIterator.
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
     * @param i
     */
    public PathWalkerImpl(PathIterator i) {
        it = i;
    }

    /**
     * @return true if we still have more of the current segment, or more
     * segments left.
     */
    @Override
    public boolean canStepForward() {
        if (currentSegment.getLength() > distanceAlongCurrentSegment) {
            return true;
        } else return it.hasNext();
    }

    /**
     * Specify the distance this PathWalker is to progress along the current
     * step.
     */
    @Override
    public void stepForward(double distance) {
        distanceOfThisStepRemaining += distance;
    }

    /**
     * @return true if there is still some distance to go along this path
     */
    @Override
    public boolean hasNext() {
        if (0 == distanceOfThisStepRemaining) {
            return false;
        } else if (distanceAlongCurrentSegment < currentSegment.getLength()) {
            return true;
        } else return it.hasNext();
    }

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

    private Vec2D endInMiddleOfSegment() {
        distanceAlongCurrentSegment += distanceOfThisStepRemaining;
        distanceOfThisStepRemaining = 0;
        int x = getCoordinateOnSegment(distanceAlongCurrentSegment, currentSegment.getA().x, currentSegment.getB().x);
        int y = getCoordinateOnSegment(distanceAlongCurrentSegment, currentSegment.getA().y, currentSegment.getB().y);
        return new Vec2D(x, y);
    }

    private Vec2D endAtSegmentEnd(double remainingDistanceAlongCurrentSegment) {
        Vec2D stop = currentSegment.getB();
        distanceOfThisStepRemaining -= remainingDistanceAlongCurrentSegment;
        distanceAlongCurrentSegment = currentSegment.getLength();
        return stop;
    }

    private Vec2D startInMiddleOfSegment() {
        int x = getCoordinateOnSegment(distanceAlongCurrentSegment, currentSegment.getA().x, currentSegment.getB().x);
        int y = getCoordinateOnSegment(distanceAlongCurrentSegment, currentSegment.getA().y, currentSegment.getB().y);
        return new Vec2D(x, y);
    }

    private Vec2D startNewSegment() {
        currentSegment = it.nextSegment();
        distanceAlongCurrentSegment = 0;
        return currentSegment.getA();
    }

    private int getCoordinateOnSegment(double distanceAlongSegment, int coordinate1, int coordinate2) {
        double segmentLength = currentSegment.getLength();
        double delta = 0;

        if (0 != segmentLength) {
            delta = (coordinate2 - coordinate1) * distanceAlongSegment / segmentLength;
        }

        return coordinate1 + (int) delta;
    }
}