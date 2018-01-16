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

package freerails.controller;

import freerails.util.LineSegment;
import freerails.world.track.PathIterator;
import junit.framework.TestCase;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Test for ToAndFroPathIteratorTest.
 */
public class ToAndFroPathIteratorTest extends TestCase {

    /**
     * @param arg0
     */
    public ToAndFroPathIteratorTest(String arg0) {
        super(arg0);
    }

    /**
     *
     */
    public void testNextSegment() {
        List<Point> l = new ArrayList<>();
        LineSegment line = new LineSegment();

        l.add(new Point(0, 1));
        l.add(new Point(10, 11));
        l.add(new Point(20, 22));

        PathIterator it = new ToAndFroPathIterator(l);

        assertTrue(it.hasNext());
        it.nextSegment(line);
        assertLineEquals(0, 1, 10, 11, line);

        assertTrue(it.hasNext());
        it.nextSegment(line);
        assertLineEquals(10, 11, 20, 22, line);

        assertTrue(it.hasNext());
        it.nextSegment(line);
        assertLineEquals(20, 22, 10, 11, line);

        assertTrue(it.hasNext());
        it.nextSegment(line);
        assertLineEquals(10, 11, 0, 1, line);

        assertTrue(it.hasNext());
        it.nextSegment(line);
        assertLineEquals(0, 1, 10, 11, line);

        assertTrue(it.hasNext());
        it.nextSegment(line);
        assertLineEquals(10, 11, 20, 22, line);
    }

    private void assertLineEquals(int x1, int y1, int x2, int y2, LineSegment line) {
        assertEquals(x1, line.getX1());
        assertEquals(x2, line.getX2());
        assertEquals(y1, line.getY1());
        assertEquals(y2, line.getY2());
    }
}