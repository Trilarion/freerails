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
 * Schedule.java
 *
 */
package freerails.model.train.schedule;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mutable train schedule containing station orders and the next scheduled stop.
 */
public class Schedule implements UnmodifiableSchedule {
    /**
     * Vector of TrainOrder.
     */
    public static final UnmodifiableSchedule EMPTY = new Schedule();
    private List<TrainOrder> orders = new ArrayList<>();
    private int nextScheduledOrder = -1;

    /**
     * Whether the train should ignore the stationToGoto and goto the first
     * station in the list.
     */
    private boolean hasPriorityOrders = false;

    /** Creates an empty schedule with no scheduled destination. */
    public Schedule() {
    }

    /**
     * Creates a mutable copy of a read-only schedule.
     *
     * @param schedule schedule whose order state is copied
     */
    public Schedule(UnmodifiableSchedule schedule) {
        nextScheduledOrder = schedule.getNextScheduledOrder();
        hasPriorityOrders = schedule.hasPriorityOrders();

        for (int i = 0; i < schedule.getNumberOfOrders(); i++) {
            orders.add(schedule.getOrder(i));
        }
    }

    /**
     * @param orders schedule orders in index order
     * @param gotoStation index of the next scheduled order, or {@code -1} if none
     * @param hasPriorityOrders whether order zero is a priority order
     */
    public Schedule(TrainOrder[] orders, int gotoStation, boolean hasPriorityOrders) {
        this.orders = Arrays.asList(orders);
        nextScheduledOrder = gotoStation;
        this.hasPriorityOrders = hasPriorityOrders;
    }

    /**
     * @param order new priority order
     */
    public void setPriorityOrders(TrainOrder order) {
        if (hasPriorityOrders) {
            // Replace existing priority orders.
            orders.set(PRIORITY_ORDERS, order);
        } else {
            // Insert priority orders at position 0;
            hasPriorityOrders = true;
            orders.add(PRIORITY_ORDERS, order);
            if (nextScheduledOrder >= 0) {
                nextScheduledOrder++;
            }
        }
    }

    /**
     * Removes the order at the specified position.
     *
     * @param orderNumber index of the order to remove
     */
    public void removeOrder(int orderNumber) {
        if (PRIORITY_ORDERS == orderNumber && hasPriorityOrders) {
            // If we are removing the priority stop.
            hasPriorityOrders = false;
        }
        if (orderNumber >= orders.size()) {
            // cannot remove an order that's already removed!
            return;
        }
        orders.remove(orderNumber);

        // shift current station down
        if (nextScheduledOrder > orderNumber) {
            nextScheduledOrder--;
        }

        if (orders.size() <= nextScheduledOrder) {
            nextScheduledOrder = firstScheduleStop();
        }

        if (0 == numberOfScheduledStops()) {
            nextScheduledOrder = -1;
        }
    }

    /** Returns the first index reserved for ordinary scheduled stops. */
    private int firstScheduleStop() {
        return hasPriorityOrders ? 1 : 0;
    }

    /** Returns the number of ordinary scheduled stops, excluding a priority order. */
    private int numberOfScheduledStops() {
        return orders.size() - firstScheduleStop();
    }

    /**
     * Inserts an ordinary order at the specified position. Note you must call
     * {@link #setPriorityOrders(TrainOrder)} to set a priority order.
     *
     * @param orderNumber index at which to insert the order
     * @param order order to insert
     * @throws IllegalArgumentException if the index is outside the ordinary-order range
     * @throws IllegalStateException if the schedule already contains the maximum number of orders
     */
    public void addOrder(int orderNumber, TrainOrder order) {
        if (orderNumber < firstScheduleStop() || orderNumber > orders.size()) {
            throw new IllegalArgumentException(String.valueOf(orderNumber));
        }
        if (!canAddOrder()) {
            throw new IllegalStateException();
        }

        orders.add(orderNumber, order);

        if (nextScheduledOrder >= orderNumber) {
            nextScheduledOrder++;
        }

        if (-1 == nextScheduledOrder && 0 < numberOfScheduledStops()) {
            nextScheduledOrder = firstScheduleStop();
        }
    }

