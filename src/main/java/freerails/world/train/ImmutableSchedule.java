/*
 * Created on 24-Aug-2003
 *
 */
package freerails.world.train;

import freerails.world.common.FreerailsSerializable;
import freerails.world.common.ImInts;
import freerails.world.common.ImList;

/**
 * A Schedule that is immutable.
 *
 * @author Luke Lindsay
 */
public class ImmutableSchedule implements Schedule, FreerailsSerializable {
    private static final long serialVersionUID = 3977858458324318264L;

    private final ImList<TrainOrdersModel> orders;

    private final int nextScheduledOrder;

    @Override
    public int hashCode() {
        int result;
        result = nextScheduledOrder;
        result = 29 * result + (hasPriorityOrders ? 1 : 0);

        return result;
    }

    private final boolean hasPriorityOrders;

    public ImmutableSchedule(TrainOrdersModel[] orders, int gotoStation,
                             boolean hasPriorityOrders) {
        this.orders = new ImList<TrainOrdersModel>(orders);
        this.nextScheduledOrder = gotoStation;
        this.hasPriorityOrders = hasPriorityOrders;
    }

    public TrainOrdersModel getOrder(int i) {
        return orders.get(i);
    }

    public int getOrderToGoto() {
        return hasPriorityOrders ? 0 : nextScheduledOrder;
    }

    public int getStationToGoto() {
        int orderToGoto = getOrderToGoto();

        if (-1 == orderToGoto) {
            return -1;
        }
        TrainOrdersModel order = orders.get(orderToGoto);
        return order.getStationID();
    }

    public ImInts getWagonsToAdd() {
        TrainOrdersModel order = orders.get(getOrderToGoto());
        return order.consist;
    }

    public boolean hasPriorityOrders() {
        return hasPriorityOrders;
    }

    public int getNumOrders() {
        return orders.size();
    }

    public int getNextScheduledOrder() {
        return this.nextScheduledOrder;
    }

    public boolean stopsAtStation(int stationNumber) {
        for (int i = 0; i < this.getNumOrders(); i++) {
            TrainOrdersModel order = this.getOrder(i);

            if (order.getStationID() == stationNumber) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ImmutableSchedule) {
            ImmutableSchedule test = (ImmutableSchedule) o;

            return this.hasPriorityOrders == test.hasPriorityOrders
                    && this.nextScheduledOrder == test.nextScheduledOrder
                    && this.orders.equals(test.orders);
        }
        return false;
    }

    public boolean autoConsist() {
        TrainOrdersModel order = orders.get(getOrderToGoto());
        return order.autoConsist;
    }
}