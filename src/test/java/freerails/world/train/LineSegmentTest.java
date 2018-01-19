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
import junit.framework.TestCase;

/**
 *
 */
public class LineSegmentTest extends TestCase {

    /**
     * @param arg0
     */
    public LineSegmentTest(String arg0) {
        super(arg0);
    }

    /**
     *
     */
    public void testGetLength() {
        LineSegment line = new LineSegment(0, 0, 100, 0);
        assertEquals(100, line.getLength(), 0.1);
    }
}