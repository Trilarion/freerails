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
import freerails.model.track.SimplePathIteratorImpl;
import freerails.util.Vec2D;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 *
 */
public class SimpleTestPathIteratorTest extends TestCase {

    /**
     *
     */
    public void testHasNext() {
        Vec2D[] points = {Vec2D.ZERO, new Vec2D(100, 0)};
        PathIterator pathIterator = new SimplePathIteratorImpl(Arrays.asList(points));
        assertTrue(pathIterator.hasNext());
        pathIterator.nextSegment();
        assertTrue(!pathIterator.hasNext());
    }

    /**
     *
     */
    public void testNextSegment() {
        Vec2D[] points = {new Vec2D(1, 4), new Vec2D(2, 5), new Vec2D(3, 6)};

        PathIterator pathIterator = new SimplePathIteratorImpl(Arrays.asList(points));
        assertTrue(pathIterator.hasNext());

        Segment segment = pathIterator.nextSegment();
        TestUtils.assertLineSegmentEquals(points[0], points[1], segment);
        assertTrue(pathIterator.hasNext());
        segment = pathIterator.nextSegment();
        TestUtils.assertLineSegmentEquals(points[1], points[2], segment);
        assertTrue(!pathIterator.hasNext());
    }

}