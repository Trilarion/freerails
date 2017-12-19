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

package freerails.world.track;

import freerails.world.common.ImPoint;
import freerails.world.common.Step;
import junit.framework.TestCase;

/**
 *
 */
public class TrackSectionTest extends TestCase {

    /**
     *
     */
    public void testEqualsObject() {
        TrackSection a = new TrackSection(Step.EAST, new ImPoint(10, 5));
        TrackSection b = new TrackSection(Step.WEST, new ImPoint(11, 5));
        assertEquals(a, a);
        assertEquals(b, b);
        assertEquals(a, b);
        assertEquals(b, a);

    }

}
