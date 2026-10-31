package freerails.util;

import junit.framework.TestCase;

/**
 * Tests array2 d behavior.
 */
public class Array2DTest extends TestCase {

    /**
     * Tests stores values by column and row and reports size behavior.
     */
    public void testStoresValuesByColumnAndRowAndReportsSize() {
        Array2D array = new Array2D(3, 2);

        assertEquals(0, array.get(0, 0));
        array.set(2, 1, 17);
        array.set(1, 0, -4);

        assertEquals(17, array.get(2, 1));
        assertEquals(-4, array.get(1, 0));
        assertEquals(3, array.getWidth());
        assertEquals(2, array.getHeight());
        assertEquals(new Vector2D(3, 2), array.getSize());
    }

    /**
     * Tests equality and hash code include dimensions and contents behavior.
     */
    public void testEqualityAndHashCodeIncludeDimensionsAndContents() {
        Array2D first = new Array2D(2, 2);
        Array2D equal = new Array2D(2, 2);
        Array2D differentContents = new Array2D(2, 2);
        Array2D differentDimensions = new Array2D(1, 2);

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
     * Tests rejects negative dimensions behavior.
     */
    public void testRejectsNegativeDimensions() {
        assertInvalidDimensions(-1, 2);
        assertInvalidDimensions(2, -1);
    }

    /**
     * Tests rejects dimensions whose product overflows behavior.
     */
    public void testRejectsDimensionsWhoseProductOverflows() {
        assertInvalidDimensions(Integer.MAX_VALUE, 2);
    }

    /**
     * Tests zero sized dimension is allowed behavior.
     */
    public void testZeroSizedDimensionIsAllowed() {
        Array2D array = new Array2D(0, 5);

        assertEquals(0, array.getWidth());
        assertEquals(5, array.getHeight());
    }

    /**
     * Tests coordinates outside array are rejected behavior.
     */
    public void testCoordinatesOutsideArrayAreRejected() {
        Array2D array = new Array2D(2, 2);
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
        try {
            new Array2D(width, height);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    /**
     * Asserts invalid coordinate.
     * @param array the array to use in the test.
     * @param x the x to use in the test.
     * @param y the y to use in the test.
     */
    private void assertInvalidCoordinate(Array2D array, int x, int y) {
        try {
            array.get(x, y);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    /**
     * Asserts invalid set coordinate.
     * @param array the array to use in the test.
     * @param x the x to use in the test.
     * @param y the y to use in the test.
     */
    private void assertInvalidSetCoordinate(Array2D array, int x, int y) {
        try {
            array.set(x, y, 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
        }
    }
}
