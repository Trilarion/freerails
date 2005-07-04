package jfreerails.controller;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.IntLine;
import junit.framework.TestCase;

/**
 * JUnit test for ToAndFroPathIteratorTest.
 * 
 * @author Luke Lindsay 30-Oct-2002
 * 
 */
public class ToAndFroPathIteratorTest extends TestCase {
	public ToAndFroPathIteratorTest(String arg0) {
		super(arg0);
	}

	public void testNextSegment() {
		List<Point> l = new ArrayList<Point>();
		IntLine line = new IntLine();

		l.add(new Point(0, 1));
		l.add(new Point(10, 11));
		l.add(new Point(20, 22));

		FreerailsPathIterator it = new ToAndFroPathIterator(l);

		assertTrue(it.hasNext());
		it.nextSegment(line);
		assertLineEquals(0, 1, 10, 11, line);

		assertTrue(it.hasNext());
		it.nextSegment(line);
		assertLineEquals(10, 11, 20, 22, line);

		assertTrue(it.hasNext());
		it.nextSegment(line);
		assertLineEquals(20, 22, 10, 11, line);

		assertTrue(it.hasNext());
		it.nextSegment(line);
		assertLineEquals(10, 11, 0, 1, line);

		assertTrue(it.hasNext());
		it.nextSegment(line);
		assertLineEquals(0, 1, 10, 11, line);

		assertTrue(it.hasNext());
		it.nextSegment(line);
		assertLineEquals(10, 11, 20, 22, line);
	}

	private void assertLineEquals(int x1, int y1, int x2, int y2, IntLine line) {
		assertEquals(x1, line.x1);
		assertEquals(x2, line.x2);
		assertEquals(y1, line.y1);
		assertEquals(y2, line.y2);
	}
}