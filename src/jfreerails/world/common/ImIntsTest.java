/*
 * Created on 06-Jul-2005
 *
 */
package jfreerails.world.common;

import junit.framework.TestCase;

public class ImIntsTest extends TestCase {

	/*
	 * Test method for 'jfreerails.world.common.ImInts.append(int...)'
	 */
	public void testAppend() {

		int[] a = { 1, 2, 3 };
		int[] b = { 4, 5, 6, 7 };
		int[] c = { 1, 2, 3, 4, 5, 6, 7 };
		ImInts ai = new ImInts(a);
		ImInts ci = new ImInts(c);
		assertFalse(ci.equals(ai));
		assertEquals(ci, ai.append(b));

	}
	
	public void testEquals(){
		int[] a = { 1, 2, 3 };
		int[] b = { 1, 2, 3 };
		ImInts ai = new ImInts(a);
		ImInts bi = new ImInts(b);
		assertEquals(ai, bi);
		ImInts ci = new ImInts(1, 2, 3 );
		assertEquals(ai, ci);
		
	}

}
