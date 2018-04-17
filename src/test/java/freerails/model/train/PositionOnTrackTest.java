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

import freerails.util.Vec2D;
import freerails.model.terrain.TileTransition;
import junit.framework.TestCase;

/**
 * Test for PositionOnTrack.
 */
public class PositionOnTrackTest extends TestCase {

    /**
     *
     */
    public void testValidation() {
        assertTrue(PositionOnTrack.MAX_COORDINATE < 70000);
        assertTrue(PositionOnTrack.MAX_COORDINATE > 10000);
        assertEquals(PositionOnTrack.MAX_DIRECTION, 7);

        assertNoException(0, 0, TileTransition.EAST);
        assertNoException(PositionOnTrack.MAX_COORDINATE, PositionOnTrack.MAX_COORDINATE, TileTransition.NORTH_WEST);

        assertException(-1, 0, TileTransition.EAST);
        assertException(0, -1, TileTransition.EAST);

        assertException(PositionOnTrack.MAX_COORDINATE + 1, PositionOnTrack.MAX_COORDINATE, TileTransition.NORTH_WEST);

        assertException(PositionOnTrack.MAX_COORDINATE, PositionOnTrack.MAX_COORDINATE + 1, TileTransition.NORTH_WEST);
    }

    /**
     *
     */
    public void testToInt() {
        PositionOnTrack p1 = PositionOnTrack.createComingFrom(new Vec2D(10, 20), TileTransition.NORTH);
        PositionOnTrack p2 = PositionOnTrack.createComingFrom(new Vec2D(10, 30), TileTransition.NORTH);
        assertTrue(p1.toInt() != p2.toInt());
    }

    /**
     *
     */
    public void testSetValuesFromInt() {
        PositionOnTrack p1 = PositionOnTrack.createComingFrom(new Vec2D(10, 20), TileTransition.NORTH);

        int i = p1.toInt();
        PositionOnTrack p2 = PositionOnTrack.createComingFrom(new Vec2D(60, 70), TileTransition.EAST);
        assertTrue(!p1.equals(p2));
        p2.setValuesFromInt(i);

        assertEquals(p1, p2);

        TileTransition v = TileTransition.getInstance(7); // 7 is the maximum vector number.

        p1 = PositionOnTrack.createComingFrom(new Vec2D(PositionOnTrack.MAX_COORDINATE, PositionOnTrack.MAX_COORDINATE), v);
    }

    /*
     * Test for boolean equals(Object)
     */
    public void testEqualsObject() {
        PositionOnTrack p1 = PositionOnTrack.createComingFrom(new Vec2D(10, 20), TileTransition.NORTH);
        PositionOnTrack p2 = PositionOnTrack.createComingFrom(new Vec2D(10, 20), TileTransition.NORTH);
        assertEquals(p1, p2);

        p1 = PositionOnTrack.createComingFrom(new Vec2D(10, 50), TileTransition.NORTH);
        p2 = PositionOnTrack.createComingFrom(new Vec2D(10, 20), TileTransition.NORTH);

        assertTrue(!p1.equals(p2));
    }

    // TODO replace with TestUtils
    private void assertNoException(int x, int y, TileTransition v) {
        try {
            PositionOnTrack.createComingFrom(new Vec2D(x, y), v);
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    private void assertException(int x, int y, TileTransition v) {
        try {
            PositionOnTrack.createComingFrom(new Vec2D(x, y), v);
            assertTrue(false);
        } catch (Exception e) {
        }
    }

    /**
     *
     */
    public void testGetOpposite() {
        PositionOnTrack p1 = PositionOnTrack.createComingFrom(new Vec2D(10, 10), TileTransition.NORTH);
        PositionOnTrack p2 = p1.getOpposite();
        assertNotNull(p2);

        PositionOnTrack p3 = PositionOnTrack.createComingFrom(new Vec2D(10, 11), TileTransition.SOUTH);
        assertEquals(p3, p2);
    }
}