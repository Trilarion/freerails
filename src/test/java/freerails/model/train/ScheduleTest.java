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
package freerails.model.train;

import freerails.model.train.schedule.Schedule;
import freerails.model.train.schedule.TrainOrder;
import freerails.model.train.schedule.UnmodifiableSchedule;
import junit.framework.TestCase;

/**
 *
 */
public class ScheduleTest extends TestCase {

    /**
     *
     */
    public void test1() {
        Schedule schedule = new Schedule();
        assertEquals(0, schedule.getNumberOfOrders());
        assertEquals(-1, schedule.getCurrentOrderIndex());
        assertEquals(-1, schedule.getNextScheduledOrder());
        assertEquals(-1, schedule.getNextStationId());
        assertCurrentOrderUnavailable(schedule);

        schedule.addOrder(order(10));
        assertEquals(0, schedule.getCurrentOrderIndex());
        assertEquals(0, schedule.getNextScheduledOrder());
        assertEquals(10, schedule.getNextStationId());

        schedule.addOrder(order(20));
        schedule.addOrder(order(30));
        assertEquals(0, schedule.getCurrentOrderIndex());
        assertEquals(10, schedule.getNextStationId());

        schedule.gotoNextStation();
        assertEquals(1, schedule.getCurrentOrderIndex());
        assertEquals(20, schedule.getNextStationId());
    }

    public void testAdvancingRegularOrdersWrapsOnce() {
        Schedule schedule = new Schedule();
        schedule.addOrder(order(10));
        schedule.addOrder(order(20));
        schedule.addOrder(order(30));

        assertEquals(10, schedule.getNextStationId());
        schedule.gotoNextStation();
        assertEquals(20, schedule.getNextStationId());
        schedule.gotoNextStation();
        assertEquals(30, schedule.getNextStationId());
        schedule.gotoNextStation();
        assertEquals(10, schedule.getNextStationId());
        assertEquals(0, schedule.getCurrentOrderIndex());
    }

    public void testAddingAndRemovingOrdersPreservesSelectedStop() {
        Schedule schedule = scheduleWithOrders(10, 20, 30);
        schedule.setOrderToGoto(1);

        schedule.addOrder(1, order(15));
        assertSelected(schedule, 2, 20);
        schedule.addOrder(2, order(25));
        assertSelected(schedule, 3, 20);
        schedule.addOrder(order(50));
        assertSelected(schedule, 3, 20);

        schedule.removeOrder(1);
        assertSelected(schedule, 2, 20);
        schedule.removeOrder(4);
        assertSelected(schedule, 2, 20);
        schedule.removeOrder(2);
        assertSelected(schedule, 2, 30);
    }

    public void testMovingCurrentOrderPreservesSelectedStop() {
        Schedule schedule = scheduleWithOrders(10, 20, 30, 40);
        schedule.setOrderToGoto(2);

        schedule.pullUp(2);
        assertSelected(schedule, 1, 30);
        schedule.pushDown(1);
        assertSelected(schedule, 2, 30);
    }

    public void testPriorityOrderLifecycle() {
        Schedule schedule = new Schedule();
        schedule.setPriorityOrders(order(90));
        assertTrue(schedule.hasPriorityOrders());
        assertEquals(0, schedule.getCurrentOrderIndex());
        assertEquals(-1, schedule.getNextScheduledOrder());
        assertEquals(90, schedule.getNextStationId());

        schedule.gotoNextStation();
        assertFalse(schedule.hasPriorityOrders());
        assertEquals(-1, schedule.getCurrentOrderIndex());
        assertEquals(-1, schedule.getNextScheduledOrder());
        assertEquals(-1, schedule.getNextStationId());
        schedule.setPriorityOrders(order(90));

        schedule.addOrder(order(10));
        schedule.addOrder(order(20));
        assertEquals(1, schedule.getNextScheduledOrder());
        schedule.setOrderToGoto(2);
        assertEquals(90, schedule.getNextStationId());

        schedule.setPriorityOrders(order(91));
        assertEquals(91, schedule.getNextStationId());
        assertEquals(2, schedule.getNextScheduledOrder());
        schedule.gotoNextStation();
        assertFalse(schedule.hasPriorityOrders());
        assertSelected(schedule, 1, 20);
        schedule.gotoNextStation();
        assertSelected(schedule, 0, 10);
        schedule.gotoNextStation();
        assertSelected(schedule, 1, 20);

        schedule.setPriorityOrders(order(92));
        schedule.removeOrder(0);
        assertFalse(schedule.hasPriorityOrders());
        assertSelected(schedule, 1, 20);
    }

