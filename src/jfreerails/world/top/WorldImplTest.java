/*
 * Created on 20-Mar-2003
 * 
 */
package jfreerails.world.top;

import jfreerails.world.common.FreerailsSerializable;
import junit.framework.TestCase;

/**
 * @author Luke
 * 
 */
public class WorldImplTest extends TestCase {

	FreerailsSerializable fs = new FreerailsSerializable(){};

	

	public void testGet(){
		World w = new WorldImpl();
		w.add(KEY.TERRAIN_TYPES, fs);
		assertEquals(w.get(KEY.TERRAIN_TYPES, 0), fs);
	}
	
	public void testConstructor(){
		
	}
	

}
