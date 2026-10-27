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

/*
 *
 */
package freerails.util;

import junit.framework.TestCase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class UtilsTest extends TestCase {

    /**
     *
     */
    public void testEqualsBySerialization() {

        Serializable a = new Vec2D(10, 10);
        Serializable b = new Vec2D(10, 10);
        Serializable c = new Vec2D(30, 10);

        assertTrue(Utils.equalsBySerialization(a, b));
        assertTrue(Utils.equalsBySerialization(a, a));
        assertTrue(Utils.equalsBySerialization(b, b));
        assertTrue(Utils.equalsBySerialization(c, c));
        assertFalse(Utils.equalsBySerialization(a, c));
    }

    public void testRemoveLastRemovesLastElementRatherThanSizeValue() {
        List<Integer> values = new ArrayList<>(Arrays.asList(4, 2, 3));

        Utils.removeLast(values);

        assertEquals(Arrays.asList(4, 2), values);
    }

    public void testRemoveLastRejectsEmptyCollection() {
        try {
            Utils.removeLast(new ArrayList<>());
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            // expected
        }
    }

    public void testRemoveLastOfListReturnsCopyWithoutLastElement() {
        List<String> original = new ArrayList<>(Arrays.asList("a", "b", "c"));

        List<String> result = Utils.removeLastOfList(original);

        assertEquals(Arrays.asList("a", "b"), result);
        assertEquals(Arrays.asList("a", "b", "c"), original);
    }

    public void testVerifyUnmodifiableReturnsDefensiveImmutableCopy() {
        List<String> source = new ArrayList<>(Arrays.asList("one", "two"));

        List<String> result = Utils.verifyUnmodifiable(source);
        source.add("three");

        assertEquals(Arrays.asList("one", "two"), result);
        try {
            result.add("four");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

}
