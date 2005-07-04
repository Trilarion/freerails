/*
 * Created on Mar 28, 2004
 */
package jfreerails.world.cargo;

import junit.framework.TestCase;

/**
 * JUnit test for CargoType.
 * 
 * @author Luke
 * 
 */
public class CargoTypeTest extends TestCase {
	public void testCargoType() {
		// Test that invalid categories get rejected.
		try {
			new CargoType(10, "Test", "Non valid catgeory");
			fail();
		} catch (Exception e) {
		}

		try {
			new CargoType(10, "Test", "Mail");
		} catch (Exception e) {
			fail();
		}
	}
}