    public void testPriorityOrderCannotBeReorderedOrSelectedAsRegular() {
        Schedule schedule = new Schedule();
        schedule.setPriorityOrders(order(90));
        schedule.addOrder(order(10));
        schedule.addOrder(order(20));
        schedule.addOrder(order(30));

        assertFalse(schedule.canPullUp(0));
        assertFalse(schedule.canPushDown(0));
        assertFalse(schedule.canSetGotoStation(0));
        assertThrowsIllegalArgument(() -> schedule.pullUp(0));
        assertThrowsIllegalArgument(() -> schedule.pushDown(0));
        assertThrowsIllegalArgument(() -> schedule.setOrderToGoto(0));

        assertFalse(schedule.canPullUp(1));
        assertTrue(schedule.canPushDown(1));
        assertTrue(schedule.canSetGotoStation(1));
        assertThrowsIllegalArgument(() -> schedule.pullUp(1));
        schedule.pushDown(1);
        schedule.setOrderToGoto(1);
        assertEquals(1, schedule.getNextScheduledOrder());
        assertEquals(90, schedule.getNextStationId());
    }

    public void testRemovingAllStopsAtStationKeepsCurrentOrderValid() {
        Schedule schedule = new Schedule();
        schedule.addOrder(order(20));
        schedule.addOrder(order(10));
        schedule.addOrder(order(30));
        schedule.addOrder(order(10));
        schedule.setOrderToGoto(2);
        schedule.setPriorityOrders(order(10));

        schedule.removeAllStopsAtStation(10);

        assertEquals(2, schedule.getNumberOfOrders());
        assertFalse(schedule.hasPriorityOrders());
        assertFalse(schedule.stopsAtStation(10));
        assertTrue(schedule.getCurrentOrderIndex() >= 0);
        assertTrue(schedule.getCurrentOrderIndex() < schedule.getNumberOfOrders());
        assertEquals(schedule.getCurrentOrderIndex(), schedule.getNextScheduledOrder());
        assertFalse(schedule.getNextStationId() == 10);

        schedule = new Schedule();
        schedule.setPriorityOrders(order(10));
        schedule.addOrder(order(10));
        schedule.setOrderToGoto(1);
        schedule.removeAllStopsAtStation(10);

        assertEquals(0, schedule.getNumberOfOrders());
        assertFalse(schedule.hasPriorityOrders());
        assertEquals(-1, schedule.getCurrentOrderIndex());
        assertEquals(-1, schedule.getNextScheduledOrder());
        assertEquals(-1, schedule.getNextStationId());
    }

    public void testOrderLimitIncludesPriorityOrder() {
        Schedule schedule = new Schedule();
        for (int i = 0; i < UnmodifiableSchedule.MAXIMUM_NUMBER_OF_ORDER; i++) {
            assertTrue(schedule.canAddOrder());
            schedule.addOrder(order(i));
        }
        assertFalse(schedule.canAddOrder());
        assertAddRejectedWithoutMutation(schedule);

        schedule.setPriorityOrders(order(90));
        assertEquals(UnmodifiableSchedule.MAXIMUM_NUMBER_OF_ORDER + 1, schedule.getNumberOfOrders());
        assertFalse(schedule.canAddOrder());
        assertAddRejectedWithoutMutation(schedule);

        schedule.removeOrder(0);
        assertEquals(UnmodifiableSchedule.MAXIMUM_NUMBER_OF_ORDER, schedule.getNumberOfOrders());
        assertFalse(schedule.canAddOrder());
        assertAddRejectedWithoutMutation(schedule);
    }

    private static TrainOrder order(int stationId) {
        return new TrainOrder(stationId, null, false, false);
    }

    private static Schedule scheduleWithOrders(int... stationIds) {
        Schedule schedule = new Schedule();
        for (int stationId : stationIds) {
            schedule.addOrder(order(stationId));
        }
        return schedule;
    }

    private static void assertSelected(Schedule schedule, int index, int stationId) {
        assertEquals(index, schedule.getCurrentOrderIndex());
        assertEquals(index, schedule.getNextScheduledOrder());
        assertEquals(stationId, schedule.getNextStationId());
    }

    private static void assertCurrentOrderUnavailable(Schedule schedule) {
        assertThrowsIllegalState(schedule::getWagonsToAdd);
        assertThrowsIllegalState(schedule::autoConsist);
    }

    private static void assertThrowsIllegalState(Runnable action) {
        try {
            action.run();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // Expected for an empty schedule.
        }
    }

    private static void assertThrowsIllegalArgument(Runnable action) {
        try {
            action.run();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // Expected when an operation targets the priority order.
        }
    }

    private static void assertAddRejectedWithoutMutation(Schedule schedule) {
        int orderCount = schedule.getNumberOfOrders();
        int currentOrderIndex = schedule.getCurrentOrderIndex();
        int nextScheduledOrder = schedule.getNextScheduledOrder();
        int stationId = schedule.getNextStationId();
        assertThrowsIllegalState(() -> schedule.addOrder(order(100)));
        assertEquals(orderCount, schedule.getNumberOfOrders());
        assertEquals(currentOrderIndex, schedule.getCurrentOrderIndex());
        assertEquals(nextScheduledOrder, schedule.getNextScheduledOrder());
        assertEquals(stationId, schedule.getNextStationId());
    }

}
