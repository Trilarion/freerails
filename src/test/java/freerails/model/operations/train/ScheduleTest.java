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

package freerails.model.operations.train;

import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.schedule.TrainOrder;
import freerails.model.operations.train.schedule.UnmodifiableSchedule;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Tests schedule behavior.
 */
public class ScheduleTest extends TestCase {

    /**
     * Tests empty and cyclic schedule behavior.
     */
    public void testEmptyAndCyclicSchedule() {
        ScheduleState schedule = new ScheduleState();
        assertEquals(0, schedule.getNumberOfOrders());
        assertEquals(-1, schedule.getCurrentOrderIndex());
        assertEquals(-1, schedule.getNextStationId());

        schedule = schedule.addOrder(order(10)).addOrder(order(20)).addOrder(order(30));
        assertSelected(schedule, 0, 10);
        schedule = schedule.gotoNextStation();
        assertSelected(schedule, 1, 20);
        schedule = schedule.gotoNextStation();
        assertSelected(schedule, 2, 30);
        schedule = schedule.gotoNextStation();
        assertSelected(schedule, 0, 10);
    }

    /**
     * Tests schedule operations return independent snapshots behavior.
     */
    public void testScheduleOperationsReturnIndependentSnapshots() {
        ScheduleState initial = scheduleWithOrders(10, 20);
        ScheduleState selected = initial.setOrderToGoto(1);
        ScheduleState updated = selected.setOrder(1, order(25));
        ScheduleState advanced = selected.gotoNextStation();

        assertSelected(initial, 0, 10);
        assertSelected(selected, 1, 20);
        assertSelected(updated, 1, 25);
        assertSelected(advanced, 0, 10);
        assertNotSame(initial, selected);
    }

    /**
     * Tests schedule defensively copies constructor array and hash includes orders behavior.
     */
    public void testScheduleDefensivelyCopiesConstructorArrayAndHashIncludesOrders() {
        TrainOrder[] orders = {order(10), order(20)};
        ScheduleState schedule = new ScheduleState(orders, 0);
        orders[0] = order(99);

        assertEquals(10, schedule.getNextStationId());
        assertEquals(schedule, scheduleWithOrders(10, 20));
        assertEquals(schedule.hashCode(), scheduleWithOrders(10, 20).hashCode());
        assertFalse(schedule.equals(scheduleWithOrders(10, 30)));
        assertFalse(schedule.hashCode() == scheduleWithOrders(10, 30).hashCode());
    }

    /**
     * Tests schedule serialization restores immutable snapshot behavior.
     * @throws Exception if the test schedule serialization restores immutable snapshot operation fails.
     */
    public void testScheduleSerializationRestoresImmutableSnapshot() throws Exception {
        ScheduleState original = scheduleWithOrders(10, 20).setOrderToGoto(1);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(original);
        }

