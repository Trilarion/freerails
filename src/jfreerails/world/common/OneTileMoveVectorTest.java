/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jfreerails.world.common;

import java.awt.event.KeyEvent;
import java.util.NoSuchElementException;
import junit.framework.TestCase;


public class OneTileMoveVectorTest extends TestCase {
    final OneTileMoveVector n = OneTileMoveVector.NORTH;
    final OneTileMoveVector ne = OneTileMoveVector.NORTH_EAST;
    final OneTileMoveVector e = OneTileMoveVector.EAST;
    final OneTileMoveVector se = OneTileMoveVector.SOUTH_EAST;
    final OneTileMoveVector s = OneTileMoveVector.SOUTH;
    final OneTileMoveVector sw = OneTileMoveVector.SOUTH_WEST;
    final OneTileMoveVector w = OneTileMoveVector.WEST;
    final OneTileMoveVector nw = OneTileMoveVector.NORTH_WEST;

    public OneTileMoveVectorTest(String arg0) {
        super(arg0);
    }

    public void testGetDirection() {
        double d = 0;
        assertTrue(d == n.getDirection());
        d = 2 * Math.PI / 8 * 1;
        assertTrue(d == ne.getDirection());
    }

    public void testGetNearestVector() {
        //Each vector should be the nearest to itself!
        OneTileMoveVector[] vectors = OneTileMoveVector.getList();

        for (int i = 0; i < vectors.length; i++) {
            OneTileMoveVector v = vectors[i];
            OneTileMoveVector v2 = OneTileMoveVector.getNearestVector(v.deltaX,
                    v.deltaY);
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

    private void assertNearest(OneTileMoveVector v, int dx, int dy) {
        OneTileMoveVector v2 = OneTileMoveVector.getNearestVector(dx, dy);
        assertEquals(v, v2);
    }

    public void testGetNewTemplateNumber() {
        assertEquals(OneTileMoveVector.NORTH.getNewTemplateNumber(), 1);
        assertEquals(OneTileMoveVector.NORTH_EAST.getNewTemplateNumber(), 2);
        assertEquals(OneTileMoveVector.EAST.getNewTemplateNumber(), 4);
    }

    public void testGetInstanceMappedToKey() {
        OneTileMoveVector v;

        try {
            v = OneTileMoveVector.getInstanceMappedToKey(KeyEvent.VK_B);
            fail();
        } catch (NoSuchElementException e) {
        }

        v = OneTileMoveVector.getInstanceMappedToKey(KeyEvent.VK_NUMPAD1);
        assertEquals(sw, v);
        v = OneTileMoveVector.getInstanceMappedToKey(KeyEvent.VK_NUMPAD2);
        assertEquals(s, v);
        v = OneTileMoveVector.getInstanceMappedToKey(KeyEvent.VK_NUMPAD3);
        assertEquals(se, v);
        v = OneTileMoveVector.getInstanceMappedToKey(KeyEvent.VK_NUMPAD4);
        assertEquals(w, v);
        v = OneTileMoveVector.getInstanceMappedToKey(KeyEvent.VK_NUMPAD6);
        assertEquals(e, v);
        v = OneTileMoveVector.getInstanceMappedToKey(KeyEvent.VK_NUMPAD7);
        assertEquals(nw, v);
        v = OneTileMoveVector.getInstanceMappedToKey(KeyEvent.VK_NUMPAD8);
        assertEquals(n, v);
        v = OneTileMoveVector.getInstanceMappedToKey(KeyEvent.VK_NUMPAD9);
        assertEquals(ne, v);
    }
}