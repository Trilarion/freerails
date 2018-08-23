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
import freerails.util.TestUtils;
import freerails.model.track.PathIterator;
import freerails.model.track.TestPathIterator;
import freerails.model.track.SimplePathIteratorImpl;
import freerails.util.Vec2D;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;

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
        Vec2D[] points = {Vec2D.ZERO, new Vec2D(100, 0), new Vec2D(100, 100)};
        PathIterator it = new SimplePathIteratorImpl(Arrays.asList(points));
        pathWalker = new PathWalkerImpl(it);
    }

    /*
     * Test for boolean canStepForward()
     */
    public void testCanStepForward() {
        setup();

        assertTrue(pathWalker.canStepForward());
        pathWalker.stepForward(500); // The path length is 200;
        while (pathWalker.hasNext()) {
            pathWalker.nextSegment();
        }
        assertTrue(!pathWalker.canStepForward());

        setup();
        assertTrue(pathWalker.canStepForward());
        pathWalker.stepForward(10);
        assertTrue(pathWalker.canStepForward());
        Segment line = null;
        assertTrue(pathWalker.hasNext());
        line = pathWalker.nextSegment();
        TestUtils.assertLineSegmentEquals(Vec2D.ZERO, new Vec2D(10, 0), line);
        assertTrue(!pathWalker.hasNext());
        assertTrue(pathWalker.canStepForward());
        pathWalker.stepForward(500); // The path length is 200;
        assertTrue(pathWalker.hasNext());
        line = pathWalker.nextSegment();
        TestUtils.assertLineSegmentEquals(new Vec2D(10, 0), new Vec2D(100, 0), line);
        assertTrue(pathWalker.hasNext());
        line = pathWalker.nextSegment();
        TestUtils.assertLineSegmentEquals(new Vec2D(100, 0), new Vec2D(100, 100), line);
        assertTrue(!pathWalker.canStepForward());
    }

    /**
     *
     */
    public void testHasNext() {
        Segment line = null;

        setup();
        assertTrue(!pathWalker.hasNext());
        pathWalker.stepForward(10);
        assertTrue(pathWalker.hasNext());
        line = pathWalker.nextSegment();
        TestUtils.assertLineSegmentEquals(Vec2D.ZERO, new Vec2D(10, 0), line);
        assertTrue(!pathWalker.hasNext());

        setup();
        assertTrue(!pathWalker.hasNext());
        pathWalker.stepForward(110);
        assertTrue(pathWalker.hasNext());
        line = pathWalker.nextSegment();
        TestUtils.assertLineSegmentEquals(Vec2D.ZERO, new Vec2D(100, 0), line);
        assertTrue(pathWalker.hasNext());
        line = pathWalker.nextSegment();
        TestUtils.assertLineSegmentEquals(new Vec2D(100, 0), new Vec2D(100, 10), line);
        assertTrue(!pathWalker.hasNext());

        /*
         * Now test with underlying pathIterators with few elements.
         */
        ArrayList<Vec2D> points = new ArrayList<>();
        assertHasNextEqualsFalse(points);
        points = new ArrayList<>();
        points.add(new Vec2D(0, 0));
        assertHasNextEqualsFalse(points);
        points = new ArrayList<>();
        points.add(new Vec2D(0, 0));
        points.add(new Vec2D(100, 0));
        PathIterator it2 = TestPathIterator.forwardsIterator(points);
        assertTrue(it2.hasNext());
        pathWalker = new PathWalkerImpl(it2);
        assertTrue(!pathWalker.hasNext());
        pathWalker.stepForward(1000);
        assertTrue(pathWalker.hasNext());
        line = pathWalker.nextSegment();
        assertTrue(!pathWalker.hasNext());
    }

    private void assertHasNextEqualsFalse(ArrayList<Vec2D> points) {
        PathIterator pathIterator = TestPathIterator.forwardsIterator(points);
        assertTrue(!pathIterator.hasNext());
        pathWalker = new PathWalkerImpl(pathIterator);
        pathWalker.stepForward(100);
        assertTrue(!pathWalker.hasNext());
    }
}