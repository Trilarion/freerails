package freerails.util;

import junit.framework.TestCase;

/**
 * Tests integer two-dimensional point behavior.
 */
public class IntPoint2DTest extends TestCase {

    /**
     * Tests constructors and equality behavior.
     */
    public void testConstructorsAndEquality() {
        IntPoint2D zero = new IntPoint2D();
        IntPoint2D vector = new IntPoint2D(3, 4);
        IntPoint2D copy = new IntPoint2D(vector);

        assertEquals(IntPoint2D.ZERO, zero);
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
        IntPoint2D first = new IntPoint2D(8, -6);
        IntPoint2D second = new IntPoint2D(2, 3);

        assertEquals(new IntPoint2D(10, -3), IntPoint2D.add(first, second));
        assertEquals(new IntPoint2D(6, -9), IntPoint2D.subtract(first, second));
        assertEquals(new IntPoint2D(5, -9), IntPoint2D.subtract(first, 3));
        assertEquals(new IntPoint2D(16, -18), IntPoint2D.multiply(first, second));
        assertEquals(new IntPoint2D(24, -18), IntPoint2D.multiply(first, 3));
        assertEquals(new IntPoint2D(4, -3), IntPoint2D.divide(first, 2));
        assertEquals(new IntPoint2D(8, 3), IntPoint2D.max(first, second));
        assertEquals(new IntPoint2D(2, -6), IntPoint2D.min(first, second));
    }

    /**
     * Tests coordinate comparisons and awt conversions behavior.
     */
    public void testCoordinateComparisonsAndAwtConversions() {
        IntPoint2D vector = new IntPoint2D(3, 4);

        assertTrue(vector.below(new IntPoint2D(4, 5)));
        assertFalse(vector.below(new IntPoint2D(3, 5)));
        assertTrue(vector.belowOrEqual(new IntPoint2D(3, 4)));
        assertFalse(vector.belowOrEqual(new IntPoint2D(2, 5)));
        assertTrue(vector.above(new IntPoint2D(2, 3)));
        assertFalse(vector.above(new IntPoint2D(3, 3)));
        assertTrue(vector.aboveOrEqual(new IntPoint2D(3, 4)));
        assertFalse(vector.aboveOrEqual(new IntPoint2D(4, 3)));
        assertEquals(new java.awt.Point(3, 4), IntPoint2D.toPoint(vector));
        assertEquals(new java.awt.Dimension(3, 4), IntPoint2D.toDimension(vector));
    }

    /**
     * Tests compare to uses y then x behavior.
     */
    public void testCompareToUsesYThenX() {
        IntPoint2D sameYLowerX = new IntPoint2D(1, 3);
        IntPoint2D sameYHigherX = new IntPoint2D(2, 3);
        IntPoint2D higherYLowerX = new IntPoint2D(-10, 4);

        assertTrue(sameYLowerX.compareTo(sameYHigherX) < 0);
        assertTrue(sameYHigherX.compareTo(higherYLowerX) < 0);
        assertTrue(higherYLowerX.compareTo(sameYHigherX) > 0);
    }

    /**
     * Tests norm does not overflow behavior.
     */
    public void testNormDoesNotOverflow() {
        IntPoint2D vector = new IntPoint2D(Integer.MAX_VALUE, Integer.MAX_VALUE);

        assertEquals(Math.hypot(Integer.MAX_VALUE, Integer.MAX_VALUE), vector.norm(), 0.001);
    }

    /**
     * Tests compare to does not overflow at integer extremes behavior.
     */
    public void testCompareToDoesNotOverflowAtIntegerExtremes() {
        IntPoint2D smallest = new IntPoint2D(Integer.MIN_VALUE, Integer.MIN_VALUE);
        IntPoint2D largest = new IntPoint2D(Integer.MAX_VALUE, Integer.MAX_VALUE);

        assertTrue(smallest.compareTo(largest) < 0);
        assertTrue(largest.compareTo(smallest) > 0);
        assertEquals(0, smallest.compareTo(new IntPoint2D(Integer.MIN_VALUE, Integer.MIN_VALUE)));
    }

    /**
     * Tests divide by zero is rejected behavior.
     */
    public void testDivideByZeroIsRejected() {
        TestUtils.assertThrows(ArithmeticException.class, () -> IntPoint2D.divide(new IntPoint2D(1, 2), 0));
    }
}
