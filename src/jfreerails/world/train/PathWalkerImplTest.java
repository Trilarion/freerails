package jfreerails.world.train;

import java.awt.Point;
import java.util.ArrayList;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.FreerailsPathIteratorImpl;
import jfreerails.world.common.IntLine;
import junit.framework.TestCase;


/** JUnit test.
 * @author Luke
 */
public class PathWalkerImplTest extends TestCase {
    FreerailsPathIterator it;
    PathWalker pw;

    public PathWalkerImplTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PathWalkerImplTest.class);
    }

    /*
     * Test for boolean canStepForward()
     */
    public void testCanStepForward() {
        setup();

        assertTrue(pw.canStepForward());
        pw.stepForward(500); //The path length is 200;
        moveToNextLimit();

        assertTrue(!pw.canStepForward());

        setup();
        assertTrue(pw.canStepForward());
        pw.stepForward(10);
        assertTrue(pw.canStepForward());

        IntLine line = new IntLine();
        assertTrue(pw.hasNext());
        pw.nextSegment(line);
        assertLineEquals(0, 0, 10, 0, line);
        assertTrue(!pw.hasNext());
        assertTrue(pw.canStepForward());

        pw.stepForward(500); //The path length is 200;
        assertTrue(pw.hasNext());
        pw.nextSegment(line);
        assertLineEquals(10, 0, 100, 0, line);
        assertTrue(pw.hasNext());
        pw.nextSegment(line);
        assertLineEquals(100, 0, 100, 100, line);

        assertTrue(!pw.canStepForward());
    }

    private void moveToNextLimit() {
        IntLine line = new IntLine();

        while (pw.hasNext()) {
            pw.nextSegment(line);
        }
    }

    public void testHasNext() {
        IntLine line = new IntLine();

        setup();
        assertTrue(!pw.hasNext());
        pw.stepForward(10);
        assertTrue(pw.hasNext());

        pw.nextSegment(line);
        assertLineEquals(0, 0, 10, 0, line);
        assertTrue(!pw.hasNext());

        setup();
        assertTrue(!pw.hasNext());
        pw.stepForward(110);
        assertTrue(pw.hasNext());
        line = new IntLine();
        pw.nextSegment(line);
        assertLineEquals(0, 0, 100, 0, line);
        assertTrue(pw.hasNext());
        pw.nextSegment(line);
        assertLineEquals(100, 0, 100, 10, line);
        assertTrue(!pw.hasNext());

        /* Now test with underlying pathIterators with few elements.
         */
        ArrayList<Point> points = new ArrayList<Point>();

        assertHasNextEqualsFalse(points);

        points = new ArrayList<Point>();
        points.add(new Point(0, 0));

        assertHasNextEqualsFalse(points);

        points = new ArrayList<Point>();
        points.add(new Point(0, 0));
        points.add(new Point(100, 0));

        FreerailsPathIterator it2 = FreerailsPathIteratorImpl.forwardsIterator(points);
        assertTrue(it2.hasNext());
        pw = new PathWalkerImpl(it2);
        assertTrue(!pw.hasNext());
        pw.stepForward(1000);
        assertTrue(pw.hasNext());
        pw.nextSegment(line);
        assertTrue(!pw.hasNext());
    }

    void assertHasNextEqualsFalse(ArrayList points) {
        FreerailsPathIterator it2 = FreerailsPathIteratorImpl.forwardsIterator(points);

        assertTrue(!it2.hasNext());
        pw = new PathWalkerImpl(it2);
        pw.stepForward(100);
        assertTrue(!pw.hasNext());
    }

    public void setup() {
        int[] xpoints = {0, 100, 100};
        int[] ypoints = {0, 0, 100};
        it = new SimplePathIteratorImpl(xpoints, ypoints);
        pw = new PathWalkerImpl(it);
    }

    private void assertLineEquals(int x1, int y1, int x2, int y2, IntLine line) {
        assertEquals(x1, line.x1);
        assertEquals(x2, line.x2);
        assertEquals(y1, line.y1);
        assertEquals(y2, line.y2);
    }
}