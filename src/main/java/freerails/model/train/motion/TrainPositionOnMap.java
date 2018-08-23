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

package freerails.model.train.motion;

import freerails.model.train.TrainState;
import freerails.util.Segment;
import freerails.util.Pair;
import freerails.model.track.PathIterator;
import freerails.model.track.SimplePathIteratorImpl;
import freerails.util.Vec2D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// TODO does not use any Train motion
/**
 * This <b>immutable</b> class represents the position of a train as a String
 * of points. There must be at least two points. The first point is the position
 * of the front of the train; the last point is the position of the end of the
 * train. Any intermediate points are positions of 'kinks' in the track.
 *
 * Coordinates are expressed in display coordinates relative to the map origin
 * (as opposed to map squares).
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

    private static final long serialVersionUID = 3979269144611010865L;
    private final List<Vec2D> points;
    private final double speed, acceleration;
    private final TrainState activity;

    // TODO use Vec2D instead of xs, ys
    private TrainPositionOnMap(List<Vec2D> points, double speed, double acceleration, TrainState activity) {
        this.points = points;
        this.acceleration = acceleration;
        this.speed = speed;
        this.activity = activity;
    }

    /**
     * @param points
     * @return
     */
    public static TrainPositionOnMap createInstance(List<Vec2D> points) {
        return new TrainPositionOnMap(points, 0.0d, 0.0d, TrainState.READY);
    }

    /**
     * @param path
     * @return
     */
    public static TrainPositionOnMap createInSameDirectionAsPath(PathIterator path) {
        List<Vec2D> points = new ArrayList<>();
        Segment line = null;
        int i = 0;

        while (path.hasNext()) {
            line = path.nextSegment();
            points.add(i, line.getA());
            i++;

            if (i > 10000) {
                throw new IllegalStateException("The TrainPosition has more than 10,000 points, which suggests that something is wrong.");
            }
        }

        points.add(i, line.getB());

        return new TrainPositionOnMap(points, 0.0d, 0.0d, TrainState.READY);
    }

    /**
     * @param path
     * @param speed
     * @param acceleration
     * @param activity
     * @return
     */
    public static TrainPositionOnMap createInSameDirectionAsPathReversed(Pair<PathIterator, Integer> path, double speed, double acceleration, TrainState activity) {

        Segment line = null;
        PathIterator pathIt = path.getA();
        int pathSize = path.getB();

        if (pathSize > 10000) {
            throw new IllegalStateException("The TrainPosition has more than 10,000 points, which suggests that something is wrong.");
        }
        Vec2D[] points = new Vec2D[pathSize];

        for (int i = pathSize - 1; i > 0; i--) {
            if (!pathIt.hasNext()) {
                throw new IllegalStateException("Programming error at:" + i + " from:" + pathSize);
            }
            line = pathIt.nextSegment();
            points[i] = line.getA();
        }

        points[0] = line.getB();

        return new TrainPositionOnMap(Arrays.asList(points), speed, acceleration, activity);
    }

    /**
     * @param a
     * @param b
     * @return
     */
    private static boolean headsAreEqual(TrainPositionOnMap a, TrainPositionOnMap b) {
        Vec2D aHead = a.getP(0);
        Vec2D bHead = b.getP(0);
        return aHead.equals(bHead);
    }

    /**
     * @param a
     * @param b
     * @return
     */
    private static boolean tailsAreEqual(TrainPositionOnMap a, TrainPositionOnMap b) {
        Vec2D aTail = a.getP(a.getLength() - 1);
        Vec2D bTail = b.getP(b.getLength() - 1);
        return aTail.equals(bTail);
    }

    /**
     * @param a
     * @param b
     * @return
     */
    private static boolean aHeadEqualsBTail(TrainPositionOnMap a, TrainPositionOnMap b) {
        Vec2D aHead = a.getP(0);
        Vec2D bTail = b.getP(b.getLength() - 1);
        return aHead.equals(bTail);
    }

    /**
     * @return
     */
    public static boolean isCrashSite() {
        return false;
    }

    /**
     * @return
     */
    public static int getFrameCt() {
        return 1;
    }

    private static TrainPositionOnMap addBtoHeadOfA(TrainPositionOnMap a, TrainPositionOnMap b) {
        if (!aHeadEqualsBTail(a, b)) {
            throw new IllegalArgumentException("Tried to add " + b.toString() + " to the head of " + a.toString());
        }
        int newLength = a.getLength() + b.getLength() - 2;

        Vec2D[] newPoints = new Vec2D[newLength];

        int aLength = a.getLength();
        int bLength = b.getLength();

        // First copy the points from B
        for (int i = 0; i < bLength - 1; i++) {
            newPoints[i] = b.getP(i);
        }

        // Second copy the points from A.
        for (int i = 1; i < aLength; i++) {
            newPoints[i + bLength - 2] = a.getP(i);
        }

        return new TrainPositionOnMap(Arrays.asList(newPoints), b.acceleration, b.speed, b.activity);
    }

    @Override
    public int hashCode() {
        return points.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof TrainPositionOnMap) {
            TrainPositionOnMap other = (TrainPositionOnMap) obj;
            int thisLength = getLength();
            int otherLength = other.getLength();

            if (thisLength == otherLength) {
                PathIterator path1;
                PathIterator path2;
                Segment line1 = null;
                Segment line2 = null;

                path1 = other.path();
                path2 = path();

                while (path1.hasNext() && path2.hasNext()) {
                    line1 = path1.nextSegment();
                    line2 = path2.nextSegment();

                    if (!line1.equals(line2)) {
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
        return points.size();
    }

    public Vec2D getP(int position) {
        return points.get(position);
    }

    /**
     * @return
     */
    public PathIterator path() {
        return new SimplePathIteratorImpl(points);
    }

    /**
     * @return
     */
    public PathIterator reversePath() {
        List<Vec2D> list = new ArrayList<>(points); // clone() not available for lists
        Collections.reverse(list); // reverse the list in place
        return new SimplePathIteratorImpl(list);
    }

    /**
     * @return
     */
    public TrainPositionOnMap reverse() {
        List<Vec2D> list = new ArrayList<>(points); // clone() not available for lists
        Collections.reverse(list); // reverse the list in place
        return new TrainPositionOnMap(list, speed, acceleration, activity);
    }

    /**
     * @param b
     * @return
     */
    public TrainPositionOnMap addToHead(TrainPositionOnMap b) {
        return addBtoHeadOfA(this, b);
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
        return addBtoHeadOfA(a, this);
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
            PathIterator path = b.path();
            int i = 0;
            Segment line = null;

            while (path.hasNext()) {
                line = path.nextSegment();

                if (!getP(i).equals(line.getA())) {
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
            int newLength = getLength() - b.getLength() + 2;

            Vec2D[] newPoints = new Vec2D[newLength];

            // Copy from this
            for (int i = 0; i < newLength - 1; i++) {
                newPoints[i] = getP(i);
            }

            // Copy tail from b
            newPoints[newLength - 1] = b.getP(0);

            return new TrainPositionOnMap(Arrays.asList(newPoints), speed, acceleration, activity);
        }
        throw new IllegalArgumentException();
    }

    /**
     * @param b
     * @return
     */
    public boolean canRemoveFromTail(TrainPositionOnMap b) {
        if (tailsAreEqual(this, b)) {
            PathIterator path = b.reversePath();
            int i = getLength() - 1;

            while (path.hasNext()) {
                Segment line = path.nextSegment();

                if (!getP(i).equals(line.getA())) {
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

        for (Vec2D point : points) {
            sb.append('(');
            sb.append(point);
            sb.append("), ");
        }

        sb.append('}');

        return sb.toString();
    }
}