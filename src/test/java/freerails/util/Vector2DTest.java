package freerails.util;

import junit.framework.TestCase;

/**
 * Tests integer two-dimensional vector behavior.
 */
public class Vector2DTest extends TestCase {

    /**
     * Tests constructors and equality behavior.
     */
    public void testConstructorsAndEquality() {
        Vector2D zero = new Vector2D();
        Vector2D vector = new Vector2D(3, 4);
        Vector2D copy = new Vector2D(vector);

        assertEquals(Vector2D.ZERO, zero);
        assertEquals(vector, copy);
        assertEquals(vector.hashCode(), copy.hashCode());
        assertEquals("(3, 4)", vector.toString());
        assertFalse(vector.equals(null));
        assertFalse(vector.equals("vector"));
    }

    /**
     * Tests arithmetic and pointwise extrema behavior.
     */
    public void testArithmeticAndPointwiseExtrema() {
        Vector2D first = new Vector2D(8, -6);
        Vector2D second = new Vector2D(2, 3);

        assertEquals(new Vector2D(10, -3), Vector2D.add(first, second));
        assertEquals(new Vector2D(6, -9), Vector2D.subtract(first, second));
        assertEquals(new Vector2D(5, -9), Vector2D.subtract(first, 3));
        assertEquals(new Vector2D(16, -18), Vector2D.multiply(first, second));
        assertEquals(new Vector2D(24, -18), Vector2D.multiply(first, 3));
        assertEquals(new Vector2D(4, -3), Vector2D.divide(first, 2));
        assertEquals(new Vector2D(8, 3), Vector2D.max(first, second));
        assertEquals(new Vector2D(2, -6), Vector2D.min(first, second));
    }

    /**
     * Tests coordinate comparisons and awt conversions behavior.
     */
    public void testCoordinateComparisonsAndAwtConversions() {
        Vector2D vector = new Vector2D(3, 4);

        assertTrue(vector.below(new Vector2D(4, 5)));
        assertFalse(vector.below(new Vector2D(3, 5)));
        assertTrue(vector.belowOrEqual(new Vector2D(3, 4)));
        assertFalse(vector.belowOrEqual(new Vector2D(2, 5)));
        assertTrue(vector.above(new Vector2D(2, 3)));
        assertFalse(vector.above(new Vector2D(3, 3)));
        assertTrue(vector.aboveOrEqual(new Vector2D(3, 4)));
        assertFalse(vector.aboveOrEqual(new Vector2D(4, 3)));
        assertEquals(new java.awt.Point(3, 4), Vector2D.toPoint(vector));
        assertEquals(new java.awt.Dimension(3, 4), Vector2D.toDimension(vector));
    }

    /**
     * Tests compare to uses y then x behavior.
     */
    public void testCompareToUsesYThenX() {
        Vector2D sameYLowerX = new Vector2D(1, 3);
        Vector2D sameYHigherX = new Vector2D(2, 3);
        Vector2D higherYLowerX = new Vector2D(-10, 4);

        assertTrue(sameYLowerX.compareTo(sameYHigherX) < 0);
        assertTrue(sameYHigherX.compareTo(higherYLowerX) < 0);
        assertTrue(higherYLowerX.compareTo(sameYHigherX) > 0);
    }

    /**
     * Tests norm does not overflow behavior.
     */
    public void testNormDoesNotOverflow() {
        Vector2D vector = new Vector2D(Integer.MAX_VALUE, Integer.MAX_VALUE);

        assertEquals(Math.hypot(Integer.MAX_VALUE, Integer.MAX_VALUE), vector.norm(), 0.001);
    }

    /**
     * Tests compare to does not overflow at integer extremes behavior.
     */
    public void testCompareToDoesNotOverflowAtIntegerExtremes() {
        Vector2D smallest = new Vector2D(Integer.MIN_VALUE, Integer.MIN_VALUE);
        Vector2D largest = new Vector2D(Integer.MAX_VALUE, Integer.MAX_VALUE);

        assertTrue(smallest.compareTo(largest) < 0);
        assertTrue(largest.compareTo(smallest) > 0);
        assertEquals(0, smallest.compareTo(new Vector2D(Integer.MIN_VALUE, Integer.MIN_VALUE)));
    }

    /**
     * Tests divide by zero is rejected behavior.
     */
    public void testDivideByZeroIsRejected() {
        try {
            Vector2D.divide(new Vector2D(1, 2), 0);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
    }
}
