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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void testImmutableListOverloadsReturnDefensiveImmutableCopies() {
        List<String> sourceList = new ArrayList<>(Arrays.asList("one", "two"));
        String[] sourceArray = {"three", "four"};

        List<String> fromList = Utils.immutableList(sourceList);
        List<String> fromArray = Utils.immutableList(sourceArray);
        sourceList.add("five");
        sourceArray[0] = "changed";

        assertEquals(Arrays.asList("one", "two"), fromList);
        assertEquals(Arrays.asList("three", "four"), fromArray);
        assertUnmodifiable(fromList);
        assertUnmodifiable(fromArray);
    }

    public void testCloneBySerialisationCreatesEqualIndependentCopy() {
        Vec2D original = new Vec2D(4, 9);

        Serializable copy = Utils.cloneBySerialisation(original);

        assertEquals(original, copy);
        assertNotSame(original, copy);
    }

    public void testFindConstantFieldNameAndEqual() {
        assertEquals("MATCH", Utils.findConstantFieldName(ConstantHolder.MATCH));
        assertNull(Utils.findConstantFieldName(new ConstantHolder()));
        assertTrue(Utils.equal(null, null));
        assertFalse(Utils.equal(null, "value"));
        assertFalse(Utils.equal("value", null));
        assertTrue(Utils.equal("value", new String("value")));
        assertFalse(Utils.equal("one", "two"));
    }

    public void testNullVerificationReturnsValueAndRejectsNull() {
        Object value = new Object();

        assertSame(value, Utils.verifyNotNull(value));
        assertSame(value, Utils.verifyNotNull(value, "value is required"));
        List<Object> iterable = Collections.singletonList(value);
        assertSame(iterable, Utils.verifyNoneNull(iterable));

        try {
            Utils.verifyNotNull(null, "required");
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertEquals("required", expected.getMessage());
        }

        try {
            Utils.verifyNoneNull(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }

        try {
            Utils.verifyNoneNull(Arrays.asList(value, null));
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    public void testIntegerListSumAndListCombination() {
        assertEquals(Arrays.asList(1, 0, 1), Utils.integerListFromBoolean(true, false, true));
        assertEquals(Collections.emptyList(), Utils.integerListFromBoolean());
        assertEquals(6, Utils.sumOfIntegerList(Arrays.asList(1, 2, 3)));
        assertEquals(0, Utils.sumOfIntegerList(Collections.emptyList()));
        assertEquals(Arrays.asList("a", "b", "c"),
                Utils.combineTwoLists(Arrays.asList("a", "b"), Collections.singletonList("c")));
    }

    public void testInitializedMapAndContainsId() {
        List<Identifiable> values = Arrays.asList(new Identifiable(3), new Identifiable(7));
        Map<String, Integer> initialized = Utils.initializedMap(Arrays.asList("a", "b"), 5);

        assertEquals(2, initialized.size());
        assertEquals(Integer.valueOf(5), initialized.get("a"));
        assertEquals(Integer.valueOf(5), initialized.get("b"));
        assertEquals(new HashMap<>(), Utils.initializedMap(Collections.emptyList(), 5));
        assertTrue(Utils.containsId(7, values));
        assertFalse(Utils.containsId(9, values));
        assertFalse(Utils.containsId(7, Collections.emptyList()));
    }

    private static void assertUnmodifiable(List<?> list) {
        try {
            list.clear();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    public static final class ConstantHolder {
        public static final ConstantHolder MATCH = new ConstantHolder();
        public final String INSTANCE = "match";
        private static final String PRIVATE = "match";
    }

}
