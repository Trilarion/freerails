package jfreerails.world.top;

import junit.framework.TestCase;

public class WorldImplTest extends TestCase {

	public WorldImplTest(String arg0) {
		super(arg0);
	}
		

	public void testGetMapWidth() {
		NewWorldImpl w = new NewWorldImpl(10, 20);
		assertEquals("We created a world with a width of 10.", 10, w.getMapWidth());
		w.setup(30, 50);
		assertEquals("We changed the world's  width to 30.", 30, w.getMapWidth());
	}

	public void testGetMapHeight() {
		NewWorldImpl w = new NewWorldImpl(10, 20);
		assertEquals("We created a world with a height of 20.", 20, w.getMapHeight());
		w.setup(30, 50);
		assertEquals("We changed the world's width to 50.", 50, w.getMapHeight());
	}
	
}
