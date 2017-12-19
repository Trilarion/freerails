package freerails.world.train;

import freerails.world.common.FreerailsPathIterator;
import freerails.world.common.IntLine;
import junit.framework.TestCase;

/**
 * Junit test.
 *
 */
public class SimplePathIteratorImplTest extends TestCase {

    /**
     *
     * @param arg0
     */
    public SimplePathIteratorImplTest(String arg0) {
        super(arg0);
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SimplePathIteratorImplTest.class);
    }

    /**
     *
     */
    public void testHasNext() {
        int[] xpoints = {0, 100};
        int[] ypoints = {0, 0};

        FreerailsPathIterator it = new SimplePathIteratorImpl(xpoints, ypoints);
        assertTrue(it.hasNext());
        it.nextSegment(new IntLine(0, 0, 0, 0));
        assertTrue(!it.hasNext());
    }

    /**
     *
     */
    public void testNextSegment() {
        int[] xpoints = {1, 2, 3};
        int[] ypoints = {4, 5, 6};

        FreerailsPathIterator it = new SimplePathIteratorImpl(xpoints, ypoints);
        assertTrue(it.hasNext());

        IntLine line = new IntLine(0, 0, 0, 0);
        it.nextSegment(line);
        assertLineEquals(1, 4, 2, 5, line);
        assertTrue(it.hasNext());
        it.nextSegment(line);
        assertLineEquals(2, 5, 3, 6, line);
        assertTrue(!it.hasNext());
    }

    private void assertLineEquals(int x1, int y1, int x2, int y2, IntLine line) {
        assertEquals(x1, line.x1);
        assertEquals(x2, line.x2);
        assertEquals(y1, line.y1);
        assertEquals(y2, line.y2);
    }
}