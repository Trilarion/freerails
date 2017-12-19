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

import junit.framework.TestCase;

/**
 * JUnit test for OneTileMoveVector.
 *
 */
public class StepTest extends TestCase {
    final Step n = Step.NORTH;

    final Step ne = Step.NORTH_EAST;

    final Step e = Step.EAST;

    final Step se = Step.SOUTH_EAST;

    final Step s = Step.SOUTH;

    final Step sw = Step.SOUTH_WEST;

    final Step w = Step.WEST;

    final Step nw = Step.NORTH_WEST;

    /**
     *
     * @param arg0
     */
    public StepTest(String arg0) {
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
        Step[] vectors = Step.getList();

        for (Step v : vectors) {
            Step v2 = Step.getNearestVector(v.deltaX, v.deltaY);
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

    private void assertNearest(Step v, int dx, int dy) {
        Step v2 = Step.getNearestVector(dx, dy);
        assertEquals(v, v2);
    }

    /**
     *
     */
    public void testGetNewTemplateNumber() {
        assertEquals(Step.NORTH.get8bitTemplate(), 1);
        assertEquals(Step.NORTH_EAST.get8bitTemplate(), 2);
        assertEquals(Step.EAST.get8bitTemplate(), 4);
    }
}