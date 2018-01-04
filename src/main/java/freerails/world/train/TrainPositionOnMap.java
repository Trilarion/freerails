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

package freerails.world.train;

import freerails.util.ImInts;
import freerails.util.IntArray;
import freerails.util.IntLine;
import freerails.util.Pair;
import freerails.world.FreerailsPathIterator;

import java.io.Serializable;

/**
 * This <b>immutable</b> class represents the position of a train as a String
 * of points. There must be at least two points. The first point is the position
 * of the front of the train; the last point is the position of the end of the
 * train. Any intermediate points are positions of 'kinks' in the track.
 *
 * Coordinates are expressed in display coordinates relative to the map origin
 * (as opposed to map squares).
 *
 *
 *
 * Train positions can be combined and divided as illustrated below (notice what
 * happens to the head and tail that are combined)
 *
 * <table width="100%" border="0">
 * <caption>??</caption>
 * <tr>
 * <td>if</td>
 * <td>{@code a}</td>
 * <td>{@code =}</td>
 * <td><code>{<strong>(10, 10)</strong>, (20,20), (30,30), (40,40) }</code></td>
 * </tr>
 * <tr>
 * <td>and</td>
 * <td>{@code b}</td>
 * <td>{@code =}</td>
 * <td><code>{(1,1), (4,4), (5,5), <strong>(10, 10)</strong>}</code></td>
 * </tr>
 * <tr>
 * <td>then</td>
 * <td>{@code a.addToHead(b)}</td>
 * <td>{@code =}</td>
 * <td>{@code {(1,1), (4,4), (5,5), (20,20), (30,30), (40,40) }}</td>
 * </tr>
 * <tr>
 * <td>and</td>
 * <td>{@code b.addToTail(a)}</td>
 * <td>{@code =}</td>
 * <td>{@code {(1,1), (4,4), (5,5), (20,20), (30,30), (40,40) }}</td>
 * </tr>
 * <tr>
 * <td>and if</td>
 * <td>{@code c}</td>
 * <td>{@code =}</td>
 * <td>{@code {(1,1), (4,4), (5,5), (20,20), (30,30), (40,40) }}</td>
 * </tr>
 * <tr>
 * <td>then</td>
 * <td>{@code c.removeFromTail(a)}</td>
 * <td>{@code =}</td>
 * <td>{@code {(1,1), (4,4), (5,5), (10, 10)}}</td>
 * </tr>
 * <tr>
 * <td>and</td>
 * <td>{@code c.removeFromHead(b)}</td>
 * <td>{@code =}</td>
 * <td>{@code {(10, 10), (20,20), (30,30), (40,40) }}</td>
 * </tr>
 * </table>
 */
public class TrainPositionOnMap implements Serializable {

    /**
     *
     */
    public static final int CRASH_FRAMES_COUNT = 15;

    private static final long serialVersionUID = 3979269144611010865L;

    private final ImInts xpoints;

    private final ImInts ypoints;

    private final double speed, acceleration;

    private final SpeedTimeAndStatus.TrainActivity activity;

    private TrainPositionOnMap(int[] xs, int[] ys, double speed,
                               double acceleration, SpeedTimeAndStatus.TrainActivity activity) {
        if (xs.length != ys.length) {
            throw new IllegalArgumentException();
        }

        xpoints = new ImInts(xs);
        ypoints = new ImInts(ys);
        this.acceleration = acceleration;
        this.speed = speed;
        this.activity = activity;
    }

    /**
     * @param xpoints
     * @param ypoints
     * @return
     */
    public static TrainPositionOnMap createInstance(int[] xpoints, int[] ypoints) {
        return new TrainPositionOnMap(xpoints, ypoints, 0d, 0d,
                SpeedTimeAndStatus.TrainActivity.READY);
    }

    /**
     * @param path
     * @return
     */
    public static TrainPositionOnMap createInSameDirectionAsPath(
            FreerailsPathIterator path) {
        return createInSameDirectionAsPath(path, 0d, 0d,
                SpeedTimeAndStatus.TrainActivity.READY);
    }

    /**
     * @param path
     * @param speed
     * @param acceleration
     * @param activity
     * @return
     */
    public static TrainPositionOnMap createInSameDirectionAsPathReversed(
            Pair<FreerailsPathIterator, Integer> path, double speed,
            double acceleration, SpeedTimeAndStatus.TrainActivity activity) {

        IntLine line = new IntLine();

        FreerailsPathIterator pathIt = path.getA();
        int pathSize = path.getB();

        if (pathSize > 10000) {
            throw new IllegalStateException(
                    "The TrainPosition has more than 10,000 points, which suggests that something is wrong.");
        }
        int[] xPoints = new int[pathSize];
        int[] yPoints = new int[pathSize];

        for (int i = pathSize - 1; i > 0; i--) {
            if (!pathIt.hasNext()) {
                throw new IllegalStateException("Programming error at:" + i
                        + " from:" + pathSize);
            }
            pathIt.nextSegment(line);
            xPoints[i] = line.x1;
            yPoints[i] = line.y1;
        }

        xPoints[0] = line.x2;
        yPoints[0] = line.y2;

        return new TrainPositionOnMap(xPoints, yPoints, speed, acceleration,
                activity);
    }

