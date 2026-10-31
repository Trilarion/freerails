/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
package freerails.model.operations.train;

import freerails.util.Segment;
import freerails.util.Vector2D;
import freerails.util.Pair;
import freerails.util.TestUtils;
import freerails.model.ModelConstants;
import freerails.model.track.PathIterator;
import freerails.model.terrain.TileTransition;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Test for PathOnTiles.
 */
public class PathOnTilesTest extends TestCase {

    /**
     * Tests path on tiles behavior.
     */
    public void testPathOnTiles() {
        final Vector2D p1 = null;
        final TileTransition[] transitions1 = null;
        TestUtils.assertThrows(() -> new PathOnTiles(p1, transitions1));

        final Vector2D p2 = Vector2D.ZERO;
        TestUtils.assertThrows(() -> new PathOnTiles(p2, transitions1));

        final TileTransition[] transitions2 = new TileTransition[]{null, null};
        TestUtils.assertThrows(() -> new PathOnTiles(p2, transitions2));

        final TileTransition[] transitions3 = new TileTransition[]{TileTransition.NORTH, TileTransition.SOUTH};
        TestUtils.assertThrows(() -> new PathOnTiles(p2, transitions2));
        new PathOnTiles(p2, transitions3);
    }

    /**
     * Tests get step index behavior.
     */
    public void testGetStepIndex() {
        Vector2D p1 = Vector2D.ZERO;
        TileTransition[] tileTransitions = new TileTransition[]{TileTransition.SOUTH_EAST, TileTransition.EAST, TileTransition.EAST};
        PathOnTiles pathOnTiles = new PathOnTiles(p1, tileTransitions);
        assertEquals(0, pathOnTiles.getStepIndex(0));
        assertEquals(0, pathOnTiles.getStepIndex(1));
        assertEquals(0, pathOnTiles.getStepIndex(100));
        assertEquals(1, pathOnTiles.getStepIndex(200));
        assertEquals(2, pathOnTiles.getStepIndex(300));
    }

