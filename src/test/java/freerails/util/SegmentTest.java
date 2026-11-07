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

package freerails.util;

import junit.framework.TestCase;

/**
 * Tests segment behavior.
 */
public class SegmentTest extends TestCase {

    /**
     * Tests get length behavior.
     */
    public void testGetLength() {
        Segment line = new Segment(IntPoint2D.ZERO, new IntPoint2D(100, 0));
        assertEquals(100, line.getLength(), 0.1);
    }

    /**
     * Tests length does not overflow for extreme coordinates behavior.
     */
    public void testLengthDoesNotOverflowForExtremeCoordinates() {
        Segment line = new Segment(new IntPoint2D(Integer.MIN_VALUE, Integer.MIN_VALUE),
                new IntPoint2D(Integer.MAX_VALUE, Integer.MAX_VALUE));

        assertEquals(6_074_000_998.537885, line.getLength(), 0.001);
    }

    /**
     * Tests endpoints must not be null behavior.
     */
    public void testEndpointsMustNotBeNull() {
        try {
            new Segment(null, IntPoint2D.ZERO);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
        }
        try {
            new Segment(IntPoint2D.ZERO, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
        }
    }

    /**
     * Tests equality hash code and endpoint accessors behavior.
     */
    public void testEqualityHashCodeAndEndpointAccessors() {
        IntPoint2D start = new IntPoint2D(1, 2);
        IntPoint2D end = new IntPoint2D(4, 6);
        Segment segment = new Segment(start, end);
        Segment equal = new Segment(new IntPoint2D(1, 2), new IntPoint2D(4, 6));

        assertSame(start, segment.a());
        assertSame(end, segment.b());
        assertEquals(segment, segment);
        assertEquals(segment, equal);
        assertEquals(segment.hashCode(), equal.hashCode());
        assertFalse(segment.equals(null));
        assertFalse(segment.equals("segment"));
        assertFalse(segment.equals(new Segment(end, start)));
        assertEquals("((1, 2), (4, 6))", segment.toString());
    }
}