/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
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
package freerails.model.cargo;

import freerails.util.Vector2D;
import freerails.util.Utils;
import junit.framework.TestCase;

import java.io.Serializable;

/**
 * Test for CargoBatchBundle
 */
public class CargoBatchBundleTest extends TestCase {

    /**
     * Creates two bundles with different and equal cargo and tests the equals method (also for immutable bundles).
     */
    public void testEquals() {

        MutableCargoBatchBundle bundle1 = new MutableCargoBatchBundle();
        MutableCargoBatchBundle bundle2 = new MutableCargoBatchBundle();

        CargoBatch batch1 = new CargoBatch(1, new Vector2D(2, 3), 4, 5);
        CargoBatch batch2 = new CargoBatch(4, new Vector2D(2, 3), 4, 5);

        // add some cargo to the bundle1
        int quantity1 = 10;
        bundle1.addCargo(batch1, quantity1);
        int quantity2 = 20;
        bundle1.addCargo(batch2, quantity2);

        // should not be equal
        assertBundlesNotEqual(bundle1, bundle2);

        // add a bit of cargo to bundle2
        bundle2.addCargo(batch2, quantity2);

        // still not equal
        assertBundlesNotEqual(bundle1, bundle2);

        // now add more so that everything in bundle1 is also in bundle2
        bundle2.addCargo(batch1, quantity1);

        // should be equal
        assertBundlesEqual(bundle1, bundle2);
    }

    private void assertBundlesEqual(MutableCargoBatchBundle bundle1, MutableCargoBatchBundle bundle2) {

        // equals with itself
        assertEquals(bundle1, bundle1);
        assertEquals(bundle2, bundle2);

        // equals with each other
        assertEquals(bundle1, bundle2);
        assertEquals(bundle2, bundle1);

        // copy to immutable cargo batch bundle
        ImmutableCargoBatchBundle immutableBundle1 = bundle1.toImmutableCargoBundle();

        // equals with everything else
        assertEquals(immutableBundle1, immutableBundle1);
        assertEquals(immutableBundle1, bundle1);
        assertEquals(immutableBundle1, bundle2);

        // copy to immutable cargo batch bundle
        ImmutableCargoBatchBundle immutableBundle2 = bundle2.toImmutableCargoBundle();

        // equal with everything else
        assertEquals(immutableBundle2, immutableBundle2);
        assertEquals(immutableBundle2, immutableBundle1);
        assertEquals(immutableBundle2, bundle1);
        assertEquals(immutableBundle2, bundle2);

        // serialize
        Serializable cloneA = Utils.cloneBySerialisation(immutableBundle1);
        Serializable cloneB = Utils.cloneBySerialisation(immutableBundle2);
        assertEquals(cloneA, cloneB);
    }

    private void assertBundlesNotEqual(MutableCargoBatchBundle bundle1, MutableCargoBatchBundle bundle2) {

        // not equal with each other
        assertFalse(bundle1.equals(bundle2));
        assertFalse(bundle2.equals(bundle1));

        // copy to immutable cargo batch bundle
        ImmutableCargoBatchBundle immutableBundle1 = bundle1.toImmutableCargoBundle();
        ImmutableCargoBatchBundle immutableBundle2 = bundle2.toImmutableCargoBundle();

        // not equal with immutable bundles
        assertFalse(bundle1.equals(immutableBundle2));
        assertFalse(immutableBundle2.equals(bundle1));

        assertFalse(bundle2.equals(immutableBundle1));
        assertFalse(immutableBundle1.equals(bundle2));

        assertFalse(immutableBundle2.equals(immutableBundle1));
        assertFalse(immutableBundle1.equals(immutableBundle2));
    }
}
