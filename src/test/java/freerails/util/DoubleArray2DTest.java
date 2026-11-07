package freerails.util;

import junit.framework.TestCase;

/**
 * Tests double array behavior.
 */
public class DoubleArray2DTest extends TestCase {

    /**
     * Tests typed storage, row-major coordinates, and dimensions.
     */
    public void testStoresValuesByColumnAndRowAndReportsSize() {
        DoubleArray2D array = new DoubleArray2D(3, 2);

        assertEquals(0.0, array.get(0, 0));
        array.set(2, 1, 17.5);
        array.set(1, 0, -4.25);

        assertEquals(17.5, array.get(2, 1));
        assertEquals(-4.25, array.get(1, 0));
        assertEquals(3, array.getWidth());
        assertEquals(2, array.getHeight());
        assertEquals(new IntPoint2D(3, 2), array.getSize());
    }

    /**
     * Tests equality and hash code include dimensions and contents.
     */
    public void testEqualityAndHashCodeIncludeDimensionsAndContents() {
        DoubleArray2D first = new DoubleArray2D(2, 2);
        DoubleArray2D equal = new DoubleArray2D(2, 2);
        DoubleArray2D differentContents = new DoubleArray2D(2, 2);
        DoubleArray2D differentDimensions = new DoubleArray2D(1, 2);

        first.set(1, 1, 9.5);
        equal.set(1, 1, 9.5);
        differentContents.set(0, 0, 9.5);

        assertEquals(first, first);
        assertEquals(first, equal);
        assertEquals(first.hashCode(), equal.hashCode());
        assertFalse(first.equals(differentContents));
        assertFalse(first.equals(differentDimensions));
        assertFalse(first.equals(null));
        assertFalse(first.equals("array"));
        assertFalse(first.equals(new IntArray2D(2, 2)));
    }

    /**
     * Tests rejection of invalid dimensions.
     */
    public void testRejectsInvalidDimensions() {
        assertInvalidDimensions(-1, 2);
        assertInvalidDimensions(2, -1);
        assertInvalidDimensions(Integer.MAX_VALUE, 2);
    }

    /**
     * Tests that zero-sized dimensions are allowed.
     */
    public void testZeroSizedDimensionIsAllowed() {
        DoubleArray2D array = new DoubleArray2D(0, 5);

        assertEquals(0, array.getWidth());
        assertEquals(5, array.getHeight());
    }

    /**
     * Tests rejection of coordinates outside the array.
     */
    public void testCoordinatesOutsideArrayAreRejected() {
        DoubleArray2D array = new DoubleArray2D(2, 2);
        assertInvalidCoordinate(array, -1, 0);
        assertInvalidCoordinate(array, 2, 0);
        assertInvalidCoordinate(array, 0, -1);
        assertInvalidCoordinate(array, 0, 2);
        assertInvalidSetCoordinate(array, -1, 0);
    }

    /**
     * Asserts that invalid dimensions are rejected.
     *
     * @param width the array width
     * @param height the array height
     */
    private void assertInvalidDimensions(int width, int height) {
        TestUtils.assertThrows(IllegalArgumentException.class, () -> new DoubleArray2D(width, height));
    }

    /**
     * Asserts that an invalid coordinate is rejected when reading.
     *
     * @param array the array to use in the test
     * @param x the column
     * @param y the row
     */
    private void assertInvalidCoordinate(DoubleArray2D array, int x, int y) {
        TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> array.get(x, y));
    }

    /**
     * Asserts that an invalid coordinate is rejected when writing.
     *
     * @param array the array to use in the test
     * @param x the column
     * @param y the row
     */
    private void assertInvalidSetCoordinate(DoubleArray2D array, int x, int y) {
        TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> array.set(x, y, 1.0));
    }
}
