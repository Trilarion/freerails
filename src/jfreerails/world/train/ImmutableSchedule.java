/*
 * Created on 24-Aug-2003
 *
  */
package jfreerails.world.train;

import java.util.Arrays;
import jfreerails.world.common.FreerailsSerializable;


/**
 * A Schedule that is immutable.
 * @author Luke Lindsay
 *
 */
public class ImmutableSchedule implements Schedule, FreerailsSerializable {
    private final TrainOrdersModel[] m_orders;
    private final int nextScheduledOrder;

    public int hashCode() {
        int result;
        result = nextScheduledOrder;
        result = 29 * result + (m_hasPriorityOrders ? 1 : 0);

        return result;
    }

    private final boolean m_hasPriorityOrders;

    public ImmutableSchedule( /*=const*/
        TrainOrdersModel[] orders, int gotoStation, boolean hasPriorityOrders) {
        m_orders = orders;
        nextScheduledOrder = gotoStation;
        m_hasPriorityOrders = hasPriorityOrders;
    }

    public TrainOrdersModel getOrder(int i) {
        return m_orders[i];
    }

    public int getOrderToGoto() {
        return m_hasPriorityOrders ? 0 : nextScheduledOrder;
    }

    public int getStationToGoto() {
        int orderToGoto = getOrderToGoto();

        if (-1 == orderToGoto) {
            return -1;
        } else {
            return m_orders[orderToGoto].getStationNumber();
        }
    }

    public /*=const*/ int[] getWagonsToAdd() {
        return m_orders[getOrderToGoto()].consist;
    }

    public boolean hasPriorityOrders() {
        return m_hasPriorityOrders;
    }

    public int getNumOrders() {
        return m_orders.length;
    }

    public int getNextScheduledOrder() {
        return this.nextScheduledOrder;
    }

    public boolean stopsAtStation(int stationNumber) {
        for (int i = 0; i < this.getNumOrders(); i++) {
            TrainOrdersModel order = this.getOrder(i);

            if (order.getStationNumber() == stationNumber) {
                return true;
            }
        }

        return false;
    }

    public boolean equals(Object o) {
        if (o instanceof ImmutableSchedule) {
            ImmutableSchedule test = (ImmutableSchedule)o;

            return this.m_hasPriorityOrders == test.m_hasPriorityOrders &&
            this.nextScheduledOrder == test.nextScheduledOrder &&
            Arrays.equals(this.m_orders, test.m_orders);
        } else {
            return false;
        }
    }
}