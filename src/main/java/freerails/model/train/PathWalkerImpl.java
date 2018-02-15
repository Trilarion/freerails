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

import freerails.util.LineSegment;
import freerails.model.track.PathIterator;

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
    private final LineSegment currentSegment = new LineSegment();
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
    public boolean canStepForward() {
        if (currentSegment.getLength() > distanceAlongCurrentSegment) {
            return true;
        } else return it.hasNext();
    }

    /**
     * Specify the distance this PathWalker is to progress along the current
     * step.
     */
    public void stepForward(double distance) {
        distanceOfThisStepRemaining += distance;
    }

    /**
     * @return true if there is still some distance to go along this path
     */
    public boolean hasNext() {
        if (0 == distanceOfThisStepRemaining) {
            return false;
        } else if (distanceAlongCurrentSegment < currentSegment.getLength()) {
            return true;
        } else return it.hasNext();
    }

    public void nextSegment(LineSegment line) {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        // If we are at the end of the current segment, start a new one.
        if (currentSegment.getLength() <= distanceAlongCurrentSegment) {
            startNewSegment(line);
        } else {
            startInMiddleOfSegment(line);
        }

        double remainingDistanceAlongCurrentSegment = currentSegment.getLength() - distanceAlongCurrentSegment;

        if (distanceOfThisStepRemaining > remainingDistanceAlongCurrentSegment) {
            endAtSegmentEnd(line, remainingDistanceAlongCurrentSegment);
        } else {
            endInMiddleOfSegment(line);
        }

        /*
         * Sanity check: the first point of the last line should equal the
         * second point of the current line.
         *
         */
        if (!beforeFirst) {
            if (line.getX1() != lastX) {
                throw new IllegalStateException();
            }

            if (line.getY1() != lastY) {
                throw new IllegalStateException();
            }
        }

        lastX = line.getX2();
        lastY = line.getY2();
        beforeFirst = false;
    }

    private void endInMiddleOfSegment(LineSegment line) {
        distanceAlongCurrentSegment += distanceOfThisStepRemaining;
        distanceOfThisStepRemaining = 0;
        line.setX2(getCoorinateOnSegment(distanceAlongCurrentSegment, currentSegment.getX1(), currentSegment.getX2()));
        line.setY2(getCoorinateOnSegment(distanceAlongCurrentSegment, currentSegment.getY1(), currentSegment.getY2()));
    }

    private void endAtSegmentEnd(LineSegment line, double remainingDistanceAlongCurrentSegment) {
        line.setX2(currentSegment.getX2());
        line.setY2(currentSegment.getY2());
        distanceOfThisStepRemaining -= remainingDistanceAlongCurrentSegment;
        distanceAlongCurrentSegment = currentSegment.getLength();
    }

    private void startInMiddleOfSegment(LineSegment line) {
        line.setX1(getCoorinateOnSegment(distanceAlongCurrentSegment, currentSegment.getX1(), currentSegment.getX2()));
        line.setY1(getCoorinateOnSegment(distanceAlongCurrentSegment, currentSegment.getY1(), currentSegment.getY2()));
    }

    private void startNewSegment(LineSegment line) {
        it.nextSegment(currentSegment);
        distanceAlongCurrentSegment = 0;
        line.setX1(currentSegment.getX1());
        line.setY1(currentSegment.getY1());
    }

    private int getCoorinateOnSegment(double distanceAlongSegment, int coordinate1, int coordinate2) {
        double segmentLength = currentSegment.getLength();
        double delta = 0;

        if (0 != segmentLength) {
            delta = (coordinate2 - coordinate1) * distanceAlongSegment / segmentLength;
        }

        return coordinate1 + (int) delta;
    }
}