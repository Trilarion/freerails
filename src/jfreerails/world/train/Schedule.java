/*
 * Created on 24-Aug-2003
 *
 */
package jfreerails.world.train;


/**
 * Defines methods to access a train's schedule.
 * @author Luke Lindsay
 *
 */
public interface Schedule {
    public static int PRIORITY_ORDERS = 0;
    public static final int MAXIMUM_NUMBER_OF_ORDER = 6;

    TrainOrdersModel getOrder(int i) /*=const*/;

    /** Returns the number of the order the train is currently carry out. */
    int getOrderToGoto() /*=const*/;

    /** Returns the station number of the next station the train is scheduled to
     * stop at. */
    int getStationToGoto() /*=const*/;

    /** Returns the wagons to add at the next scheduled stop. */

    /*=const*/ int[] getWagonsToAdd() /*=const*/;

    boolean hasPriorityOrders() /*=const*/;

    /**
     * Returns number of non priority orders + number of priority orders.
     * @return Number of orders.
     */
    int getNumOrders() /*=const*/;

    int getNextScheduledOrder() /*=const*/;
}