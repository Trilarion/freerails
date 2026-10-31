package freerails.util;

import junit.framework.TestCase;

/**
 * Tests double-precision two-dimensional vector behavior.
 */
public class Vector2DDoubleTest extends TestCase {

    public void testConstructorsAndEquality() {
        Vector2DDouble zero = new Vector2DDouble(0.0, 0.0);
        Vector2DDouble vector = new Vector2DDouble(3.5, 4.25);
        Vector2DDouble copy = new Vector2DDouble(vector);

        assertEquals(Vector2DDouble.ZERO, zero);
        assertEquals(vector, copy);
        assertEquals(vector.hashCode(), copy.hashCode());
        assertEquals("(3.5, 4.25)", vector.toString());
    }

    public void testArithmeticAndPointwiseExtrema() {
        Vector2DDouble first = new Vector2DDouble(8.5, -6.25);
        Vector2DDouble second = new Vector2DDouble(2.0, 3.5);

        assertEquals(new Vector2DDouble(10.5, -2.75), Vector2DDouble.add(first, second));
        assertEquals(new Vector2DDouble(6.5, -9.75), Vector2DDouble.subtract(first, second));
        assertEquals(new Vector2DDouble(5.5, -9.25), Vector2DDouble.subtract(first, 3.0));
        assertEquals(new Vector2DDouble(17.0, -21.875), Vector2DDouble.multiply(first, second));
        assertEquals(new Vector2DDouble(21.25, -15.625), Vector2DDouble.multiply(first, 2.5));
        assertEquals(new Vector2DDouble(4.25, -3.125), Vector2DDouble.divide(first, 2.0));
        assertEquals(new Vector2DDouble(8.5, 3.5), Vector2DDouble.max(first, second));
        assertEquals(new Vector2DDouble(2.0, -6.25), Vector2DDouble.min(first, second));
    }

    public void testCoordinateComparisons() {
        Vector2DDouble vector = new Vector2DDouble(3.5, 4.25);

        assertTrue(vector.below(new Vector2DDouble(4.0, 5.0)));
        assertFalse(vector.below(new Vector2DDouble(3.5, 5.0)));
        assertTrue(vector.belowOrEqual(new Vector2DDouble(3.5, 4.25)));
        assertFalse(vector.belowOrEqual(new Vector2DDouble(2.0, 5.0)));
        assertTrue(vector.above(new Vector2DDouble(2.5, 3.0)));
        assertFalse(vector.above(new Vector2DDouble(3.5, 3.0)));
        assertTrue(vector.aboveOrEqual(new Vector2DDouble(3.5, 4.25)));
        assertFalse(vector.aboveOrEqual(new Vector2DDouble(4.0, 3.0)));
    }

    public void testNormDoesNotOverflow() {
        Vector2DDouble vector = new Vector2DDouble(Double.MAX_VALUE, Double.MAX_VALUE);

        assertEquals(Math.hypot(Double.MAX_VALUE, Double.MAX_VALUE), vector.norm(), 0.0);
    }

    public void testDivisionUsesFloatingPointSemantics() {
        Vector2DDouble result = Vector2DDouble.divide(new Vector2DDouble(1.0, 2.0), 0.0);

        assertEquals(Double.POSITIVE_INFINITY, result.x());
        assertEquals(Double.POSITIVE_INFINITY, result.y());
    }
}
