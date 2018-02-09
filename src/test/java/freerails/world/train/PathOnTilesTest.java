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
import freerails.util.TestUtils;
import freerails.world.WorldConstants;
import freerails.world.track.PathIterator;
import freerails.world.terrain.TileTransition;
import junit.framework.TestCase;

import java.util.Iterator;

/**
 * Test for PathOnTiles.
 */
public class PathOnTilesTest extends TestCase {

    /**
     *
     */
    public void testPathOnTiles() {
        final Point2D p1 = null;
        final TileTransition[] transitions1 = null;
        TestUtils.assertThrows(() -> new PathOnTiles(p1, transitions1));

        final Point2D p2 = Point2D.ZERO;
        TestUtils.assertThrows(() -> new PathOnTiles(p2, transitions1));

        final TileTransition[] transitions2 = new TileTransition[]{null, null};
        TestUtils.assertThrows(() -> new PathOnTiles(p2, transitions2));

        final TileTransition[] transitions3 = new TileTransition[]{TileTransition.NORTH, TileTransition.SOUTH};
        TestUtils.assertThrows(() -> new PathOnTiles(p2, transitions2));
        new PathOnTiles(p2, transitions3);
    }

    /**
     *
     */
    public void testGetStepIndex() {
        Point2D p1 = Point2D.ZERO;
        TileTransition[] tileTransitions = new TileTransition[]{TileTransition.SOUTH_EAST, TileTransition.EAST, TileTransition.EAST};
        PathOnTiles pathOnTiles = new PathOnTiles(p1, tileTransitions);
        assertEquals(0, pathOnTiles.getStepIndex(0));
        assertEquals(0, pathOnTiles.getStepIndex(1));
        assertEquals(0, pathOnTiles.getStepIndex(30));
        assertEquals(1, pathOnTiles.getStepIndex(60));
        assertEquals(2, pathOnTiles.getStepIndex(90));
    }

