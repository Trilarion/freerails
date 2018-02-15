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
import freerails.model.track.SimplePathIteratorImpl;
import junit.framework.TestCase;

/**
 *
 */
public class SimplePathIteratorImplTest extends TestCase {

    /**
     *
     */
    public void testHasNext() {
        Integer[] xpoints = {0, 100};
        Integer[] ypoints = {0, 0};

        PathIterator pathIterator = new SimplePathIteratorImpl(xpoints, ypoints);
        assertTrue(pathIterator.hasNext());
        pathIterator.nextSegment(new LineSegment(0, 0, 0, 0));
        assertTrue(!pathIterator.hasNext());
    }

    /**
     *
     */
    public void testNextSegment() {
        Integer[] xpoints = {1, 2, 3};
        Integer[] ypoints = {4, 5, 6};

        PathIterator pathIterator = new SimplePathIteratorImpl(xpoints, ypoints);
        assertTrue(pathIterator.hasNext());

        LineSegment segment = new LineSegment(0, 0, 0, 0);
        pathIterator.nextSegment(segment);
        TestUtils.assertLineSegmentEquals(1, 4, 2, 5, segment);
        assertTrue(pathIterator.hasNext());
        pathIterator.nextSegment(segment);
        TestUtils.assertLineSegmentEquals(2, 5, 3, 6, segment);
        assertTrue(!pathIterator.hasNext());
    }

}