/*
 * Created on 09-Feb-2005
 *
 */
package freerails.world.train;

import freerails.util.Pair;
import freerails.world.common.FreerailsPathIterator;
import freerails.world.common.ImPoint;
import freerails.world.common.IntLine;
import freerails.world.common.Step;
import junit.framework.TestCase;

import java.util.Iterator;

import static freerails.world.common.Step.*;

/**
 * JUnit test for PathOnTiles.
 *
 * @author Luke
 */
public class PathOnTilesTest extends TestCase {

    /**
     *
     */
    public void testPathOnTiles() {
        ImPoint start = null;
        Step[] vectors = null;
        assertTrue(throwsException(start, vectors));
        start = new ImPoint();
        assertTrue(throwsException(start, vectors));
        vectors = new Step[]{null, null};
        assertTrue(throwsException(start, vectors));
        vectors = new Step[]{NORTH, SOUTH};
        assertFalse(throwsException(start, vectors));

    }

    /**
     *
     */
    public void testGetStepIndex() {
        ImPoint start = new ImPoint();
        Step[] vectors = new Step[]{SOUTH_EAST, EAST, EAST};
        PathOnTiles path = new PathOnTiles(start, vectors);
        assertEquals(0, path.getStepIndex(0));
        assertEquals(0, path.getStepIndex(1));
        assertEquals(0, path.getStepIndex(30));
        assertEquals(1, path.getStepIndex(60));
        assertEquals(2, path.getStepIndex(90));
    }

    /**
     *
     */
    public void testGetLength() {
        ImPoint start = new ImPoint();
        Step[] vectors = new Step[]{EAST, EAST, EAST};
        PathOnTiles path = new PathOnTiles(start, vectors);
        assertEquals(3 * Step.TILE_DIAMETER, path.getTotalDistance(), 0.001);

    }

    /**
     *
     */
    public void testGetPoint() {
        ImPoint start = new ImPoint();
        Step[] vectors = new Step[]{EAST, EAST, EAST};
        PathOnTiles path = new PathOnTiles(start, vectors);
        ImPoint expected = new ImPoint(15, 15);
        ImPoint actual = path.getPoint(0);
        assertEquals(expected, actual);
        expected = new ImPoint(45, 15);
        actual = path.getPoint(30);
        assertEquals(expected, actual);

        expected = new ImPoint(60, 15);
        actual = path.getPoint(45);
        assertEquals(expected, actual);

    }

    /**
     *
     */
    public void testGetPointPair() {
        ImPoint start = new ImPoint();
        Step[] vectors = new Step[]{EAST, EAST, EAST};
        PathOnTiles path = new PathOnTiles(start, vectors);
        ImPoint expected15 = new ImPoint(15, 15);
        Pair<ImPoint, ImPoint> actual = path.getPoint(0, 0);
        assertEquals(expected15, actual.getA());
        assertEquals(expected15, actual.getB());
        ImPoint expected45 = new ImPoint(45, 15);
        actual = path.getPoint(0, 30);
        assertEquals(expected15, actual.getA());
        assertEquals(expected45, actual.getB());

        ImPoint expected60 = new ImPoint(60, 15);
        actual = path.getPoint(30, 45);
        assertEquals(expected45, actual.getA());
        assertEquals(expected60, actual.getB());

    }

    /**
     *
     */
    public void testSubPath() {
        ImPoint start = new ImPoint();
        Step[] vectors = new Step[]{EAST, EAST, EAST};
        PathOnTiles path = new PathOnTiles(start, vectors);

        // First check.
        Pair<FreerailsPathIterator, Integer> pathIt = path.subPath(0, path
                .getTotalDistance());
        ImPoint[] expected = {new ImPoint(15, 15), new ImPoint(45, 15),
                new ImPoint(75, 15), new ImPoint(105, 15)};
        assertEquals(Integer.valueOf(expected.length), pathIt.getB());
        checkPath(pathIt.getA(), expected);

        // Second check
        pathIt = path.subPath(3, path.getTotalDistance() - 3);
        expected = new ImPoint[]{new ImPoint(18, 15), new ImPoint(45, 15),
                new ImPoint(75, 15), new ImPoint(105, 15)};
        assertEquals(Integer.valueOf(expected.length), pathIt.getB());
        checkPath(pathIt.getA(), expected);

        // 3rd check
        double i = path.getTotalDistance() - 10;
        pathIt = path.subPath(3, i);
        expected = new ImPoint[]{new ImPoint(18, 15), new ImPoint(45, 15),
                new ImPoint(75, 15), new ImPoint(98, 15)};
        assertEquals(Integer.valueOf(expected.length), pathIt.getB());
        checkPath(pathIt.getA(), expected);

        // 4th check, with a path just 1 tile long.
        start = new ImPoint(5, 5);
        vectors = new Step[]{SOUTH_WEST};
        path = new PathOnTiles(start, vectors);
        pathIt = path.subPath(18, 24);
        IntLine line = new IntLine();
        assertTrue(pathIt.getA().hasNext());
        pathIt.getA().nextSegment(line);
        assertEquals("The length of the train.", 24, line.getLength(), 1d);
        assertFalse(pathIt.getA().hasNext());

        // 5th check, same as 2nd but with different starting position.
        vectors = new Step[]{EAST, EAST, EAST};

        start = new ImPoint(4, 7);
        path = new PathOnTiles(start, vectors);

        pathIt = path.subPath(3, path.getTotalDistance() - 3);
        expected = new ImPoint[]{new ImPoint(18, 15), new ImPoint(45, 15),
                new ImPoint(75, 15), new ImPoint(105, 15)};
        for (int j = 0; j < expected.length; j++) {
            int x = expected[j].x + start.x * TILE_DIAMETER;
            int y = expected[j].y + start.y * TILE_DIAMETER;
            expected[j] = new ImPoint(x, y);
        }
        // for (ImPoint point : expected) {
        // point.x += start.x * TILE_DIAMETER;
        // point.y += start.y * TILE_DIAMETER;
        // }
        assertEquals(Integer.valueOf(expected.length), pathIt.getB());
        checkPath(pathIt.getA(), expected);

    }

    private void checkPath(FreerailsPathIterator pathIt, ImPoint[] expected) {
        IntLine line = new IntLine();
        for (int i = 0; i < expected.length - 1; i++) {
            assertTrue(pathIt.hasNext());
            pathIt.nextSegment(line);
            assertEquals(expected[i].x, line.x1);
            assertEquals(expected[i + 1].x, line.x2);
            assertEquals(expected[i].y, line.y1);
            assertEquals(expected[i + 1].y, line.y2);
        }
        assertFalse(pathIt.hasNext());
    }

    boolean throwsException(ImPoint start, Step[] vectors) {
        try {
            new PathOnTiles(start, vectors);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     *
     */
    public void testTiles() {
        PathOnTiles path = new PathOnTiles(new ImPoint(5, 5), SOUTH_WEST,
                NORTH_EAST);
        Iterator<ImPoint> it = path.tiles();

        assertEquals(new ImPoint(5, 5), it.next());
        assertEquals(new ImPoint(4, 6), it.next());
        assertEquals(new ImPoint(5, 5), it.next());
        assertFalse(it.hasNext());

    }

}
