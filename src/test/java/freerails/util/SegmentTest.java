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
 *
 */
public class SegmentTest extends TestCase {

    /**
     *
     */
    public void testGetLength() {
        Segment line = new Segment(Vec2D.ZERO, new Vec2D(100, 0));
        assertEquals(100, line.getLength(), 0.1);
    }

    public void testLengthDoesNotOverflowForExtremeCoordinates() {
        Segment line = new Segment(new Vec2D(Integer.MIN_VALUE, Integer.MIN_VALUE),
                new Vec2D(Integer.MAX_VALUE, Integer.MAX_VALUE));

        assertEquals(6_074_000_998.537885, line.getLength(), 0.001);
    }

    public void testEndpointsMustNotBeNull() {
        try {
            new Segment(null, Vec2D.ZERO);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
        }
    }
}