package jfreerails.world.common;

import junit.framework.TestCase;

public class OneTileMoveVectorTest extends TestCase {

	final OneTileMoveVector n = OneTileMoveVector.NORTH;
	final OneTileMoveVector ne = OneTileMoveVector.NORTH_EAST;
	final OneTileMoveVector e = OneTileMoveVector.EAST;
	final OneTileMoveVector se = OneTileMoveVector.SOUTH_EAST;

	final OneTileMoveVector s = OneTileMoveVector.SOUTH;
	final OneTileMoveVector sw = OneTileMoveVector.SOUTH_WEST;
	final OneTileMoveVector w = OneTileMoveVector.WEST;
	final OneTileMoveVector nw = OneTileMoveVector.NORTH_WEST;

	public OneTileMoveVectorTest(String arg0) {
		super(arg0);
	}

	public void testGetRotatedInstance() {

		assertEquals(
			"Rotating by 0 degrees should have no effect",
			n.getRotatedInstance(Rotation.BY_0_DEGREES),
			n);
		assertEquals(n.getRotatedInstance(Rotation.BY_45_DEGREES), ne);
		assertEquals(n.getRotatedInstance(Rotation.BY_90_DEGREES), e);
		assertEquals(n.getRotatedInstance(Rotation.BY_135_DEGREES), se);
		assertEquals(n.getRotatedInstance(Rotation.BY_180_DEGREES), s);

		assertEquals(nw.getRotatedInstance(Rotation.BY_45_DEGREES), n);
		assertEquals(nw.getRotatedInstance(Rotation.BY_90_DEGREES), ne);
		assertEquals(nw.getRotatedInstance(Rotation.BY_135_DEGREES), e);
		assertEquals(nw.getRotatedInstance(Rotation.BY_180_DEGREES), se);
		assertTrue(n != ne);
	}
	public void testGetDirection() {
		double d = 0;
		assertTrue(d == n.getDirection());
		d = 2 * Math.PI / 8 * 1;
		assertTrue(d == ne.getDirection());

	}

	public void testGetNearestVector() {

		//Each vector should be the nearest to itself!
		OneTileMoveVector[] vectors = OneTileMoveVector.getList();
		for (int i = 0; i < vectors.length; i++) {
			OneTileMoveVector v = vectors[i];	
			OneTileMoveVector v2 = OneTileMoveVector.getNearestVector(v.deltaX, v.deltaY);
			assertEquals(v, v2);
		}

		assertNearest(n, 0, -1);
		assertNearest(n, 0, -99);
		assertNearest(n, 2, -5);
		assertNearest(n, -2, -5);
		assertNearest(s, 2, 5);

		assertNearest(w, -5, -1);

		assertNearest(sw, -4, 3);

		assertNearest(ne, 10, -6);
        
        assertNearest(ne, 10, -6);

	}

	private void assertNearest(OneTileMoveVector v, int dx, int dy) {
		OneTileMoveVector v2 = OneTileMoveVector.getNearestVector(dx, dy);
		assertEquals(v, v2);
	}

}
