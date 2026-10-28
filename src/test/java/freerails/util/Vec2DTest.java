package freerails.util;

import junit.framework.TestCase;

public class Vec2DTest extends TestCase {

    public void testConstructorsAndEquality() {
        Vec2D zero = new Vec2D();
        Vec2D vector = new Vec2D(3, 4);
        Vec2D copy = new Vec2D(vector);

        assertEquals(Vec2D.ZERO, zero);
        assertEquals(vector, copy);
        assertEquals(vector.hashCode(), copy.hashCode());
        assertEquals("(3, 4)", vector.toString());
        assertFalse(vector.equals(null));
        assertFalse(vector.equals("vector"));
    }

    public void testArithmeticAndPointwiseExtrema() {
        Vec2D first = new Vec2D(8, -6);
        Vec2D second = new Vec2D(2, 3);

        assertEquals(new Vec2D(10, -3), Vec2D.add(first, second));
        assertEquals(new Vec2D(6, -9), Vec2D.subtract(first, second));
        assertEquals(new Vec2D(5, -9), Vec2D.subtract(first, 3));
        assertEquals(new Vec2D(16, -18), Vec2D.multiply(first, second));
        assertEquals(new Vec2D(24, -18), Vec2D.multiply(first, 3));
        assertEquals(new Vec2D(4, -3), Vec2D.divide(first, 2));
        assertEquals(new Vec2D(8, 3), Vec2D.max(first, second));
        assertEquals(new Vec2D(2, -6), Vec2D.min(first, second));
    }

    public void testCoordinateComparisonsAndAwtConversions() {
        Vec2D vector = new Vec2D(3, 4);

        assertTrue(vector.below(new Vec2D(4, 5)));
        assertFalse(vector.below(new Vec2D(3, 5)));
        assertTrue(vector.belowOrEqual(new Vec2D(3, 4)));
        assertFalse(vector.belowOrEqual(new Vec2D(2, 5)));
        assertTrue(vector.above(new Vec2D(2, 3)));
        assertFalse(vector.above(new Vec2D(3, 3)));
        assertTrue(vector.aboveOrEqual(new Vec2D(3, 4)));
        assertFalse(vector.aboveOrEqual(new Vec2D(4, 3)));
        assertEquals(new java.awt.Point(3, 4), Vec2D.toPoint(vector));
        assertEquals(new java.awt.Dimension(3, 4), Vec2D.toDimension(vector));
    }

    public void testCompareToUsesYThenX() {
        Vec2D sameYLowerX = new Vec2D(1, 3);
        Vec2D sameYHigherX = new Vec2D(2, 3);
        Vec2D higherYLowerX = new Vec2D(-10, 4);

        assertTrue(sameYLowerX.compareTo(sameYHigherX) < 0);
        assertTrue(sameYHigherX.compareTo(higherYLowerX) < 0);
        assertTrue(higherYLowerX.compareTo(sameYHigherX) > 0);
    }

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

    public void testDivideByZeroIsRejected() {
        try {
            Vec2D.divide(new Vec2D(1, 2), 0);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
    }
}
