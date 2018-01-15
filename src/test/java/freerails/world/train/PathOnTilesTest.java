/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package freerails.world.train;

import freerails.util.LineSegment;
import freerails.util.Point2D;
import freerails.util.Pair;
import freerails.world.track.PathIterator;
import freerails.world.terrain.TileTransition;
import junit.framework.TestCase;

import java.util.Iterator;

/**
 * JUnit test for PathOnTiles.
 */
public class PathOnTilesTest extends TestCase {

    /**
     *
     */
    public void testPathOnTiles() {
        Point2D start = null;
        TileTransition[] vectors = null;
        assertTrue(throwsException(start, vectors));
        start = new Point2D();
        assertTrue(throwsException(start, vectors));
        vectors = new TileTransition[]{null, null};
        assertTrue(throwsException(start, vectors));
        vectors = new TileTransition[]{TileTransition.NORTH, TileTransition.SOUTH};
        assertFalse(throwsException(start, vectors));

    }

    /**
     *
     */
    public void testGetStepIndex() {
        Point2D start = new Point2D();
        TileTransition[] vectors = new TileTransition[]{TileTransition.SOUTH_EAST, TileTransition.EAST, TileTransition.EAST};
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
        Point2D start = new Point2D();
        TileTransition[] vectors = new TileTransition[]{TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
        PathOnTiles path = new PathOnTiles(start, vectors);
        assertEquals(3 * TileTransition.TILE_DIAMETER, path.getTotalDistance(), 0.001);

    }

    /**
     *
     */
    public void testGetPoint() {
        Point2D start = new Point2D();
        TileTransition[] vectors = new TileTransition[]{TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
        PathOnTiles path = new PathOnTiles(start, vectors);
        Point2D expected = new Point2D(15, 15);
        Point2D actual = path.getPoint(0);
        assertEquals(expected, actual);
        expected = new Point2D(45, 15);
        actual = path.getPoint(30);
        assertEquals(expected, actual);

        expected = new Point2D(60, 15);
        actual = path.getPoint(45);
        assertEquals(expected, actual);

    }

    /**
     *
     */
    public void testGetPointPair() {
        Point2D start = new Point2D();
        TileTransition[] vectors = new TileTransition[]{TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
        PathOnTiles path = new PathOnTiles(start, vectors);
        Point2D expected15 = new Point2D(15, 15);
        Pair<Point2D, Point2D> actual = path.getPoint(0, 0);
        assertEquals(expected15, actual.getA());
        assertEquals(expected15, actual.getB());
        Point2D expected45 = new Point2D(45, 15);
        actual = path.getPoint(0, 30);
        assertEquals(expected15, actual.getA());
        assertEquals(expected45, actual.getB());

        Point2D expected60 = new Point2D(60, 15);
        actual = path.getPoint(30, 45);
        assertEquals(expected45, actual.getA());
        assertEquals(expected60, actual.getB());

    }

    /**
     *
     */
    public void testSubPath() {
        Point2D start = new Point2D();
        TileTransition[] vectors = new TileTransition[]{TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
        PathOnTiles path = new PathOnTiles(start, vectors);

        // First check.
        Pair<PathIterator, Integer> pathIt = path.subPath(0, path
                .getTotalDistance());
        Point2D[] expected = {new Point2D(15, 15), new Point2D(45, 15),
                new Point2D(75, 15), new Point2D(105, 15)};
        assertEquals(Integer.valueOf(expected.length), pathIt.getB());
        checkPath(pathIt.getA(), expected);

        // Second check
        pathIt = path.subPath(3, path.getTotalDistance() - 3);
        expected = new Point2D[]{new Point2D(18, 15), new Point2D(45, 15),
                new Point2D(75, 15), new Point2D(105, 15)};
        assertEquals(Integer.valueOf(expected.length), pathIt.getB());
        checkPath(pathIt.getA(), expected);

        // 3rd check
        double i = path.getTotalDistance() - 10;
        pathIt = path.subPath(3, i);
        expected = new Point2D[]{new Point2D(18, 15), new Point2D(45, 15),
                new Point2D(75, 15), new Point2D(98, 15)};
        assertEquals(Integer.valueOf(expected.length), pathIt.getB());
        checkPath(pathIt.getA(), expected);

        // 4th check, with a path just 1 tile long.
        start = new Point2D(5, 5);
        vectors = new TileTransition[]{TileTransition.SOUTH_WEST};
        path = new PathOnTiles(start, vectors);
        pathIt = path.subPath(18, 24);
        LineSegment line = new LineSegment();
        assertTrue(pathIt.getA().hasNext());
        pathIt.getA().nextSegment(line);
        assertEquals("The length of the train.", 24, line.getLength(), 1.0d);
        assertFalse(pathIt.getA().hasNext());

        // 5th check, same as 2nd but with different starting position.
        vectors = new TileTransition[]{TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};

        start = new Point2D(4, 7);
        path = new PathOnTiles(start, vectors);

        pathIt = path.subPath(3, path.getTotalDistance() - 3);
        expected = new Point2D[]{new Point2D(18, 15), new Point2D(45, 15),
                new Point2D(75, 15), new Point2D(105, 15)};
        for (int j = 0; j < expected.length; j++) {
            int x = expected[j].x + start.x * TileTransition.TILE_DIAMETER;
            int y = expected[j].y + start.y * TileTransition.TILE_DIAMETER;
            expected[j] = new Point2D(x, y);
        }
        // for (Point2D point : expected) {
        // point.x += start.x * TILE_DIAMETER;
        // point.y += start.y * TILE_DIAMETER;
        // }
        assertEquals(Integer.valueOf(expected.length), pathIt.getB());
        checkPath(pathIt.getA(), expected);

    }

    private void checkPath(PathIterator pathIt, Point2D[] expected) {
        LineSegment line = new LineSegment();
        for (int i = 0; i < expected.length - 1; i++) {
            assertTrue(pathIt.hasNext());
            pathIt.nextSegment(line);
            assertEquals(expected[i].x, line.getX1());
            assertEquals(expected[i + 1].x, line.getX2());
            assertEquals(expected[i].y, line.getY1());
            assertEquals(expected[i + 1].y, line.getY2());
        }
        assertFalse(pathIt.hasNext());
    }

    private boolean throwsException(Point2D start, TileTransition[] vectors) {
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
        PathOnTiles path = new PathOnTiles(new Point2D(5, 5), TileTransition.SOUTH_WEST,
                TileTransition.NORTH_EAST);
        Iterator<Point2D> it = path.tiles();

        assertEquals(new Point2D(5, 5), it.next());
        assertEquals(new Point2D(4, 6), it.next());
        assertEquals(new Point2D(5, 5), it.next());
        assertFalse(it.hasNext());

    }

}
