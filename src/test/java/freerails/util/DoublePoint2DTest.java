package freerails.util;

import junit.framework.TestCase;

/**
 * Tests double-precision two-dimensional point behavior.
 */
public class DoublePoint2DTest extends TestCase {

    public void testConstructorsAndEquality() {
        DoublePoint2D zero = new DoublePoint2D(0.0, 0.0);
        DoublePoint2D vector = new DoublePoint2D(3.5, 4.25);
        DoublePoint2D copy = new DoublePoint2D(vector);

        assertEquals(DoublePoint2D.ZERO, zero);
        assertEquals(vector, copy);
        assertEquals(vector.hashCode(), copy.hashCode());
        assertEquals("(3.5, 4.25)", vector.toString());
    }

    public void testArithmeticAndPointwiseExtrema() {
        DoublePoint2D first = new DoublePoint2D(8.5, -6.25);
        DoublePoint2D second = new DoublePoint2D(2.0, 3.5);

        assertEquals(new DoublePoint2D(10.5, -2.75), DoublePoint2D.add(first, second));
        assertEquals(new DoublePoint2D(6.5, -9.75), DoublePoint2D.subtract(first, second));
        assertEquals(new DoublePoint2D(5.5, -9.25), DoublePoint2D.subtract(first, 3.0));
        assertEquals(new DoublePoint2D(17.0, -21.875), DoublePoint2D.multiply(first, second));
        assertEquals(new DoublePoint2D(21.25, -15.625), DoublePoint2D.multiply(first, 2.5));
        assertEquals(new DoublePoint2D(4.25, -3.125), DoublePoint2D.divide(first, 2.0));
        assertEquals(new DoublePoint2D(8.5, 3.5), DoublePoint2D.max(first, second));
        assertEquals(new DoublePoint2D(2.0, -6.25), DoublePoint2D.min(first, second));
    }

    public void testCoordinateComparisons() {
        DoublePoint2D vector = new DoublePoint2D(3.5, 4.25);

        assertTrue(vector.below(new DoublePoint2D(4.0, 5.0)));
        assertFalse(vector.below(new DoublePoint2D(3.5, 5.0)));
        assertTrue(vector.belowOrEqual(new DoublePoint2D(3.5, 4.25)));
        assertFalse(vector.belowOrEqual(new DoublePoint2D(2.0, 5.0)));
        assertTrue(vector.above(new DoublePoint2D(2.5, 3.0)));
        assertFalse(vector.above(new DoublePoint2D(3.5, 3.0)));
        assertTrue(vector.aboveOrEqual(new DoublePoint2D(3.5, 4.25)));
        assertFalse(vector.aboveOrEqual(new DoublePoint2D(4.0, 3.0)));
    }

    public void testNormDoesNotOverflow() {
        DoublePoint2D vector = new DoublePoint2D(Double.MAX_VALUE, Double.MAX_VALUE);

        assertEquals(Math.hypot(Double.MAX_VALUE, Double.MAX_VALUE), vector.norm(), 0.0);
    }

    public void testDivisionUsesFloatingPointSemantics() {
        DoublePoint2D result = DoublePoint2D.divide(new DoublePoint2D(1.0, 2.0), 0.0);

        assertEquals(Double.POSITIVE_INFINITY, result.x());
        assertEquals(Double.POSITIVE_INFINITY, result.y());
    }
}
