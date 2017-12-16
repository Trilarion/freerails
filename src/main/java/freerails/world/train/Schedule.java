/*
 * Created on 24-Aug-2003
 *
 */
package freerails.world.train;

import freerails.world.common.ImInts;

/**
 * Defines methods to access a train's schedule.
 *
 * @author Luke Lindsay
 */
public interface Schedule {
    public static int PRIORITY_ORDERS = 0;

    public static final int MAXIMUM_NUMBER_OF_ORDER = 6;

    TrainOrdersModel getOrder(int i);

    /**
     * Returns the number of the order the train is currently carry out.
     */
    int getOrderToGoto();

    /**
     * Returns the station number of the next station the train is scheduled to
     * stop at.
     */
    int getStationToGoto();

    /**
     * Returns the wagons to add at the next scheduled stop.
     */

    ImInts getWagonsToAdd();

    /**
     * Returns the value for the autoconsist flag at the next scheduled stop.
     */
    boolean autoConsist();

    boolean hasPriorityOrders();

    /**
     * Returns number of non priority orders + number of priority orders.
     *
     * @return Number of orders.
     */
    int getNumOrders();

    int getNextScheduledOrder();
}