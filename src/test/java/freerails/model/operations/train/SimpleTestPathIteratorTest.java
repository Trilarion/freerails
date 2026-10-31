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

package freerails.model.operations.train;

import freerails.util.Segment;
import freerails.util.TestUtils;
import freerails.model.track.PathIterator;
import freerails.model.track.SimplePathIteratorImpl;
import freerails.util.Vector2D;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 * Tests simple test path iterator behavior.
 */
public class SimpleTestPathIteratorTest extends TestCase {

    /**
     * Verifies that the iterator reports a segment before traversal and no segment after traversal.
     */
    public void testHasNext() {
        Vector2D[] points = {Vector2D.ZERO, new Vector2D(100, 0)};
        PathIterator pathIterator = new SimplePathIteratorImpl(Arrays.asList(points));
        assertTrue(pathIterator.hasNext());
        pathIterator.nextSegment();
        assertTrue(!pathIterator.hasNext());
    }

    /**
     * Tests next segment behavior.
     */
    public void testNextSegment() {
        Vector2D[] points = {new Vector2D(1, 4), new Vector2D(2, 5), new Vector2D(3, 6)};

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