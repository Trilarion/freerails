/*
 * Schedule.java
 *
 * Created on 22 January 2002, 20:14
 */
package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;
import java.util.Vector;


/** This class represents a train's schedule.  That is, which stations that the
 * train should visit and what wagons the engine should pull.
 *
 * @author  lindsal
 */
public class MutableSchedule implements FreerailsSerializable, Schedule {
    /**
     * Vector of TrainOrdersModel
     */
    private final Vector orders = new Vector();
    private int nextScheduledOrder = 0;

    /** Whether the train should ignore the stationToGoto
     *and goto the first station in the list.
     */
    private boolean hasPriorityOrders = false;

    public MutableSchedule() {
    }

    public MutableSchedule(ImmutableSchedule s) {
        nextScheduledOrder = s.getNextScheduledOrder();
        hasPriorityOrders = s.hasPriorityOrders();

        for (int i = 0; i < s.getNumOrders(); i++) {
            orders.add(s.getOrder(i));
        }
    }

    public ImmutableSchedule toImmutableSchedule() {
        TrainOrdersModel[] ordersArray = new TrainOrdersModel[orders.size()];

        for (int i = 0; i < ordersArray.length; i++) {
            ordersArray[i] = (TrainOrdersModel)orders.get(i);
        }

        return new ImmutableSchedule(ordersArray, this.nextScheduledOrder,
            this.hasPriorityOrders);
    }

    public void setPriorityOrders(TrainOrdersModel order) {
        if (hasPriorityOrders) {
            //Replace existing priority orders.
            orders.set(PRIORITY_ORDERS, order);
        } else {
            //Insert priority orders at position 0;
            hasPriorityOrders = true;
            orders.add(PRIORITY_ORDERS, order);
            nextScheduledOrder++;
        }
    }

    /**
     * Removes the order at the specified position
     */
    public void removeOrder(int orderNumber) {
        if (PRIORITY_ORDERS == orderNumber && hasPriorityOrders) {
            //If we are removing the prority stop.
            hasPriorityOrders = false;
        }

        orders.remove(orderNumber);

        /* shift current station down */
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
    public void addOrder(int orderNumber, TrainOrdersModel order) {
        orders.add(orderNumber, order);

        if (nextScheduledOrder >= orderNumber) {
            nextScheduledOrder++;
        }

        if (-1 == nextScheduledOrder && 0 < numberOfScheduledStops()) {
            nextScheduledOrder = firstScheduleStop();
        }
    }

    public int addOrder(TrainOrdersModel order) {
        if (!canAddOrder()) {
            throw new IllegalStateException();
        }

        int newOrderNumber = orders.size();
        addOrder(newOrderNumber, order);

        return newOrderNumber;
    }

    public void setOrder(int orderNumber, TrainOrdersModel order) {
        if (orderNumber >= orders.size()) {
            orders.add(order);
        } else {
            orders.set(orderNumber, order);
        }
    }

    public TrainOrdersModel getOrder(int i) {
        return (TrainOrdersModel)orders.get(i);
    }

    /** Returns the number of the order the train is currently carry out. */
    public int getOrderToGoto() {
        return nextScheduledOrder;
    }

    public void setOrderToGoto(int i) {
        if (i < 0 || i >= orders.size()) {
            throw new IllegalArgumentException(String.valueOf(i));
        }

        nextScheduledOrder = i;
    }

    /** Returns the station number of the next station the train is scheduled to
     * stop at. */
    public int getStationToGoto() {
        return ((TrainOrdersModel)orders.get(nextScheduledOrder)).getStationNumber();
    }

    /** Returns the wagons to add at the next scheduled stop. */
    public int[] getWagonsToAdd() {
        return ((TrainOrdersModel)orders.get(nextScheduledOrder)).getConsist();
    }

    /** If there are no priority orders, sets the station to goto to the next station
     *in the list of orders or if there are no more stations, the first station in the list.
     * If priority orders are set, the priority orders orders are removed from the schedule
     * and the goto station is not changed.
     */
    public void gotoNextStaton() {
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

    public boolean hasPriorityOrders() {
        return this.hasPriorityOrders;
    }

    /**
     * Returns number of non priority orders + number of priority orders.
     * @return Number of orders.
     */
    public int getNumOrders() {
        return orders.size();
    }

    public boolean canPullUp(int orderNumber) {
        boolean isAlreadyAtTop = 0 == orderNumber;
        boolean isPriorityOrdersAbove = (orderNumber == 1 &&
            this.hasPriorityOrders);

        return !isAlreadyAtTop && !isPriorityOrdersAbove;
    }

    public boolean canPushDown(int orderNumber) {
        boolean isOrderPriorityOrders = (orderNumber == 0 &&
            this.hasPriorityOrders);
        boolean isAlreadyAtBottom = orderNumber == this.orders.size() - 1;

        return !isOrderPriorityOrders && !isAlreadyAtBottom;
    }

    public void pullUp(int orderNumber) {
        if (!canPullUp(orderNumber)) {
            throw new IllegalArgumentException(String.valueOf(orderNumber));
        }

        boolean isGoingToThisStation = getOrderToGoto() == orderNumber;
        TrainOrdersModel order = getOrder(orderNumber);
        removeOrder(orderNumber);
        addOrder(orderNumber - 1, order);

        if (isGoingToThisStation) {
            setOrderToGoto(orderNumber - 1);
        }
    }

    public void pushDown(int orderNumber) {
        if (!canPushDown(orderNumber)) {
            throw new IllegalArgumentException(String.valueOf(orderNumber));
        }

        boolean isGoingToThisStation = getOrderToGoto() == orderNumber;
        TrainOrdersModel order = getOrder(orderNumber);
        removeOrder(orderNumber);
        addOrder(orderNumber + 1, order);

        if (isGoingToThisStation) {
            setOrderToGoto(orderNumber + 1);
        }
    }

    public boolean canAddOrder() {
        int max = hasPriorityOrders ? MAXIMUM_NUMBER_OF_ORDER + 1
                                    : MAXIMUM_NUMBER_OF_ORDER;

        return max > getNumOrders();
    }

    public boolean canSetGotoStation(int orderNumber) {
        return !(orderNumber == 0 && hasPriorityOrders);
    }

    public int getNextScheduledOrder() {
        return this.nextScheduledOrder;
    }

    public void removeAllStopsAtStation(int stationNumber) {
        int i = 0;

        while (i < this.getNumOrders()) {
            TrainOrdersModel order = this.getOrder(i);

            if (order.getStationNumber() == stationNumber) {
                this.removeOrder(i);
            } else {
                i++;
            }
        }
    }
}