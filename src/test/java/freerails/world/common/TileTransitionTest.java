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

package freerails.world.common;

import freerails.world.TileTransition;
import junit.framework.TestCase;

/**
 * JUnit test for OneTileMoveVector.
 */
public class TileTransitionTest extends TestCase {
    final TileTransition n = TileTransition.NORTH;

    final TileTransition ne = TileTransition.NORTH_EAST;

    final TileTransition s = TileTransition.SOUTH;

    final TileTransition sw = TileTransition.SOUTH_WEST;

    final TileTransition w = TileTransition.WEST;

    /**
     * @param arg0
     */
    public TileTransitionTest(String arg0) {
        super(arg0);
    }

    /**
     *
     */
    public void testGetDirection() {
        double d = 0;
        assertTrue(d == n.getDirection());
        d = 2 * Math.PI / 8 * 1;
        assertTrue(d == ne.getDirection());
    }

    /**
     *
     */
    public void testGetNearestVector() {
        // Each vector should be the nearest to itself!
        TileTransition[] vectors = TileTransition.getList();

        for (TileTransition v : vectors) {
            TileTransition v2 = TileTransition.getNearestVector(v.deltaX, v.deltaY);
            assertEquals(v, v2);
        }

        assertNearest(n, 0, -1);
        assertNearest(n, 0, -99);
        assertNearest(n, 2, -5);
        assertNearest(n, -2, -5);
        assertNearest(s, 2, 5);

        assertNearest(w, -5, -1);

        assertNearest(sw, -4, 3);

        assertNearest(ne, 10, -6);

        assertNearest(ne, 10, -6);
    }

    private void assertNearest(TileTransition v, int dx, int dy) {
        TileTransition v2 = TileTransition.getNearestVector(dx, dy);
        assertEquals(v, v2);
    }

    /**
     *
     */
    public void testGetNewTemplateNumber() {
        assertEquals(TileTransition.NORTH.get8bitTemplate(), 1);
        assertEquals(TileTransition.NORTH_EAST.get8bitTemplate(), 2);
        assertEquals(TileTransition.EAST.get8bitTemplate(), 4);
    }
}