/*
 * Created on 12-Aug-2005
 *
 */
package jfreerails.world.cargo;

import java.io.Serializable;

import jfreerails.util.Utils;
import junit.framework.TestCase;

public class CargoBundleTest extends TestCase {

	public void testEquals() {
		MutableCargoBundle bundle1 = new MutableCargoBundle();
		MutableCargoBundle bundle2 = new MutableCargoBundle();
		CargoBatch batch1 = new CargoBatch(1, 2, 3, 4, 5);
		CargoBatch batch2 = new CargoBatch(4, 2, 3, 4, 5);
		int q1 = 10;
		int q2 = 20;
		bundle1.addCargo(batch1, q1);
		bundle1.addCargo(batch2, q2);
		assertBundlesNotEqual(bundle1, bundle2);
		bundle2.addCargo(batch2, q2);
		assertBundlesNotEqual(bundle1, bundle2);
		bundle2.addCargo(batch1, q1);
		assertBundlesEqual(bundle1, bundle2);
	}

	private void assertBundlesEqual(MutableCargoBundle a, MutableCargoBundle b) {
		assertEquals(a, b);
		assertEquals(a, a);
		assertEquals(b, a);
		ImmutableCargoBundle immA = a.toImmutableCargoBundle();
		assertEquals(immA, immA);
		assertEquals(immA, b);

		ImmutableCargoBundle immB = b.toImmutableCargoBundle();
		assertEquals(immA, immB);
		Serializable cloneA = Utils.cloneBySerialisation(immA);
		Serializable cloneB = Utils.cloneBySerialisation(immB);
		assertEquals(cloneA, cloneB);
		assertEquals(a, immB);
	}

	private void assertBundlesNotEqual(MutableCargoBundle a,
			MutableCargoBundle b) {
		assertFalse(a.equals(b));
		assertFalse(b.equals(a));
		assertFalse(a.toImmutableCargoBundle().equals(b));
		assertFalse(a.toImmutableCargoBundle().equals(
				b.toImmutableCargoBundle()));
		assertFalse(a.equals(b.toImmutableCargoBundle()));
	}

}