    /**
     *
     */
    public void testGetLength() {
        Point2D p1 = Point2D.ZERO;
        TileTransition[] tileTransitions = new TileTransition[]{TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
        PathOnTiles pathOnTiles = new PathOnTiles(p1, tileTransitions);
        assertEquals(3 * WorldConstants.TILE_SIZE, pathOnTiles.getTotalDistance(), 0.001);
    }

    /**
     *
     */
    public void testGetPoint() {
        Point2D p1 = Point2D.ZERO;
        TileTransition[] tileTransitions = new TileTransition[]{TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
        PathOnTiles pathOnTiles = new PathOnTiles(p1, tileTransitions);
        Point2D expected = new Point2D(15, 15);
        Point2D actual = pathOnTiles.getPoint(0);
        assertEquals(expected, actual);

        expected = new Point2D(45, 15);
        actual = pathOnTiles.getPoint(30);
        assertEquals(expected, actual);

        expected = new Point2D(60, 15);
        actual = pathOnTiles.getPoint(45);
        assertEquals(expected, actual);
    }

    /**
     *
     */
    public void testGetPointPair() {
        Point2D p1 = new Point2D();
        TileTransition[] tileTransitions = new TileTransition[]{TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
        PathOnTiles pathOnTiles = new PathOnTiles(p1, tileTransitions);
        Point2D expected1 = new Point2D(15, 15);
        Pair<Point2D, Point2D> actual = pathOnTiles.getPoint(0, 0);
        assertEquals(expected1, actual.getA());
        assertEquals(expected1, actual.getB());

        Point2D expected2 = new Point2D(45, 15);
        actual = pathOnTiles.getPoint(0, 30);
        assertEquals(expected1, actual.getA());
        assertEquals(expected2, actual.getB());

        Point2D expected3 = new Point2D(60, 15);
        actual = pathOnTiles.getPoint(30, 45);
        assertEquals(expected2, actual.getA());
        assertEquals(expected3, actual.getB());
    }

    /**
     *
     */
    public void testSubPath() {
        Point2D p1 = new Point2D();
        TileTransition[] tileTransitions = new TileTransition[]{TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
        PathOnTiles pathOnTiles = new PathOnTiles(p1, tileTransitions);

        // First check
        Pair<PathIterator, Integer> pathIt = pathOnTiles.subPath(0, pathOnTiles.getTotalDistance());
        Point2D[] expected = {new Point2D(15, 15), new Point2D(45, 15), new Point2D(75, 15), new Point2D(105, 15)};
        assertEquals(Integer.valueOf(expected.length), pathIt.getB());
        checkPath(pathIt.getA(), expected);

        // Second check
        pathIt = pathOnTiles.subPath(3, pathOnTiles.getTotalDistance() - 3);
        expected = new Point2D[]{new Point2D(18, 15), new Point2D(45, 15), new Point2D(75, 15), new Point2D(105, 15)};
        assertEquals(Integer.valueOf(expected.length), pathIt.getB());
        checkPath(pathIt.getA(), expected);

        // 3rd check
        double i = pathOnTiles.getTotalDistance() - 10;
        pathIt = pathOnTiles.subPath(3, i);
        expected = new Point2D[]{new Point2D(18, 15), new Point2D(45, 15), new Point2D(75, 15), new Point2D(98, 15)};
        assertEquals(Integer.valueOf(expected.length), pathIt.getB());
        checkPath(pathIt.getA(), expected);

        // 4th check, with a path just 1 tile long.
        p1 = new Point2D(5, 5);
        tileTransitions = new TileTransition[]{TileTransition.SOUTH_WEST};
        pathOnTiles = new PathOnTiles(p1, tileTransitions);
        pathIt = pathOnTiles.subPath(18, 24);
        LineSegment line = new LineSegment();
        assertTrue(pathIt.getA().hasNext());
        pathIt.getA().nextSegment(line);
        assertEquals("The length of the train.", 24, line.getLength(), 1.0d);
        assertFalse(pathIt.getA().hasNext());

        // 5th check, same as 2nd but with different starting position.
        tileTransitions = new TileTransition[]{TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};

        p1 = new Point2D(4, 7);
        pathOnTiles = new PathOnTiles(p1, tileTransitions);

        pathIt = pathOnTiles.subPath(3, pathOnTiles.getTotalDistance() - 3);
        expected = new Point2D[]{new Point2D(18, 15), new Point2D(45, 15),
                new Point2D(75, 15), new Point2D(105, 15)};
        for (int j = 0; j < expected.length; j++) {
            int x = expected[j].x + p1.x * WorldConstants.TILE_SIZE;
            int y = expected[j].y + p1.y * WorldConstants.TILE_SIZE;
            expected[j] = new Point2D(x, y);
        }
        assertEquals(Integer.valueOf(expected.length), pathIt.getB());
        checkPath(pathIt.getA(), expected);
    }

    private void checkPath(PathIterator pathIterator, Point2D[] expected) {
        LineSegment segment = new LineSegment();
        for (int i = 0; i < expected.length - 1; i++) {
            assertTrue(pathIterator.hasNext());
            pathIterator.nextSegment(segment);
            assertEquals(expected[i].x, segment.getX1());
            assertEquals(expected[i + 1].x, segment.getX2());
            assertEquals(expected[i].y, segment.getY1());
            assertEquals(expected[i + 1].y, segment.getY2());
        }
        assertFalse(pathIterator.hasNext());
    }

    /**
     *
     */
    public void testTiles() {
        Point2D p1 = new Point2D(5,5);
        PathOnTiles pathOnTiles = new PathOnTiles(p1, TileTransition.SOUTH_WEST, TileTransition.NORTH_EAST);
        Iterator<Point2D> it = pathOnTiles.tilesIterator();

        assertEquals(p1, it.next());
        assertEquals(new Point2D(4, 6), it.next());
        assertEquals(p1, it.next());
        assertFalse(it.hasNext());
    }

}