        ScheduleState restored;
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (ScheduleState) input.readObject();
        }

        assertEquals(original, restored);
        assertEquals(original.hashCode(), restored.hashCode());
        assertSelected(restored.gotoNextStation(), 0, 10);
        assertSelected(restored, 1, 20);
    }

    /**
     * Tests adding removing and moving stops preserves selected stop behavior.
     */
    public void testAddingRemovingAndMovingStopsPreservesSelectedStop() {
        ScheduleState schedule = scheduleWithOrders(10, 20, 30);
        schedule = schedule.setOrderToGoto(1);

        schedule = schedule.addOrder(1, order(15));
        assertSelected(schedule, 2, 20);
        schedule = schedule.addOrder(2, order(25));
        assertSelected(schedule, 3, 20);
        schedule = schedule.addOrder(order(50));
        assertSelected(schedule, 3, 20);
        schedule = schedule.removeOrder(1);
        assertSelected(schedule, 2, 20);
        schedule = schedule.pullUp(3);
        assertSelected(schedule, 3, 20);
        schedule = schedule.pushDown(2);
        assertSelected(schedule, 2, 20);
        schedule = schedule.removeOrder(2);
        assertSelected(schedule, 2, 30);
    }

    /**
     * Tests urgent stop is inserted before and consumed then route resumes behavior.
     */
    public void testUrgentStopIsInsertedBeforeAndConsumedThenRouteResumes() {
        ScheduleState schedule = scheduleWithOrders(10, 20, 30, 40).setOrderToGoto(2);

        schedule = schedule.addUrgentOrder(order(90));
        assertEquals(5, schedule.getNumberOfOrders());
        assertEquals(2, schedule.getCurrentOrderIndex());
        assertEquals(90, schedule.getNextStationId());
        assertTrue(schedule.getOrder(2).isOneShot());
        assertFalse(schedule.canAddUrgentOrder());

        schedule = schedule.gotoNextStation();
        assertEquals(4, schedule.getNumberOfOrders());
        assertSelected(schedule, 2, 30);
        assertFalse(schedule.getOrder(2).isOneShot());
        assertSelected(schedule.gotoNextStation(), 3, 40);
    }

    /**
     * Tests urgent stop can be consumed from an otherwise empty schedule behavior.
     */
    public void testUrgentStopCanBeConsumedFromAnOtherwiseEmptySchedule() {
        ScheduleState schedule = new ScheduleState().addUrgentOrder(order(90));
        assertSelected(schedule, 0, 90);

        schedule = schedule.gotoNextStation();
        assertEquals(0, schedule.getNumberOfOrders());
        assertEquals(-1, schedule.getCurrentOrderIndex());
        assertEquals(-1, schedule.getNextStationId());
    }

    /**
     * Tests urgent stop can be edited without losing one shot marker behavior.
     */
    public void testUrgentStopCanBeEditedWithoutLosingOneShotMarker() {
        ScheduleState schedule = scheduleWithOrders(10, 20).addUrgentOrder(order(90));
        schedule = schedule.setOrder(0, order(91));
        assertEquals(91, schedule.getNextStationId());
        assertTrue(schedule.getOrder(0).isOneShot());

        schedule = schedule.gotoNextStation();
        assertEquals(10, schedule.getNextStationId());
        assertFalse(schedule.getOrder(schedule.getCurrentOrderIndex()).isOneShot());
    }

    /**
     * Tests invalid movement indexes are rejected behavior.
     */
    public void testInvalidMovementIndexesAreRejected() {
        ScheduleState schedule = scheduleWithOrders(10, 20);
        assertFalse(schedule.canPullUp(-1));
        assertFalse(schedule.canPullUp(schedule.getNumberOfOrders()));
        assertFalse(schedule.canPushDown(-1));
        assertFalse(schedule.canPushDown(schedule.getNumberOfOrders()));
    }

    /**
     * Tests removing all stops at station keeps current index valid behavior.
     */
    public void testRemovingAllStopsAtStationKeepsCurrentIndexValid() {
        ScheduleState schedule = scheduleWithOrders(20, 10, 30, 10).setOrderToGoto(2);
        schedule = schedule.addUrgentOrder(order(10));

        schedule = schedule.removeAllStopsAtStation(10);

        assertEquals(2, schedule.getNumberOfOrders());
        assertFalse(schedule.stopsAtStation(10));
        assertTrue(schedule.getCurrentOrderIndex() >= 0);
        assertTrue(schedule.getCurrentOrderIndex() < schedule.getNumberOfOrders());

        schedule = new ScheduleState().addUrgentOrder(order(10)).removeAllStopsAtStation(10);
        assertEquals(0, schedule.getNumberOfOrders());
        assertEquals(-1, schedule.getCurrentOrderIndex());
        assertEquals(-1, schedule.getNextStationId());
    }

    /**
     * Tests order limit allows six ordinary stops and one urgent stop behavior.
     */
    public void testOrderLimitAllowsSixOrdinaryStopsAndOneUrgentStop() {
        ScheduleState schedule = new ScheduleState();
        for (int i = 0; i < UnmodifiableSchedule.MAXIMUM_NUMBER_OF_STOPS; i++) {
            assertTrue(schedule.canAddOrder());
            schedule = schedule.addOrder(order(i));
        }
        assertFalse(schedule.canAddOrder());
        assertTrue(schedule.canAddUrgentOrder());

        schedule = schedule.addUrgentOrder(order(90));
        assertEquals(UnmodifiableSchedule.MAXIMUM_NUMBER_OF_STOPS + 1, schedule.getNumberOfOrders());
        assertFalse(schedule.canAddOrder());
        assertFalse(schedule.canAddUrgentOrder());

        schedule = schedule.gotoNextStation();
        assertEquals(UnmodifiableSchedule.MAXIMUM_NUMBER_OF_STOPS, schedule.getNumberOfOrders());
        assertFalse(schedule.canAddOrder());
        assertTrue(schedule.canAddUrgentOrder());
    }

    /**
     * Performs order.
     * @param stationId the station identifier.
     * @return the train order for the specified station.
     */
    private static TrainOrder order(int stationId) {
        return TrainOrder.keepConsist(stationId);
    }

    /**
     * Performs order.
     * @param stationId the station identifier.
     * @return the train order for the specified station.
     */
    private static ScheduleState scheduleWithOrders(int... stationIds) {
        ScheduleState schedule = new ScheduleState();
        for (int stationId : stationIds) {
            schedule = schedule.addOrder(order(stationId));
        }
        return schedule;
    }

    /**
     * Asserts selected.
     * @param schedule the train schedule under test.
     * @param index the index of the entry to check.
     * @param stationId the station identifier.
     */
    private static void assertSelected(ScheduleState schedule, int index, int stationId) {
        assertEquals(index, schedule.getCurrentOrderIndex());
        assertEquals(stationId, schedule.getNextStationId());
    }
}
