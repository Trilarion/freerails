
package jfreerails.server;

import jfreerails.world.top.SKEY;
import jfreerails.world.top.World;
import junit.framework.TestCase;
import static jfreerails.server.MapFixtureFactory2.*;
/**
 
 * 
 * @author Luke Lindsay
 *
  */
public class MapFixtureFactory2Test extends TestCase {

	public void testGetCopy(){
		World w1, w2;
		w1 = getCopy();
		assertNotNull(w1);
		w2 = getCopy();
		assertNotNull(w2);
		assertNotSame(w1, w2);
		assertEquals(w1, w2);
				
	}
	
	public void testLists(){
		World w1;
		w1 = getCopy();
		assertTrue(w1.size(SKEY.CARGO_TYPES) > 0);
		assertTrue(w1.size(SKEY.TRACK_RULES) > 0);
		assertTrue(w1.size(SKEY.TERRAIN_TYPES) > 0);
		
	}
	
	public void testMap(){
		World w1;
		w1 = getCopy();
		assertEquals(w1.getMapWidth(), 25);
		assertEquals(w1.getMapWidth(), 25);
	
	}
	
	public void testPlayers(){
		World w1;
		w1 = getCopy();
		assertEquals(4, w1.getNumberOfPlayers());
	}
				
}