    /**
     * Tests get length behavior.
     */
    public void testGetLength() {
        Vector2D p1 = Vector2D.ZERO;
        TileTransition[] tileTransitions = new TileTransition[]{TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
        PathOnTiles pathOnTiles = new PathOnTiles(p1, tileTransitions);
        assertEquals(3 * ModelConstants.MODEL_UNITS_PER_TILE, pathOnTiles.getTotalDistance(), 0.001);
    }

    /**
     * Tests get point behavior.
     */
    public void testGetPoint() {
        Vector2D p1 = Vector2D.ZERO;
        TileTransition[] tileTransitions = new TileTransition[]{TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
        PathOnTiles pathOnTiles = new PathOnTiles(p1, tileTransitions);
        Vector2D expected = new Vector2D(50, 50);
        Vector2D actual = pathOnTiles.getPoint(0);
        assertEquals(expected, actual);

        expected = new Vector2D(150, 50);
        actual = pathOnTiles.getPoint(100);
        assertEquals(expected, actual);

        assertEquals(new Vector2D(51, 50), pathOnTiles.getPoint(1));

        expected = new Vector2D(200, 50);
        actual = pathOnTiles.getPoint(150);
        assertEquals(expected, actual);

        PathOnTiles diagonal = new PathOnTiles(Vector2D.ZERO, TileTransition.NORTH_EAST);
        assertEquals(new Vector2D(100, 0), diagonal.getPoint(ModelConstants.MODEL_DIAGONAL_TILE_LENGTH / 2));
    }

    /**
     * Tests get point pair behavior.
     */
    public void testGetPointPair() {
        Vector2D p1 = new Vector2D();
        TileTransition[] tileTransitions = new TileTransition[]{TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
        PathOnTiles pathOnTiles = new PathOnTiles(p1, tileTransitions);
        Vector2D expected1 = new Vector2D(50, 50);
        Pair<Vector2D, Vector2D> actual = pathOnTiles.getPoint(0, 0);
        assertEquals(expected1, actual.a());
        assertEquals(expected1, actual.b());

        Vector2D expected2 = new Vector2D(150, 50);
        actual = pathOnTiles.getPoint(0, 100);
        assertEquals(expected1, actual.a());
        assertEquals(expected2, actual.b());

        Vector2D expected3 = new Vector2D(200, 50);
        actual = pathOnTiles.getPoint(100, 150);
        assertEquals(expected2, actual.a());
        assertEquals(expected3, actual.b());
    }

    /**
     * Tests sub path behavior.
     */
    public void testSubPath() {
        Vector2D p1 = new Vector2D();
        TileTransition[] tileTransitions = new TileTransition[]{TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
        PathOnTiles pathOnTiles = new PathOnTiles(p1, tileTransitions);

        // First check
        Pair<PathIterator, Integer> pathIt = pathOnTiles.subPath(0, pathOnTiles.getTotalDistance());
        Vector2D[] expected = {new Vector2D(50, 50), new Vector2D(150, 50), new Vector2D(250, 50), new Vector2D(350, 50)};
        assertEquals(Integer.valueOf(expected.length), pathIt.b());
        checkPath(pathIt.a(), expected);

        // Second check
        pathIt = pathOnTiles.subPath(10, pathOnTiles.getTotalDistance() - 10);
        expected = new Vector2D[]{new Vector2D(60, 50), new Vector2D(150, 50), new Vector2D(250, 50), new Vector2D(350, 50)};
        assertEquals(Integer.valueOf(expected.length), pathIt.b());
        checkPath(pathIt.a(), expected);

        // 3rd check
        double i = pathOnTiles.getTotalDistance() - ModelConstants.MODEL_UNITS_PER_TILE / 3.0;
        pathIt = pathOnTiles.subPath(10, i);
        expected = new Vector2D[]{new Vector2D(60, 50), new Vector2D(150, 50), new Vector2D(250, 50), new Vector2D(327, 50)};
        assertEquals(Integer.valueOf(expected.length), pathIt.b());
        checkPath(pathIt.a(), expected);

        // 4th check, with a path just 1 tile long.
        p1 = new Vector2D(5, 5);
        tileTransitions = new TileTransition[]{TileTransition.SOUTH_WEST};
        pathOnTiles = new PathOnTiles(p1, tileTransitions);
        pathIt = pathOnTiles.subPath(60, 80);
        Segment line = null;
        assertTrue(pathIt.a().hasNext());
        line = pathIt.a().nextSegment();
        assertEquals("The length of the train.", 80, line.getLength(), 1.0d);
        assertFalse(pathIt.a().hasNext());

        // 5th check, same as 2nd but with different starting position.
        tileTransitions = new TileTransition[]{TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};

        p1 = new Vector2D(4, 7);
        pathOnTiles = new PathOnTiles(p1, tileTransitions);

        pathIt = pathOnTiles.subPath(10, pathOnTiles.getTotalDistance() - 10);
        expected = new Vector2D[]{new Vector2D(60, 50), new Vector2D(150, 50),
                new Vector2D(250, 50), new Vector2D(350, 50)};
        for (int j = 0; j < expected.length; j++) {
            int x = expected[j].x() + p1.x() * ModelConstants.MODEL_UNITS_PER_TILE;
            int y = expected[j].y() + p1.y() * ModelConstants.MODEL_UNITS_PER_TILE;
            expected[j] = new Vector2D(x, y);
        }
        assertEquals(Integer.valueOf(expected.length), pathIt.b());
        checkPath(pathIt.a(), expected);
    }

    /**
     * Checks path.
     * @param pathIterator the path iterator to use in the test.
     * @param expected the expected value.
     */
    private void checkPath(PathIterator pathIterator, Vector2D[] expected) {
        Segment segment = null;
        for (int i = 0; i < expected.length - 1; i++) {
            assertTrue(pathIterator.hasNext());
            segment = pathIterator.nextSegment();
            assertEquals(expected[i], segment.a());
            assertEquals(expected[i + 1], segment.b());
        }
        assertFalse(pathIterator.hasNext());
    }

    /**
     * Tests tiles behavior.
     */
    public void testTiles() {
        Vector2D p1 = new Vector2D(5,5);
        PathOnTiles pathOnTiles = new PathOnTiles(p1, TileTransition.SOUTH_WEST, TileTransition.NORTH_EAST);
        Iterator<Vector2D> it = pathOnTiles.tilesIterator();

        assertEquals(p1, it.next());
        assertEquals(new Vector2D(4, 6), it.next());
        assertEquals(p1, it.next());
        assertFalse(it.hasNext());
    }

    /**
     * Tests mutable transition input cannot change path value behavior.
     */
    public void testMutableTransitionInputCannotChangePathValue() {
        List<TileTransition> transitions =
                new ArrayList<>(List.of(TileTransition.EAST, TileTransition.SOUTH));
        PathOnTiles path = new PathOnTiles(Vector2D.ZERO, transitions);
        PathOnTiles originalValue = new PathOnTiles(Vector2D.ZERO, TileTransition.EAST, TileTransition.SOUTH);
        int originalHashCode = path.hashCode();
        double originalDistance = path.getTotalDistance();

        transitions.clear();
        transitions.add(TileTransition.NORTH);

        assertEquals(2, path.steps());
        assertEquals(TileTransition.EAST, path.getStep(0));
        assertEquals(TileTransition.SOUTH, path.getStep(1));
        assertEquals(originalDistance, path.getTotalDistance(), 0.0);
        assertEquals(originalValue, path);
        assertEquals(originalHashCode, path.hashCode());
    }

}
