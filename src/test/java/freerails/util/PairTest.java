package freerails.util;

import junit.framework.TestCase;

public class PairTest extends TestCase {

    public void testEqualsHandlesNullValuesAndIsReflexive() {
        Pair<String, Integer> bothNull = new Pair<>(null, null);
        Pair<String, Integer> sameValues = new Pair<>(null, null);

        assertEquals(bothNull, bothNull);
        assertEquals(bothNull, sameValues);
        assertEquals(sameValues, bothNull);
        assertEquals(bothNull.hashCode(), sameValues.hashCode());
        assertFalse(bothNull.equals(new Pair<>("value", null)));
    }

    public void testAccessorsStringRepresentationAndTypeSensitiveEquality() {
        Pair<String, Integer> pair = new Pair<>("route", 12);

        assertEquals("route", pair.a());
        assertEquals(Integer.valueOf(12), pair.b());
        assertEquals("Pair(route, 12)", pair.toString());
        assertFalse(pair.equals(null));
        assertFalse(pair.equals("Pair(route, 12)"));
        assertFalse(pair.equals(new Pair<>("route", 13)));
        assertFalse(pair.equals(new Pair<>("other", 12)));
    }
}
