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
 *
 */
package freerails.util;

import junit.framework.TestCase;

/**
 *
 */
public class ImmutableListTest extends TestCase {

    /**
     *
     */
    public void testEquals() {
        Integer[] a = {1, 2, 3};
        Integer[] b = {1, 2, 3};
        ImmutableList<Integer> ai = new ImmutableList<>(a);
        ImmutableList<Integer> bi = new ImmutableList<>(b);
        assertEquals(ai, bi);
        ImmutableList<Integer> ci = new ImmutableList<>(1, 2, 3);
        assertEquals(ai, ci);
    }


    /**
     *
     */
    public void testCombineLists() {

        Integer[] a = {1, 2, 3};
        Integer[] b = {4, 5, 6, 7};
        Integer[] c = {1, 2, 3, 4, 5, 6, 7};
        ImmutableList<Integer> ai = new ImmutableList<>(a);
        ImmutableList<Integer> bi = new ImmutableList<>(b);
        ImmutableList<Integer> ci = new ImmutableList<>(c);
        assertFalse(ci.equals(ai));
        assertEquals(ci, Utils.combineTwoImmutableLists(ai, bi));
    }

    /**
     *
     */
    public void testRemoveLast() {

        // Test method does not change original
        ImmutableList<Integer> original = new ImmutableList<>(1, 2, 3, 4);
        ImmutableList<Integer> clone = (ImmutableList<Integer>) Utils.cloneBySerialisation(original);

        assertEquals(original, clone);

        ImmutableList<Integer> actual = Utils.removeLastOfImmutableList(new ImmutableList<>(1, 2, 3));
        ImmutableList<Integer> expected = new ImmutableList<>(1, 2);
        assertEquals(expected, actual);

        actual = Utils.removeLastOfImmutableList(new ImmutableList<>(1, 2));
        expected = new ImmutableList<>(1);
        assertEquals(expected, actual);
    }
}
