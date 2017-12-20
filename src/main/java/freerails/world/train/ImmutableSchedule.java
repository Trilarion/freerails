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
 *
 */
package freerails.world.train;

import freerails.util.ImInts;
import freerails.util.ImList;

import java.io.Serializable;

/**
 * A Schedule that is immutable.
 */
public class ImmutableSchedule implements Schedule, Serializable {
    private static final long serialVersionUID = 3977858458324318264L;

    private final ImList<TrainOrdersModel> orders;

    private final int nextScheduledOrder;
    private final boolean hasPriorityOrders;

    /**
     * @param orders
     * @param gotoStation
     * @param hasPriorityOrders
     */
    public ImmutableSchedule(TrainOrdersModel[] orders, int gotoStation,
                             boolean hasPriorityOrders) {
        this.orders = new ImList<>(orders);
        this.nextScheduledOrder = gotoStation;
        this.hasPriorityOrders = hasPriorityOrders;
    }

    @Override
    public int hashCode() {
        int result;
        result = nextScheduledOrder;
        result = 29 * result + (hasPriorityOrders ? 1 : 0);

        return result;
    }

    /**
     * @param i
     * @return
     */
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

    /**
     * @return
     */
    public boolean hasPriorityOrders() {
        return hasPriorityOrders;
    }

    public int getNumOrders() {
        return orders.size();
    }

    /**
     * @return
     */
    public int getNextScheduledOrder() {
        return this.nextScheduledOrder;
    }

    /**
     * @param stationNumber
     * @return
     */
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