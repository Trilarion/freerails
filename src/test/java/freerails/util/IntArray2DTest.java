package freerails.util;

import junit.framework.TestCase;

/**
 * Tests integer 2D array behavior.
 */
public class IntArray2DTest extends TestCase {

    /**
     * Tests storing values by column and row and reporting size.
     */
    public void testStoresValuesByColumnAndRowAndReportsSize() {
        IntArray2D array = new IntArray2D(3, 2);

        assertEquals(0, array.get(0, 0));
        array.set(2, 1, 17);
        array.set(1, 0, -4);

        assertEquals(17, array.get(2, 1));
        assertEquals(-4, array.get(1, 0));
        assertEquals(3, array.getWidth());
        assertEquals(2, array.getHeight());
        assertEquals(new IntPoint2D(3, 2), array.getSize());
    }

    /**
     * Tests equality and hash code include dimensions and contents.
     */
    public void testEqualityAndHashCodeIncludeDimensionsAndContents() {
        IntArray2D first = new IntArray2D(2, 2);
        IntArray2D equal = new IntArray2D(2, 2);
        IntArray2D differentContents = new IntArray2D(2, 2);
        IntArray2D differentDimensions = new IntArray2D(1, 2);

        first.set(1, 1, 9);
        equal.set(1, 1, 9);
        differentContents.set(0, 0, 9);

        assertEquals(first, first);
        assertEquals(first, equal);
        assertEquals(first.hashCode(), equal.hashCode());
        assertFalse(first.equals(differentContents));
        assertFalse(first.equals(differentDimensions));
        assertFalse(first.equals(null));
        assertFalse(first.equals("array"));
    }

    /**
     * Tests rejection of negative dimensions.
     */
    public void testRejectsNegativeDimensions() {
        assertInvalidDimensions(-1, 2);
        assertInvalidDimensions(2, -1);
    }

    /**
     * Tests rejection of dimensions whose product overflows.
     */
    public void testRejectsDimensionsWhoseProductOverflows() {
        assertInvalidDimensions(Integer.MAX_VALUE, 2);
    }

    /**
     * Tests that a zero-sized dimension is allowed.
     */
    public void testZeroSizedDimensionIsAllowed() {
        IntArray2D array = new IntArray2D(0, 5);

        assertEquals(0, array.getWidth());
        assertEquals(5, array.getHeight());
    }

    /**
     * Tests rejection of coordinates outside the array.
     */
    public void testCoordinatesOutsideArrayAreRejected() {
        IntArray2D array = new IntArray2D(2, 2);
        assertInvalidCoordinate(array, -1, 0);
        assertInvalidCoordinate(array, 2, 0);
        assertInvalidCoordinate(array, 0, -1);
        assertInvalidCoordinate(array, 0, 2);
        assertInvalidSetCoordinate(array, -1, 0);
    }

    /**
     * Asserts invalid dimensions.
     * @param width the width to use in the test.
     * @param height the height to use in the test.
     */
    private void assertInvalidDimensions(int width, int height) {
        TestUtils.assertThrows(IllegalArgumentException.class, () -> new IntArray2D(width, height));
    }

    /**
     * Asserts invalid coordinate.
     * @param array the array to use in the test.
     * @param x the x to use in the test.
     * @param y the y to use in the test.
     */
    private void assertInvalidCoordinate(IntArray2D array, int x, int y) {
        TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> array.get(x, y));
    }

    /**
     * Asserts invalid set coordinate.
     * @param array the array to use in the test.
     * @param x the x to use in the test.
     * @param y the y to use in the test.
     */
    private void assertInvalidSetCoordinate(IntArray2D array, int x, int y) {
        TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> array.set(x, y, 1));
    }
}
