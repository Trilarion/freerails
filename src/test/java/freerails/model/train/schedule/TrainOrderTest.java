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

package freerails.model.train.schedule;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class TrainOrderTest extends TestCase {

    public void testConsistIsCopiedAndCannotBeMutatedThroughGetter() {
        List<Integer> source = new ArrayList<>(List.of(2, 4));
        TrainOrder order = new TrainOrder(10, TrainOrder.ConsistPolicy.EXPLICIT, source, true, false);

        source.set(0, 7);
        source.add(9);

        assertEquals(List.of(2, 4), order.getConsist());
        try {
            order.getConsist().add(11);
            fail("Expected the consist to be immutable");
        } catch (UnsupportedOperationException expected) {
            // The order exposes an immutable snapshot.
        }
    }

    public void testWaitUntilFullRequiresANonemptyConsist() {
        assertFalse(new TrainOrder(10, TrainOrder.ConsistPolicy.UNCHANGED, List.of(), true, false).isWaitUntilFull());
        assertFalse(new TrainOrder(10, TrainOrder.ConsistPolicy.EXPLICIT, List.of(), true, false).isWaitUntilFull());
        assertTrue(new TrainOrder(10, TrainOrder.ConsistPolicy.EXPLICIT, List.of(2), true, false).isWaitUntilFull());
    }

    public void testAutomaticConsistOptionIsIndependentOfExplicitConsist() {
        List<Integer> consist = new ArrayList<>(List.of(2));
        TrainOrder explicitAutoOrder = new TrainOrder(10, TrainOrder.ConsistPolicy.EXPLICIT, consist, false, true);
        TrainOrder unspecifiedAutoOrder = new TrainOrder(10, TrainOrder.ConsistPolicy.UNCHANGED, List.of(), true, true);

        consist.clear();

        assertTrue(explicitAutoOrder.isAutoConsist());
        assertEquals(List.of(2), explicitAutoOrder.getConsist());
        assertTrue(unspecifiedAutoOrder.isAutoConsist());
        assertEquals(List.of(), unspecifiedAutoOrder.getConsist());
        assertEquals(TrainOrder.ConsistPolicy.UNCHANGED, unspecifiedAutoOrder.getConsistPolicy());
        assertFalse(unspecifiedAutoOrder.isWaitUntilFull());
    }

    public void testUnchangedAndExplicitlyEmptyConsistsAreDistinct() {
        TrainOrder unchanged = new TrainOrder(10, TrainOrder.ConsistPolicy.UNCHANGED, List.of(), false, false);
        TrainOrder explicitlyEmpty = new TrainOrder(10, TrainOrder.ConsistPolicy.EXPLICIT, List.of(), false, false);

        assertEquals(List.of(), unchanged.getConsist());
        assertEquals(List.of(), explicitlyEmpty.getConsist());
        assertFalse(unchanged.equals(explicitlyEmpty));
        assertFalse(unchanged.orderHasWagons());
        assertFalse(explicitlyEmpty.orderHasWagons());
    }

    public void testConsistPolicySurvivesSerialization() throws Exception {
        TrainOrder order = new TrainOrder(10, TrainOrder.ConsistPolicy.UNCHANGED, List.of(), false, true);
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream output = new java.io.ObjectOutputStream(bytes)) {
            output.writeObject(order);
        }

        TrainOrder restored;
        try (java.io.ObjectInputStream input = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (TrainOrder) input.readObject();
        }

        assertEquals(order, restored);
        assertEquals(TrainOrder.ConsistPolicy.UNCHANGED, restored.getConsistPolicy());
    }
}