    /**
     * @param order order to append
     * @return index at which the order was added
     * @throws IllegalStateException if the schedule already contains the maximum number of orders
     */
    public int addOrder(TrainOrder order) {
        if (!canAddOrder()) {
            throw new IllegalStateException();
        }

        int newOrderNumber = orders.size();
        addOrder(newOrderNumber, order);

        return newOrderNumber;
    }

    /**
     * @param orderNumber index to replace, or the end index to append at
     * @param order order to set
     */
    public void setOrder(int orderNumber, TrainOrder order) {
        if (orderNumber >= orders.size()) {
            orders.add(order);
        } else {
            orders.set(orderNumber, order);
        }
    }

    /**
     * @param index zero-based order index
     * @return the order at {@code index}
     */
    @Override
    public TrainOrder getOrder(int index) {
        return orders.get(index);
    }

    /**
     * Returns the index of the order currently being carried out.
     */
    @Override
    public int getCurrentOrderIndex() {
        return hasPriorityOrders ? 0 : nextScheduledOrder;
    }

    /**
     * @param i index of the order to visit next
     * @throws IllegalArgumentException if the index is outside the schedule
     */
    public void setOrderToGoto(int i) {
        if (!canSetGotoStation(i)) {
            throw new IllegalArgumentException(String.valueOf(i));
        }

        nextScheduledOrder = i;
    }

    /**
     * Returns the station identifier for the current order, or {@code -1} if none.
     */
    @Override
    public int getNextStationId() {
        int orderToGoto = getCurrentOrderIndex();

        if (-1 == orderToGoto) {
            return -1;
        }
        TrainOrder order = orders.get(orderToGoto);
        return order.getStationID();
    }

    /**
     * Returns the consist setting for the current order.
     *
     * @throws IllegalStateException if the schedule has no current order
     */
    @Override
    public List<Integer> getWagonsToAdd() {
        return getCurrentOrder().getConsist();
    }

    /**
     * Advances to the next ordinary stop, wrapping at the end. A priority order
     * is removed when it has been visited, without advancing the ordinary stop.
     */
    public void gotoNextStation() {
        if (hasPriorityOrders) {
            removeOrder(PRIORITY_ORDERS);
            return;
        }

        if (numberOfScheduledStops() == 0) {
            return;
        }

        nextScheduledOrder++;

        if (orders.size() <= nextScheduledOrder) {
            nextScheduledOrder = firstScheduleStop();
        }
    }

    /** Returns whether the schedule currently has a priority order. */
    @Override
    public boolean hasPriorityOrders() {
        return hasPriorityOrders;
    }

    /**
     * Returns number of non priority orders + number of priority orders.
     *
     * @return Number of orders.
     */
    @Override
    public int getNumberOfOrders() {
        return orders.size();
    }

    /**
     * Returns whether the specified order can be moved one position toward the top.
     *
     * @param orderNumber order index
     * @return {@code true} if it can be pulled up without crossing the priority order
     */
    public boolean canPullUp(int orderNumber) {
        boolean isAlreadyAtTop = 0 == orderNumber;
        boolean isPriorityOrdersAbove = orderNumber == 1 && hasPriorityOrders;

        return !isAlreadyAtTop && !isPriorityOrdersAbove;
    }

    /**
     * Returns whether the specified order can be moved one position toward the bottom.
     *
     * @param orderNumber order index
     * @return {@code true} if it can be pushed down
     */
    public boolean canPushDown(int orderNumber) {
        boolean isOrderPriorityOrders = orderNumber == 0 && hasPriorityOrders;
        boolean isAlreadyAtBottom = orderNumber == orders.size() - 1;

        return !isOrderPriorityOrders && !isAlreadyAtBottom;
    }

