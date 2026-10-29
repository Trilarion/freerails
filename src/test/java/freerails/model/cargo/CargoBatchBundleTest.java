/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.util.Vec2D;
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

        CargoBatchBundle bundle1 = new CargoBatchBundle();
        CargoBatchBundle bundle2 = new CargoBatchBundle();

        CargoBatch batch1 = new CargoBatch(1, new Vec2D(2, 3), 4, 5);
        CargoBatch batch2 = new CargoBatch(4, new Vec2D(2, 3), 4, 5);

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

    public void testZeroAndNegativeQuantitiesHaveDefinedBehavior() {
        CargoBatch batch = new CargoBatch(1, new Vec2D(2, 3), 4, 5);
        CargoBatchBundle bundle = new CargoBatchBundle();

        assertEquals(0, bundle.getAmount(batch));
        assertFalse(bundle.contains(batch));

        bundle.addCargo(batch, 12);
        bundle.addCargo(batch, -5);
        assertEquals(7, bundle.getAmount(batch));

        bundle.addCargo(batch, -7);
        assertEquals(0, bundle.getAmount(batch));
        assertFalse(bundle.contains(batch));
        assertEquals(0, bundle.size());

        bundle.setAmount(batch, -3);
        assertTrue("Negative quantities are retained as signed corrections.", bundle.contains(batch));
        assertEquals(-3, bundle.getAmount(batch));
        bundle.setAmount(batch, 0);
        assertFalse(bundle.contains(batch));
    }

    public void testCopyAndEqualityPreserveBatchIdentityAndHashContract() {
        CargoBatch firstBatch = new CargoBatch(1, new Vec2D(2, 3), 4, 5);
        CargoBatch secondBatch = new CargoBatch(1, new Vec2D(7, 8), 9, 10);
        CargoBatch otherType = new CargoBatch(2, new Vec2D(2, 3), 4, 5);

        CargoBatchBundle source = new CargoBatchBundle();
        source.setAmount(firstBatch, 12);
        source.setAmount(secondBatch, 30);
        source.setAmount(otherType, 4);
        CargoBatchBundle copy = new CargoBatchBundle(source);

        CargoBatchBundle reverseInsertionOrder = new CargoBatchBundle();
        reverseInsertionOrder.setAmount(otherType, 4);
        reverseInsertionOrder.setAmount(secondBatch, 30);
        reverseInsertionOrder.setAmount(firstBatch, 12);
        assertEquals(source, reverseInsertionOrder);
        assertEquals(source.hashCode(), reverseInsertionOrder.hashCode());

        CargoBatchBundle differentBatchIdentity = new CargoBatchBundle();
        differentBatchIdentity.setAmount(new CargoBatch(1, new Vec2D(3, 4), 4, 5), 12);
        differentBatchIdentity.setAmount(secondBatch, 30);
        differentBatchIdentity.setAmount(otherType, 4);
        assertFalse(source.equals(differentBatchIdentity));

        source.setAmount(firstBatch, 0);
        source.addCargo(secondBatch, 1);
        assertEquals(12, copy.getAmount(firstBatch));
        assertEquals(30, copy.getAmount(secondBatch));
        assertEquals(4, copy.getAmount(otherType));
        assertEquals(3, copy.size());
        assertTrue(copy.getCargoBatches().contains(firstBatch));
        assertTrue(copy.getCargoBatches().contains(secondBatch));
        assertTrue(copy.getCargoBatches().contains(otherType));
        assertNotSame(source, copy);
        assertFalse(source.equals(copy));
    }

    public void testAmountOfTypeSumsDistinctProductionBatches() {
        CargoBatchBundle bundle = new CargoBatchBundle();
        CargoBatch firstBatch = new CargoBatch(1, new Vec2D(1, 2), 3, 4);
        CargoBatch secondBatch = new CargoBatch(1, new Vec2D(5, 6), 7, 8);
        CargoBatch thirdBatch = new CargoBatch(2, new Vec2D(1, 2), 3, 4);
        bundle.setAmount(firstBatch, 11);
        bundle.setAmount(secondBatch, 17);
        bundle.setAmount(thirdBatch, 23);

        assertEquals(28, bundle.getAmountOfType(1));
        assertEquals(23, bundle.getAmountOfType(2));
        assertEquals(0, bundle.getAmountOfType(3));
        assertEquals(3, bundle.size());
        assertTrue(bundle.contains(firstBatch));
        assertTrue(bundle.contains(secondBatch));
    }

    private void assertBundlesEqual(CargoBatchBundle bundle1, CargoBatchBundle bundle2) {

        // equals with itself
        assertEquals(bundle1, bundle1);
        assertEquals(bundle2, bundle2);

        // equals with each other
        assertEquals(bundle1, bundle2);
        assertEquals(bundle2, bundle1);

        // copy to immutable cargo batch bundle

        // equals with everything else
        assertEquals(bundle1, bundle1);
        assertEquals(bundle1, bundle1);
        assertEquals(bundle1, bundle2);

        // copy to immutable cargo batch bundle

        // equal with everything else
        assertEquals(bundle2, bundle2);
        assertEquals(bundle2, bundle1);
        assertEquals(bundle2, bundle1);
        assertEquals(bundle2, bundle2);

        // serialize
        Serializable cloneA = Utils.cloneBySerialisation(bundle1);
        Serializable cloneB = Utils.cloneBySerialisation(bundle2);
        assertEquals(cloneA, cloneB);
    }

    private void assertBundlesNotEqual(CargoBatchBundle bundle1, CargoBatchBundle bundle2) {

        // not equal with each other
        assertFalse(bundle1.equals(bundle2));
        assertFalse(bundle2.equals(bundle1));

        // copy to immutable cargo batch bundle

        // not equal with immutable bundles
        assertFalse(bundle1.equals(bundle2));
        assertFalse(((UnmodifiableCargoBatchBundle) bundle2).equals(bundle1));

        assertFalse(bundle2.equals(bundle1));
        assertFalse(((UnmodifiableCargoBatchBundle) bundle1).equals(bundle2));

        assertFalse(((UnmodifiableCargoBatchBundle) bundle2).equals(bundle1));
        assertFalse(((UnmodifiableCargoBatchBundle) bundle1).equals(bundle2));
    }
}
