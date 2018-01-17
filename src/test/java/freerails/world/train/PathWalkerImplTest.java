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

import freerails.util.LineSegment;
import freerails.world.track.PathIterator;
import freerails.world.track.PathIteratorImpl;
import freerails.world.track.SimplePathIteratorImpl;
import junit.framework.TestCase;

import java.awt.*;
import java.util.ArrayList;

/**
 * Test.
 */
public class PathWalkerImplTest extends TestCase {

    private PathWalker pw;

    /**
     * @param arg0
     */
    public PathWalkerImplTest(String arg0) {
        super(arg0);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(PathWalkerImplTest.class);
    }

    /*
     * Test for boolean canStepForward()
     */

    /**
     *
     */

    public void testCanStepForward() {
        setup();

        assertTrue(pw.canStepForward());
        pw.stepForward(500); // The path length is 200;
        moveToNextLimit();

        assertTrue(!pw.canStepForward());

        setup();
        assertTrue(pw.canStepForward());
        pw.stepForward(10);
        assertTrue(pw.canStepForward());

        LineSegment line = new LineSegment();
        assertTrue(pw.hasNext());
        pw.nextSegment(line);
        assertLineEquals(0, 0, 10, 0, line);
        assertTrue(!pw.hasNext());
        assertTrue(pw.canStepForward());

        pw.stepForward(500); // The path length is 200;
        assertTrue(pw.hasNext());
        pw.nextSegment(line);
        assertLineEquals(10, 0, 100, 0, line);
        assertTrue(pw.hasNext());
        pw.nextSegment(line);
        assertLineEquals(100, 0, 100, 100, line);

        assertTrue(!pw.canStepForward());
    }

    private void moveToNextLimit() {
        LineSegment line = new LineSegment();

        while (pw.hasNext()) {
            pw.nextSegment(line);
        }
    }

    /**
     *
     */
    public void testHasNext() {
        LineSegment line = new LineSegment();

        setup();
        assertTrue(!pw.hasNext());
        pw.stepForward(10);
        assertTrue(pw.hasNext());

        pw.nextSegment(line);
        assertLineEquals(0, 0, 10, 0, line);
        assertTrue(!pw.hasNext());

        setup();
        assertTrue(!pw.hasNext());
        pw.stepForward(110);
        assertTrue(pw.hasNext());
        line = new LineSegment();
        pw.nextSegment(line);
        assertLineEquals(0, 0, 100, 0, line);
        assertTrue(pw.hasNext());
        pw.nextSegment(line);
        assertLineEquals(100, 0, 100, 10, line);
        assertTrue(!pw.hasNext());

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

        PathIterator it2 = PathIteratorImpl
                .forwardsIterator(points);
        assertTrue(it2.hasNext());
        pw = new PathWalkerImpl(it2);
        assertTrue(!pw.hasNext());
        pw.stepForward(1000);
        assertTrue(pw.hasNext());
        pw.nextSegment(line);
        assertTrue(!pw.hasNext());
    }

    private void assertHasNextEqualsFalse(ArrayList<Point> points) {
        PathIterator it2 = PathIteratorImpl
                .forwardsIterator(points);

        assertTrue(!it2.hasNext());
        pw = new PathWalkerImpl(it2);
        pw.stepForward(100);
        assertTrue(!pw.hasNext());
    }

    /**
     *
     */
    private void setup() {
        Integer[] xpoints = {0, 100, 100};
        Integer[] ypoints = {0, 0, 100};
        PathIterator it = new SimplePathIteratorImpl(xpoints, ypoints);
        pw = new PathWalkerImpl(it);
    }

    private void assertLineEquals(int x1, int y1, int x2, int y2, LineSegment line) {
        assertEquals(x1, line.getX1());
        assertEquals(x2, line.getX2());
        assertEquals(y1, line.getY1());
        assertEquals(y2, line.getY2());
    }
}