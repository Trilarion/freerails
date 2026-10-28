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
 * <table>
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

    /**
     * Creates an immutable train position and its motion state.
     *
     * @param points ordered points from train head to tail
     * @param speed speed at this position
     * @param acceleration acceleration at this position
     * @param activity train lifecycle state
     */
    private TrainPositionOnMap(List<Vec2D> points, double speed, double acceleration, TrainState activity) {
        this.points = points;
        this.acceleration = acceleration;
        this.speed = speed;
        this.activity = activity;
    }

    /**
     * Creates a stationary position from ordered head-to-tail points.
     *
     * @param points train points in head-to-tail order
     * @return position with zero speed and acceleration and state {@link TrainState#READY}
     */
    public static TrainPositionOnMap createInstance(List<Vec2D> points) {
        return new TrainPositionOnMap(points, 0.0d, 0.0d, TrainState.READY);
    }

    /**
     * Creates a stationary train position following the path's direction.
     *
     * @param path path iterator containing at least one segment
     * @return position assembled from the path's points
     * @throws IllegalStateException if more than 10,000 points are encountered
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
     * @param path path iterator and expected number of points
     * @param speed speed at the represented position
     * @param acceleration acceleration at the represented position
     * @param activity train lifecycle state
     * @return position with the path points in the opposite order to the iterator traversal
     * @throws IllegalStateException if the point count exceeds 10,000 or the iterator
     *         yields fewer segments than the supplied count
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
     * @param a first train position
     * @param b second train position
     * @return {@code true} if their first (head) points are equal
     */
    private static boolean headsAreEqual(TrainPositionOnMap a, TrainPositionOnMap b) {
        Vec2D aHead = a.getP(0);
        Vec2D bHead = b.getP(0);
        return aHead.equals(bHead);
    }

    /**
     * @param a first train position
     * @param b second train position
     * @return {@code true} if their last (tail) points are equal
     */
    private static boolean tailsAreEqual(TrainPositionOnMap a, TrainPositionOnMap b) {
        Vec2D aTail = a.getP(a.getLength() - 1);
        Vec2D bTail = b.getP(b.getLength() - 1);
        return aTail.equals(bTail);
    }

    /**
     * @param a first train position
     * @param b second train position
     * @return {@code true} if the head of {@code a} meets the tail of {@code b}
     */
    private static boolean aHeadEqualsBTail(TrainPositionOnMap a, TrainPositionOnMap b) {
        Vec2D aHead = a.getP(0);
        Vec2D bTail = b.getP(b.getLength() - 1);
        return aHead.equals(bTail);
    }

    /** Returns whether the current location is a crash site; this implementation always returns {@code false}. */
    public static boolean isCrashSite() {
        return false;
    }

    /** Returns the number of animation frames associated with a position; currently always {@code 1}. */
    public static int getFrameCt() {
        return 1;
    }

    /**
     * Joins {@code b} to the head of {@code a}, removing the shared endpoint.
     *
     * @param a position receiving points at its head
     * @param b position joined to its head
     * @return the combined position, carrying motion state from {@code b}
     * @throws IllegalArgumentException if the head of {@code a} does not meet the tail of {@code b}
     */
    private static TrainPositionOnMap addBtoHeadOfA(TrainPositionOnMap a, TrainPositionOnMap b) {
        if (!aHeadEqualsBTail(a, b)) {
            throw new IllegalArgumentException("Tried to add " + b + " to the head of " + a);
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

    /** Returns a hash code based on this position's ordered points. */
    @Override
    public int hashCode() {
        return points.hashCode();
    }

    /** Compares the ordered path points; speed, acceleration, and state are not part of equality. */
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

    /** Returns the number of points from the head through the tail. */
    public int getLength() {
        return points.size();
    }

    /**
     * Returns the point at the specified head-to-tail index.
     *
     * @param position zero-based point index
     * @return point at that index
     * @throws IndexOutOfBoundsException if the index is outside the points
     */
    public Vec2D getP(int position) {
        return points.get(position);
    }

    /** Returns an iterator over segments from the head toward the tail. */
    public PathIterator path() {
        return new SimplePathIteratorImpl(points);
    }

    /** Returns an iterator over segments from the tail toward the head. */
    public PathIterator reversePath() {
        List<Vec2D> list = new ArrayList<>(points); // clone() not available for lists
        Collections.reverse(list); // reverse the list in place
        return new SimplePathIteratorImpl(list);
    }

    /** Returns a new position with its point order reversed. */
    public TrainPositionOnMap reverse() {
        List<Vec2D> list = new ArrayList<>(points); // clone() not available for lists
        Collections.reverse(list); // reverse the list in place
        return new TrainPositionOnMap(list, speed, acceleration, activity);
    }

    /**
     * Joins another position to this position's head.
     *
     * @param b position whose tail must meet this position's head
     * @return combined position
     * @throws IllegalArgumentException if the positions do not meet at the required endpoints
     */
    public TrainPositionOnMap addToHead(TrainPositionOnMap b) {
        return addBtoHeadOfA(this, b);
    }

    /**
     * Tests whether another position can be joined to this position's head.
     *
     * @param b candidate position to join
     * @return {@code true} if its tail equals this position's head
     */
    public boolean canAddToHead(TrainPositionOnMap b) {
        return aHeadEqualsBTail(this, b);
    }

    /**
     * Joins this position to another position's tail.
     *
     * @param a position whose head must meet this position's tail
     * @return combined position
     * @throws IllegalArgumentException if the positions do not meet at the required endpoints
     */
    public TrainPositionOnMap addToTail(TrainPositionOnMap a) {
        return addBtoHeadOfA(a, this);
    }

    /**
     * Tests whether this position can be joined to another position's tail.
     *
     * @param b candidate position to join
     * @return {@code true} if this position's head equals the candidate's tail
     */
    public boolean canAddToTail(TrainPositionOnMap b) {
        return aHeadEqualsBTail(b, this);
    }

    /**
     * Tests whether the specified position matches this position's head section.
     *
     * @param b position to remove from the head
     * @return {@code true} if the shared head and path points match
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
     * Removes the specified tail section and returns the remaining position.
     *
     * @param b tail section to remove
     * @return position ending at the start of {@code b}
     * @throws IllegalArgumentException if the tails do not coincide
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
     * Tests whether the specified position matches this position's tail section.
     *
     * @param b position to remove from the tail
     * @return {@code true} if the shared tail and path points match
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

    /** Returns a textual representation of the ordered train points. */
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