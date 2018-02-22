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
import freerails.util.TestUtils;
import freerails.model.track.PathIterator;
import freerails.model.track.TestPathIterator;
import freerails.model.track.SimplePathIteratorImpl;
import junit.framework.TestCase;

import java.awt.*;
import java.util.ArrayList;

/**
 * Test for PathWalkerImpl.
 */
public class PathWalkerImplTest extends TestCase {

    // TODO very volatile execution order here important, refactor
    private PathWalker pathWalker;

    /**
     *
     */
    private void setup() {
        Integer[] xpoints = {0, 100, 100};
        Integer[] ypoints = {0, 0, 100};
        PathIterator it = new SimplePathIteratorImpl(xpoints, ypoints);
        pathWalker = new PathWalkerImpl(it);
    }

    /*
     * Test for boolean canStepForward()
     */
    public void testCanStepForward() {
        setup();

        assertTrue(pathWalker.canStepForward());
        pathWalker.stepForward(500); // The path length is 200;
        moveToNextLimit();
        assertTrue(!pathWalker.canStepForward());

        setup();
        assertTrue(pathWalker.canStepForward());
        pathWalker.stepForward(10);
        assertTrue(pathWalker.canStepForward());
        LineSegment line = new LineSegment();
        assertTrue(pathWalker.hasNext());
        pathWalker.nextSegment(line);
        TestUtils.assertLineSegmentEquals(0, 0, 10, 0, line);
        assertTrue(!pathWalker.hasNext());
        assertTrue(pathWalker.canStepForward());
        pathWalker.stepForward(500); // The path length is 200;
        assertTrue(pathWalker.hasNext());
        pathWalker.nextSegment(line);
        TestUtils.assertLineSegmentEquals(10, 0, 100, 0, line);
        assertTrue(pathWalker.hasNext());
        pathWalker.nextSegment(line);
        TestUtils.assertLineSegmentEquals(100, 0, 100, 100, line);
        assertTrue(!pathWalker.canStepForward());
    }

    private void moveToNextLimit() {
        LineSegment line = new LineSegment();
        while (pathWalker.hasNext()) {
            pathWalker.nextSegment(line);
        }
    }

    /**
     *
     */
    public void testHasNext() {
        LineSegment line = new LineSegment();

        setup();
        assertTrue(!pathWalker.hasNext());
        pathWalker.stepForward(10);
        assertTrue(pathWalker.hasNext());
        pathWalker.nextSegment(line);
        TestUtils.assertLineSegmentEquals(0, 0, 10, 0, line);
        assertTrue(!pathWalker.hasNext());

        setup();
        assertTrue(!pathWalker.hasNext());
        pathWalker.stepForward(110);
        assertTrue(pathWalker.hasNext());
        line = new LineSegment();
        pathWalker.nextSegment(line);
        TestUtils.assertLineSegmentEquals(0, 0, 100, 0, line);
        assertTrue(pathWalker.hasNext());
        pathWalker.nextSegment(line);
        TestUtils.assertLineSegmentEquals(100, 0, 100, 10, line);
        assertTrue(!pathWalker.hasNext());

        /*
         * Now test with underlying pathIterators with few elements.
         */
        ArrayList<Point> points = new ArrayList<>();
        assertHasNextEqualsFalse(points);
        points = new ArrayList<>();
        points.add(new Point(0, 0));
        assertHasNextEqualsFalse(points);
        points = new ArrayList<>();
        points.add(new Point(0, 0));
        points.add(new Point(100, 0));
        PathIterator it2 = TestPathIterator.forwardsIterator(points);
        assertTrue(it2.hasNext());
        pathWalker = new PathWalkerImpl(it2);
        assertTrue(!pathWalker.hasNext());
        pathWalker.stepForward(1000);
        assertTrue(pathWalker.hasNext());
        pathWalker.nextSegment(line);
        assertTrue(!pathWalker.hasNext());
    }

    private void assertHasNextEqualsFalse(ArrayList<Point> points) {
        PathIterator pathIterator = TestPathIterator.forwardsIterator(points);
        assertTrue(!pathIterator.hasNext());
        pathWalker = new PathWalkerImpl(pathIterator);
        pathWalker.stepForward(100);
        assertTrue(!pathWalker.hasNext());
    }
}