    /**
     * @param path
     * @param speed
     * @param acceleration
     * @param activity
     * @return
     */
    public static TrainPositionOnMap createInSameDirectionAsPath(
            FreerailsPathIterator path, double speed, double acceleration,
            SpeedTimeAndStatus.TrainActivity activity) {
        IntArray xPointsIntArray = new IntArray();
        IntArray yPointsIntArray = new IntArray();
        IntLine line = new IntLine();
        int i = 0;

        while (path.hasNext()) {
            path.nextSegment(line);
            xPointsIntArray.add(i, line.x1);
            yPointsIntArray.add(i, line.y1);
            i++;

            if (i > 10000) {
                throw new IllegalStateException(
                        "The TrainPosition has more than 10,000 points, which suggests that something is wrong.");
            }
        }

        xPointsIntArray.add(i, line.x2);
        yPointsIntArray.add(i, line.y2);

        int[] xPoints;
        int[] yPoints;

        xPoints = xPointsIntArray.toArray();
        yPoints = yPointsIntArray.toArray();

        return new TrainPositionOnMap(xPoints, yPoints, speed, acceleration,
                activity);
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static boolean headsAreEqual(TrainPositionOnMap a,
                                        TrainPositionOnMap b) {
        int aHeadX = a.getX(0);
        int aHeadY = a.getY(0);
        int bHeadX = b.getX(0);
        int bHeadY = b.getY(0);

        return aHeadX == bHeadX && aHeadY == bHeadY;
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static boolean tailsAreEqual(TrainPositionOnMap a,
                                        TrainPositionOnMap b) {
        int aTailX = a.getX(a.getLength() - 1);
        int aTailY = a.getY(a.getLength() - 1);
        int bTailX = b.getX(b.getLength() - 1);
        int bTailY = b.getY(b.getLength() - 1);

        return aTailX == bTailX && aTailY == bTailY;
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static boolean aHeadEqualsBTail(TrainPositionOnMap a,
                                           TrainPositionOnMap b) {
        int aHeadX = a.getX(0);
        int aHeadY = a.getY(0);

        int bTailX = b.getX(b.getLength() - 1);
        int bTailY = b.getY(b.getLength() - 1);

        return aHeadX == bTailX && aHeadY == bTailY;
    }

    /**
     * @return
     */
    public boolean isCrashSite() {
        boolean crashSite = false;
        return crashSite;
    }

    /**
     * @return
     */
    public int getFrameCt() {
        int frameCt = 1;
        return frameCt;
    }

    @Override
    public int hashCode() {
        int result = 0;

        // TODO is there are danger of overflow here?
        for (int i = 0; i < xpoints.size(); i++) {
            result = 29 * result + xpoints.get(i);
        }

        for (int i = 0; i < ypoints.size(); i++) {
            result = 29 * result + ypoints.get(i);
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }

        if (o == this) {
            return true;
        }

        if (o instanceof TrainPositionOnMap) {
            TrainPositionOnMap other = (TrainPositionOnMap) o;
            int thisLength = this.getLength();
            int otherLength = other.getLength();

            if (thisLength == otherLength) {
                FreerailsPathIterator path1;
                FreerailsPathIterator path2;
                IntLine line1 = new IntLine();
                IntLine line2 = new IntLine();

                path1 = other.path();
                path2 = this.path();

                while (path1.hasNext() && path2.hasNext()) {
                    path1.nextSegment(line1);
                    path2.nextSegment(line2);

                    if (line1.x1 != line2.x1 || line1.y1 != line2.y1
                            || line1.x2 != line2.x2 || line1.y2 != line2.y2) {
                        return false;
                    }
                }

                return !path1.hasNext() && !path2.hasNext();
            }
            return false;
        }
        return false;
    }

    /**
     * @return
     */
    public int getLength() {
        return xpoints.size();
    }

    /**
     * @param position
     * @return
     */
    public int getX(int position) {
        return xpoints.get(position);
    }

    /**
     * @param position
     * @return
     */
    public int getY(int position) {
        return ypoints.get(position);
    }

    /**
     * @return
     */
    public FreerailsPathIterator path() {
        return new SimplePathIteratorImpl(this.xpoints, this.ypoints);
    }

    /**
     * @return
     */
    public FreerailsPathIterator reversePath() {
        int length = xpoints.size();
        int[] reversed_xpoints = new int[length];
        int[] reversed_ypoints = new int[length];

        for (int i = 0; i < length; i++) {
            reversed_xpoints[i] = xpoints.get(length - i - 1);
            reversed_ypoints[i] = ypoints.get(length - i - 1);
        }

        return new SimplePathIteratorImpl(reversed_xpoints, reversed_ypoints);
    }

    /**
     * @return
     */
    public TrainPositionOnMap reverse() {
        int length = xpoints.size();
        int[] reversed_xpoints = new int[length];
        int[] reversed_ypoints = new int[length];

        for (int i = 0; i < length; i++) {
            reversed_xpoints[i] = xpoints.get(length - i - 1);
            reversed_ypoints[i] = ypoints.get(length - i - 1);
        }

        return new TrainPositionOnMap(reversed_xpoints, reversed_ypoints,
                speed, acceleration, activity);
    }

    /**
     * @param b
     * @return
     */
    public TrainPositionOnMap addToHead(TrainPositionOnMap b) {
        TrainPositionOnMap a = this;

        return addBtoHeadOfA(b, a);
    }

    private TrainPositionOnMap addBtoHeadOfA(TrainPositionOnMap b,
                                             TrainPositionOnMap a) {
        if (aHeadEqualsBTail(a, b)) {
            int newLength = a.getLength() + b.getLength() - 2;

            int[] newXpoints = new int[newLength];
            int[] newYpoints = new int[newLength];

            int aLength = a.getLength();
            int bLength = b.getLength();

            // First copy the points from B
            for (int i = 0; i < bLength - 1; i++) {
                newXpoints[i] = b.getX(i);
                newYpoints[i] = b.getY(i);
            }

            // Second copy the points from A.
            for (int i = 1; i < aLength; i++) {
                newXpoints[i + bLength - 2] = a.getX(i);
                newYpoints[i + bLength - 2] = a.getY(i);
            }

            return new TrainPositionOnMap(newXpoints, newYpoints,
                    b.acceleration, b.speed, b.activity);
        }
        throw new IllegalArgumentException("Tried to add " + b.toString()
                + " to the head of " + a.toString());
    }

    /**
     * @param b
     * @return
     */
    public boolean canAddToHead(TrainPositionOnMap b) {
        return aHeadEqualsBTail(this, b);
    }

    /**
     * @param a
     * @return
     */
    public TrainPositionOnMap addToTail(TrainPositionOnMap a) {
        TrainPositionOnMap b = this;

        return addBtoHeadOfA(b, a);
    }

    /**
     * @param b
     * @return
     */
    public boolean canAddToTail(TrainPositionOnMap b) {
        return aHeadEqualsBTail(b, this);
    }

    /**
     * @param b
     * @return
     */
    public boolean canRemoveFromHead(TrainPositionOnMap b) {
        if (headsAreEqual(this, b)) {
            FreerailsPathIterator path = b.path();
            int i = 0;
            IntLine line = new IntLine();

            while (path.hasNext()) {
                path.nextSegment(line);

                if (this.getX(i) != line.x1 || this.getY(i) != line.y1) {
                    return false;
                }

                i++;
            }

            return true;
        }
        return false;
    }

    /**
     * @param b
     * @return
     */
    public TrainPositionOnMap removeFromTail(TrainPositionOnMap b) {
        if (tailsAreEqual(this, b)) {
            int newLength = this.getLength() - b.getLength() + 2;

            int[] newXpoints = new int[newLength];
            int[] newYpoints = new int[newLength];

            // Copy from this
            for (int i = 0; i < newLength - 1; i++) {
                newXpoints[i] = this.getX(i);
                newYpoints[i] = this.getY(i);
            }

            // Copy tail from b
            newXpoints[newLength - 1] = b.getX(0);
            newYpoints[newLength - 1] = b.getY(0);

            return new TrainPositionOnMap(newXpoints, newYpoints, speed,
                    acceleration, activity);
        }
        throw new IllegalArgumentException();
    }

    /**
     * @param b
     * @return
     */
    public boolean canRemoveFromTail(TrainPositionOnMap b) {
        if (tailsAreEqual(this, b)) {
            FreerailsPathIterator path = b.reversePath();
            int i = this.getLength() - 1;
            IntLine line = new IntLine();

            while (path.hasNext()) {
                path.nextSegment(line);

                if (this.getX(i) != line.x1 || this.getY(i) != line.y1) {
                    return false;
                }

                i--;
            }

            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TrainPosition {");

        for (int i = 0; i < xpoints.size(); i++) {
            sb.append("(");
            sb.append(xpoints.get(i));
            sb.append(", ");
            sb.append(ypoints.get(i));
            sb.append("), ");
        }

        sb.append("}");

        return sb.toString();
    }

    /**
     * @return
     */
    public double getSpeed() {
        return speed;
    }
}