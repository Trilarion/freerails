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

import freerails.util.Point2D;
import freerails.world.terrain.TileTransition;
import junit.framework.TestCase;

/**
 * Test for TrackSection.
 */
public class TrackSectionTest extends TestCase {

    /**
     *
     */
    public void testEquals() {
        TrackSection sectionA = new TrackSection(TileTransition.EAST, new Point2D(10, 5));
        TrackSection sectionB = new TrackSection(TileTransition.WEST, new Point2D(11, 5));

        // should all be equal
        assertEquals(sectionA, sectionA);
        assertEquals(sectionB, sectionB);
        assertEquals(sectionA, sectionB);
        assertEquals(sectionB, sectionA);

    }

}
