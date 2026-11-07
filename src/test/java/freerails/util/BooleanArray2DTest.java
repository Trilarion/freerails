package freerails.util;

import junit.framework.TestCase;

/**
 * Tests boolean array behavior.
 */
public class BooleanArray2DTest extends TestCase {

    /**
     * Tests typed storage, row-major coordinates, and dimensions.
     */
    public void testStoresValuesByColumnAndRowAndReportsSize() {
        BooleanArray2D array = new BooleanArray2D(3, 2);

        assertFalse(array.get(0, 0));
        array.set(2, 1, true);
        array.set(1, 0, true);

        assertTrue(array.get(2, 1));
        assertTrue(array.get(1, 0));
        assertFalse(array.get(0, 1));
        assertEquals(3, array.getWidth());
        assertEquals(2, array.getHeight());
        assertEquals(new IntPoint2D(3, 2), array.getSize());
    }

    /**
     * Tests equality and hash code include dimensions and contents.
     */
    public void testEqualityAndHashCodeIncludeDimensionsAndContents() {
        BooleanArray2D first = new BooleanArray2D(2, 2);
        BooleanArray2D equal = new BooleanArray2D(2, 2);
        BooleanArray2D differentContents = new BooleanArray2D(2, 2);
        BooleanArray2D differentDimensions = new BooleanArray2D(1, 2);

        first.set(1, 1, true);
        equal.set(1, 1, true);
        differentContents.set(0, 0, true);

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
        BooleanArray2D array = new BooleanArray2D(0, 5);

        assertEquals(0, array.getWidth());
        assertEquals(5, array.getHeight());
    }

    /**
     * Tests rejection of coordinates outside the array.
     */
    public void testCoordinatesOutsideArrayAreRejected() {
        BooleanArray2D array = new BooleanArray2D(2, 2);
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
        try {
            new BooleanArray2D(width, height);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    /**
     * Asserts that an invalid coordinate is rejected when reading.
     *
     * @param array the array to use in the test
     * @param x the column
     * @param y the row
     */
    private void assertInvalidCoordinate(BooleanArray2D array, int x, int y) {
        try {
            array.get(x, y);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            // expected
        }
    }

    /**
     * Asserts that an invalid coordinate is rejected when writing.
     *
     * @param array the array to use in the test
     * @param x the column
     * @param y the row
     */
    private void assertInvalidSetCoordinate(BooleanArray2D array, int x, int y) {
        try {
            array.set(x, y, true);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            // expected
        }
    }
}
