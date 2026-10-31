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
        TrainOrder order = TrainOrder.specifiedConsist(10, source, true);

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

    public void testWaitUntilFullOnlyAppliesToANonemptySpecifiedConsist() {
        assertFalse(TrainOrder.keepConsist(10).isWaitUntilFull());
        assertFalse(TrainOrder.specifiedConsist(10, List.of(), true).isWaitUntilFull());
        assertTrue(TrainOrder.specifiedConsist(10, List.of(2), true).isWaitUntilFull());
    }

    public void testLoadingPoliciesAreMutuallyExclusive() {
        TrainOrder keep = TrainOrder.keepConsist(10);
        TrainOrder specified = TrainOrder.specifiedConsist(10, List.of(2), false);
        TrainOrder automatic = TrainOrder.autoConsist(10);

        assertEquals(TrainOrder.LoadingPolicy.KEEP_CONSIST, keep.getLoadingPolicy());
        assertEquals(TrainOrder.LoadingPolicy.SPECIFIED_CONSIST, specified.getLoadingPolicy());
        assertEquals(TrainOrder.LoadingPolicy.AUTO_CONSIST, automatic.getLoadingPolicy());
        assertFalse(keep.hasSpecifiedConsist());
        assertTrue(specified.hasSpecifiedConsist());
        assertTrue(automatic.isAutoConsist());
        assertFalse(automatic.isWaitUntilFull());

        try {
            automatic.withPolicy(TrainOrder.LoadingPolicy.AUTO_CONSIST, List.of(), true);
            fail("An automatic consist cannot wait until full");
        } catch (IllegalArgumentException expected) {
            // Invalid combinations cannot be represented.
        }
    }

    public void testExplicitlyEmptyConsistIsDistinctFromKeepingConsist() {
        TrainOrder unchanged = TrainOrder.keepConsist(10);
        TrainOrder explicitlyEmpty = TrainOrder.specifiedConsist(10, List.of(), false);

        assertEquals(List.of(), unchanged.getConsist());
        assertEquals(List.of(), explicitlyEmpty.getConsist());
        assertFalse(unchanged.equals(explicitlyEmpty));
        assertFalse(unchanged.orderHasWagons());
        assertFalse(explicitlyEmpty.orderHasWagons());
    }

    public void testStationAndPolicyUpdatesPreserveOneShotMarker() {
        TrainOrder urgent = TrainOrder.specifiedConsist(10, List.of(2), true).asOneShot();

        TrainOrder moved = urgent.withStation(20)
                .withPolicy(TrainOrder.LoadingPolicy.AUTO_CONSIST, List.of(), false);

        assertEquals(20, moved.getStationId());
        assertEquals(TrainOrder.LoadingPolicy.AUTO_CONSIST, moved.getLoadingPolicy());
        assertTrue(moved.isOneShot());
    }

    public void testOrderSurvivesSerialization() throws Exception {
        TrainOrder order = TrainOrder.autoConsist(10).asOneShot();
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
        assertTrue(restored.isOneShot());
    }
}
