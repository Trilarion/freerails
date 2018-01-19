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
import freerails.world.track.SimplePathIteratorImpl;
import junit.framework.TestCase;

/**
 *
 */
public class SimplePathIteratorImplTest extends TestCase {

    /**
     * @param arg0
     */
    public SimplePathIteratorImplTest(String arg0) {
        super(arg0);
    }

    /**
     *
     */
    public void testHasNext() {
        Integer[] xpoints = {0, 100};
        Integer[] ypoints = {0, 0};

        PathIterator it = new SimplePathIteratorImpl(xpoints, ypoints);
        assertTrue(it.hasNext());
        it.nextSegment(new LineSegment(0, 0, 0, 0));
        assertTrue(!it.hasNext());
    }

    /**
     *
     */
    public void testNextSegment() {
        Integer[] xpoints = {1, 2, 3};
        Integer[] ypoints = {4, 5, 6};

        PathIterator it = new SimplePathIteratorImpl(xpoints, ypoints);
        assertTrue(it.hasNext());

        LineSegment line = new LineSegment(0, 0, 0, 0);
        it.nextSegment(line);
        assertLineEquals(1, 4, 2, 5, line);
        assertTrue(it.hasNext());
        it.nextSegment(line);
        assertLineEquals(2, 5, 3, 6, line);
        assertTrue(!it.hasNext());
    }

    private void assertLineEquals(int x1, int y1, int x2, int y2, LineSegment line) {
        assertEquals(x1, line.getX1());
        assertEquals(x2, line.getX2());
        assertEquals(y1, line.getY1());
        assertEquals(y2, line.getY2());
    }
}