package freerails.util;

import junit.framework.TestCase;

public class Array2DTest extends TestCase {

    public void testRejectsNegativeDimensions() {
        assertInvalidDimensions(-1, 2);
        assertInvalidDimensions(2, -1);
    }

    public void testRejectsDimensionsWhoseProductOverflows() {
        assertInvalidDimensions(Integer.MAX_VALUE, 2);
    }

    public void testZeroSizedDimensionIsAllowed() {
        Array2D array = new Array2D(0, 5);

        assertEquals(0, array.getWidth());
        assertEquals(5, array.getHeight());
    }

    public void testCoordinatesOutsideArrayAreRejected() {
        Array2D array = new Array2D(2, 2);
        assertInvalidCoordinate(array, -1, 0);
        assertInvalidCoordinate(array, 0, 2);
    }

    private void assertInvalidDimensions(int width, int height) {
        try {
            new Array2D(width, height);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    private void assertInvalidCoordinate(Array2D array, int x, int y) {
        try {
            array.get(x, y);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
        }
    }
}
