/*
 * Created on 09-Feb-2005
 *
 */
package jfreerails.world.train;

import java.awt.Point;

import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.IntLine;
import jfreerails.world.common.OneTileMoveVector;
import junit.framework.TestCase;
import static jfreerails.world.common.OneTileMoveVector.*;

/**
 * JUnit test for PathOnTiles.
 * 
 * @author Luke
 * 
 */
public class PathOnTilesTest extends TestCase {

	public void testPathOnTiles() {
		Point start = null;
		OneTileMoveVector[] vectors = null;
		assertTrue(throwsException(start, vectors));
		start = new Point();
		assertTrue(throwsException(start, vectors));
		vectors = new OneTileMoveVector[] { null, null };
		assertTrue(throwsException(start, vectors));
		vectors = new OneTileMoveVector[] { NORTH, SOUTH };
		assertFalse(throwsException(start, vectors));

	}

	public void testGetStepIndex() {
		Point start = new Point();
		OneTileMoveVector[] vectors = new OneTileMoveVector[] { SOUTH_EAST,
				EAST, EAST };
		PathOnTiles path = new PathOnTiles(start, vectors);
		assertEquals(0, path.getStepIndex(0));
		assertEquals(0, path.getStepIndex(1));
		assertEquals(0, path.getStepIndex(30));
		assertEquals(1, path.getStepIndex(60));
		assertEquals(2, path.getStepIndex(90));
	}

	public void testGetLength() {
		Point start = new Point();
		OneTileMoveVector[] vectors = new OneTileMoveVector[] { EAST, EAST,
				EAST };
		PathOnTiles path = new PathOnTiles(start, vectors);
		assertEquals(3 * OneTileMoveVector.TILE_DIAMETER, path.getLength());

	}

	public void testGetPoint() {
		Point start = new Point();
		OneTileMoveVector[] vectors = new OneTileMoveVector[] { EAST, EAST,
				EAST };
		PathOnTiles path = new PathOnTiles(start, vectors);
		Point expected = new Point(15, 15);
		Point actual = path.getPoint(0);
		assertEquals(expected, actual);
		expected = new Point(45, 15);
		actual = path.getPoint(30);
		assertEquals(expected, actual);

		expected = new Point(60, 15);
		actual = path.getPoint(45);
		assertEquals(expected, actual);

	}

	public void testSubPath() {
		Point start = new Point();
		OneTileMoveVector[] vectors = new OneTileMoveVector[] { EAST, EAST,
				EAST };
		PathOnTiles path = new PathOnTiles(start, vectors);

		// First check.
		FreerailsPathIterator pathIt = path.subPath(0, path.getLength());
		Point[] expected = { new Point(15, 15), new Point(45, 15),
				new Point(75, 15), new Point(105, 15) };
		checkPath(pathIt, expected);

		// Second check
		pathIt = path.subPath(3, path.getLength() - 3);
		expected = new Point[] { new Point(18, 15), new Point(45, 15),
				new Point(75, 15), new Point(105, 15) };
		checkPath(pathIt, expected);

		// 3rd check
		int i = path.getLength() - 10;
		pathIt = path.subPath(3, i);
		expected = new Point[] { new Point(18, 15), new Point(45, 15),
				new Point(75, 15), new Point(98, 15) };
		checkPath(pathIt, expected);

	}

	private void checkPath(FreerailsPathIterator pathIt, Point[] expected) {
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

	boolean throwsException(Point start, OneTileMoveVector[] vectors) {
		try {
			new PathOnTiles(start, vectors);
			return false;
		} catch (Exception e) {
			return true;
		}
	}

}