    /**
     * Moves an order one position toward the top of the schedule.
     *
     * @param orderNumber index of the order to move
     * @throws IllegalArgumentException if the order cannot be moved up
     */
    public void pullUp(int orderNumber) {
        if (!canPullUp(orderNumber)) {
            throw new IllegalArgumentException(String.valueOf(orderNumber));
        }

        boolean isGoingToThisStation = nextScheduledOrder == orderNumber;
        TrainOrder order = getOrder(orderNumber);
        removeOrder(orderNumber);
        addOrder(orderNumber - 1, order);

        if (isGoingToThisStation) {
            setOrderToGoto(orderNumber - 1);
        }
    }

    /**
     * Moves an order one position toward the bottom of the schedule.
     *
     * @param orderNumber index of the order to move
     * @throws IllegalArgumentException if the order cannot be moved down
     */
    public void pushDown(int orderNumber) {
        if (!canPushDown(orderNumber)) {
            throw new IllegalArgumentException(String.valueOf(orderNumber));
        }

        boolean isGoingToThisStation = nextScheduledOrder == orderNumber;
        TrainOrder order = getOrder(orderNumber);
        removeOrder(orderNumber);
        addOrder(orderNumber + 1, order);

        if (isGoingToThisStation) {
            setOrderToGoto(orderNumber + 1);
        }
    }

    /** Returns whether another order can be added without exceeding the schedule limit. */
    public boolean canAddOrder() {
        int max = hasPriorityOrders ? MAXIMUM_NUMBER_OF_ORDER + 1 : MAXIMUM_NUMBER_OF_ORDER;

        return max > getNumberOfOrders();
    }

    /**
     * Returns whether the specified order can be selected as the next stop.
     *
     * @param orderNumber index of the candidate order
     * @return {@code false} if the index is outside the ordinary orders
     */
    public boolean canSetGotoStation(int orderNumber) {
        return orderNumber >= firstScheduleStop()
                && orderNumber < orders.size();
    }

    /** Returns the index of the next ordinary scheduled order. */
    @Override
    public int getNextScheduledOrder() {
        return nextScheduledOrder;
    }

    /**
     * Returns whether any order visits the specified station.
     *
     * @param stationId station identifier
     * @return {@code true} if an order targets the station
     */
    @Override
    public boolean stopsAtStation(int stationId) {
        for (int i = 0; i < getNumberOfOrders(); i++) {
            TrainOrder order = getOrder(i);

            if (order.getStationID() == stationId) {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes every order targeting the specified station.
     *
     * @param stationNumber station identifier
     */
    public void removeAllStopsAtStation(int stationNumber) {
        int i = 0;

        while (i < getNumberOfOrders()) {
            TrainOrder order = getOrder(i);

            if (order.getStationID() == stationNumber) {
                removeOrder(i);
            } else {
                i++;
            }
        }
    }

    /**
     * Returns whether the current order requests automatic consist selection.
     *
     * @throws IllegalStateException if the schedule has no current order
     */
    @Override
    public boolean autoConsist() {
        return getCurrentOrder().isAutoConsist();
    }

    private TrainOrder getCurrentOrder() {
        int currentOrderIndex = getCurrentOrderIndex();
        if (currentOrderIndex == -1) {
            throw new IllegalStateException("Schedule has no current order");
        }
        return orders.get(currentOrderIndex);
    }

    /** Compares the orders, current destination index, and priority-order state. */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Schedule test) {

            return hasPriorityOrders == test.hasPriorityOrders && nextScheduledOrder == test.nextScheduledOrder && orders.equals(test.orders);
        }
        return false;
    }

    /** Returns a hash code based on the next destination and priority-order state. */
    @Override
    public int hashCode() {
        int result;
        result = nextScheduledOrder;
        result = 29 * result + (hasPriorityOrders ? 1 : 0);

        return result;
    }
}