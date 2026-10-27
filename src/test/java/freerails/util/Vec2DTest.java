package freerails.util;

import junit.framework.TestCase;

public class Vec2DTest extends TestCase {

    public void testNormDoesNotOverflow() {
        Vec2D vector = new Vec2D(Integer.MAX_VALUE, Integer.MAX_VALUE);

        assertEquals(Math.hypot(Integer.MAX_VALUE, Integer.MAX_VALUE), vector.norm(), 0.001);
    }

    public void testCompareToDoesNotOverflowAtIntegerExtremes() {
        Vec2D smallest = new Vec2D(Integer.MIN_VALUE, Integer.MIN_VALUE);
        Vec2D largest = new Vec2D(Integer.MAX_VALUE, Integer.MAX_VALUE);

        assertTrue(smallest.compareTo(largest) < 0);
        assertTrue(largest.compareTo(smallest) > 0);
        assertEquals(0, smallest.compareTo(new Vec2D(Integer.MIN_VALUE, Integer.MIN_VALUE)));
    }
}
