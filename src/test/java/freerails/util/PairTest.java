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
}
