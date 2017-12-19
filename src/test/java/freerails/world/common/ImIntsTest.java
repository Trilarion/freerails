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

/*
 * Created on 06-Jul-2005
 *
 */
package freerails.world.common;

import freerails.util.Utils;
import junit.framework.TestCase;

/**
 *
 */
public class ImIntsTest extends TestCase {

    /*
     * Test method for 'freerails.world.common.ImInts.append(int...)'
     */

    /**
     *
     */

    public void testAppend() {

        int[] a = {1, 2, 3};
        int[] b = {4, 5, 6, 7};
        int[] c = {1, 2, 3, 4, 5, 6, 7};
        ImInts ai = new ImInts(a);
        ImInts ci = new ImInts(c);
        assertFalse(ci.equals(ai));
        assertEquals(ci, ai.append(b));

    }

    /**
     *
     */
    public void testRemoveLast() {
        // Test method does not change original
        ImInts original = new ImInts(1, 2, 3, 4);
        ImInts clone = (ImInts) Utils.cloneBySerialisation(original);

        assertEquals(original, clone);
        original.removeLast();
        assertEquals(original, clone);

        ImInts actual, expected;
        actual = (new ImInts(1, 2, 3)).removeLast();
        expected = new ImInts(1, 2);
        assertEquals(expected, actual);

        actual = (new ImInts(1, 2)).removeLast();
        expected = new ImInts(1);
        assertEquals(expected, actual);

        actual = (new ImInts(1, 2, 4, 3)).removeLast();
        expected = new ImInts(1, 2, 4);
        assertEquals(expected, actual);
    }

    /**
     *
     */
    public void testEquals() {
        int[] a = {1, 2, 3};
        int[] b = {1, 2, 3};
        ImInts ai = new ImInts(a);
        ImInts bi = new ImInts(b);
        assertEquals(ai, bi);
        ImInts ci = new ImInts(1, 2, 3);
        assertEquals(ai, ci);

    }

}
