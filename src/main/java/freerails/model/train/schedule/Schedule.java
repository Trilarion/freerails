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
 * Schedule.java
 *
 */
package freerails.model.train.schedule;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a train's schedule. That is, which stations that the
 * train should visit and what wagons the engine should pull.
 */
public class Schedule implements UnmodifiableSchedule {
    /**
     * Vector of TrainOrder.
     */
    public static final UnmodifiableSchedule EMPTY = new Schedule();
    private List<TrainOrder> orders = new ArrayList();
    private int nextScheduledOrder = -1;

    /**
     * Whether the train should ignore the stationToGoto and goto the first
     * station in the list.
     */
    private boolean hasPriorityOrders = false;

    /**
     *
     */
    public Schedule() {
    }

    /**
     * @param schedule
     */
    public Schedule(UnmodifiableSchedule schedule) {
        nextScheduledOrder = schedule.getNextScheduledOrder();
        hasPriorityOrders = schedule.hasPriorityOrders();

        for (int i = 0; i < schedule.getNumberOfOrders(); i++) {
            orders.add(schedule.getOrder(i));
        }
    }

    /**
     * @param orders
     * @param gotoStation
     * @param hasPriorityOrders
     */
    public Schedule(TrainOrder[] orders, int gotoStation, boolean hasPriorityOrders) {
        this.orders = Arrays.asList(orders);
        nextScheduledOrder = gotoStation;
        this.hasPriorityOrders = hasPriorityOrders;
    }

    /**
     * @param order
     */
    public void setPriorityOrders(TrainOrder order) {
        if (hasPriorityOrders) {
            // Replace existing priority orders.
            orders.set(PRIORITY_ORDERS, order);
        } else {
            // Insert priority orders at position 0;
            hasPriorityOrders = true;
            orders.add(PRIORITY_ORDERS, order);
            nextScheduledOrder++;
        }
    }

    /**
     * Removes the order at the specified position.
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

    private int firstScheduleStop() {
        return hasPriorityOrders ? 1 : 0;
    }

    private int numberOfScheduledStops() {
        return orders.size() - firstScheduleStop();
    }

    /**
     * Inserts an order at the specified position. Note you must call
     * setPriorityOrders() to set the priority orders.
     */
    private void addOrder(int orderNumber, TrainOrder order) {
        orders.add(orderNumber, order);

        if (nextScheduledOrder >= orderNumber) {
            nextScheduledOrder++;
        }

        if (-1 == nextScheduledOrder && 0 < numberOfScheduledStops()) {
            nextScheduledOrder = firstScheduleStop();
        }
    }

    /**
     * @param order
     * @return
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
     * @param orderNumber
     * @param order
     */
    public void setOrder(int orderNumber, TrainOrder order) {
        if (orderNumber >= orders.size()) {
            orders.add(order);
        } else {
            orders.set(orderNumber, order);
        }
    }

    /**
     * @param index
     * @return
     */
    @Override
    public TrainOrder getOrder(int index) {
        return orders.get(index);
    }

    /**
     * Returns the number of the order the train is currently carry out.
     */
    @Override
    public int getCurrentOrderIndex() {
        return hasPriorityOrders ? 0 : nextScheduledOrder;
    }

    /**
     * @param i
     */
    public void setOrderToGoto(int i) {
        if (i < 0 || i >= orders.size()) {
            throw new IllegalArgumentException(String.valueOf(i));
        }

        nextScheduledOrder = i;
    }

    /**
     * Returns the station number of the next station the train is scheduled to
     * stop at.
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
     * Returns the wagons to add at the next scheduled stop.
     */
    @Override
    public List<Integer> getWagonsToAdd() {
        TrainOrder order = orders.get(getCurrentOrderIndex());
        return order.getConsist();
    }

    /**
     * If there are no priority orders, sets the station to goto to the next
     * station in the list of orders or if there are no more stations, the first
     * station in the list. If priority orders are set, the priority orders
     * orders are removed from the schedule and the goto station is not changed.
     */
    public void gotoNextStation() {
        if (hasPriorityOrders) {
            if (nextScheduledOrder != PRIORITY_ORDERS) {
                removeOrder(PRIORITY_ORDERS);

                return;
            }
        }

        nextScheduledOrder++;

        if (orders.size() <= nextScheduledOrder) {
            nextScheduledOrder = 0;
        }
    }

    /**
     * @return
     */
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
     * @param orderNumber
     * @return
     */
    public boolean canPullUp(int orderNumber) {
        boolean isAlreadyAtTop = 0 == orderNumber;
        boolean isPriorityOrdersAbove = (orderNumber == 1 && hasPriorityOrders);

        return !isAlreadyAtTop && !isPriorityOrdersAbove;
    }

    /**
     * @param orderNumber
     * @return
     */
    public boolean canPushDown(int orderNumber) {
        boolean isOrderPriorityOrders = (orderNumber == 0 && hasPriorityOrders);
        boolean isAlreadyAtBottom = orderNumber == orders.size() - 1;

        return !isOrderPriorityOrders && !isAlreadyAtBottom;
    }

    /**
     * @param orderNumber
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
     * @param orderNumber
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

    /**
     * @return
     */
    public boolean canAddOrder() {
        int max = hasPriorityOrders ? MAXIMUM_NUMBER_OF_ORDER + 1 : MAXIMUM_NUMBER_OF_ORDER;

        return max > getNumberOfOrders();
    }

    /**
     * @param orderNumber
     * @return
     */
    public boolean canSetGotoStation(int orderNumber) {
        return !(orderNumber == 0 && hasPriorityOrders);
    }

    /**
     * @return
     */
    @Override
    public int getNextScheduledOrder() {
        return nextScheduledOrder;
    }

    /**
     * @param stationId
     * @return
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
     * @param stationNumber
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

    @Override
    public boolean autoConsist() {
        TrainOrder order = orders.get(getCurrentOrderIndex());
        return order.isAutoConsist();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Schedule) {
            Schedule test = (Schedule) obj;

            return hasPriorityOrders == test.hasPriorityOrders && nextScheduledOrder == test.nextScheduledOrder && orders.equals(test.orders);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result;
        result = nextScheduledOrder;
        result = 29 * result + (hasPriorityOrders ? 1 : 0);

        return result;
    }
}