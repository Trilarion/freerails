/*
 * Copyright (C) 2002 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jfreerails.controller;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.IntLine;
import junit.framework.TestCase;


/**
 * @author Luke Lindsay 30-Oct-2002
 *
 */
public class ToAndFroPathIteratorTest extends TestCase {
    /**
     * Constructor for ToAndFroPathIteratorTest.
     * @param arg0
     */
    public ToAndFroPathIteratorTest(String arg0) {
        super(arg0);
    }

    public void testNextSegment() {
        List l = new ArrayList();
        IntLine line = new IntLine();

        l.add(new Point(0, 1));
        l.add(new Point(10, 11));
        l.add(new Point(20, 22));

        FreerailsPathIterator it = new ToAndFroPathIterator(l);

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

    private void assertLineEquals(int x1, int y1, int x2, int y2, IntLine line) {
        assertEquals(x1, line.x1);
        assertEquals(x2, line.x2);
        assertEquals(y1, line.y1);
        assertEquals(y2, line.y2);
    }
}