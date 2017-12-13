/*
 * Copyright (C) 2003 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * Created on 24-Aug-2003
 *
  */
package jfreerails.world.train;

import java.util.Arrays;

import jfreerails.world.top.ObjectKey;
import jfreerails.world.common.FreerailsSerializable;

/**
 * @author Luke Lindsay
 *
 */
public class ImmutableSchedule implements Schedule, FreerailsSerializable {
    private final TrainOrdersModel[] orders;
    private final int nextScheduledOrder;
    private final boolean hasPriorityOrders;

    /**
     * @param gotoStation index in the orders array of the next station to go
     * to.
     * @param orders Array of TrainOrdersModel representing stations to go to.
     */
    public ImmutableSchedule(TrainOrdersModel[] orders, int gotoStation,
        boolean hasPriorityOrders) {
        this.orders = (TrainOrdersModel[])orders.clone();
        this.nextScheduledOrder = gotoStation;
        this.hasPriorityOrders = hasPriorityOrders;
    }

    public TrainOrdersModel getOrder(int i) {
        return orders[i];
    }

    public int getOrderToGoto() {
        return hasPriorityOrders ? 0 : nextScheduledOrder;
    }

    public ObjectKey getStationToGoto() {
        int orderToGoto = getOrderToGoto();

        if (-1 == orderToGoto) {
            return null;
        } else {
            return orders[orderToGoto].getStationNumber();
        }
    }

    public int[] getWagonsToAdd() {
        return orders[getOrderToGoto()].consist;
    }

    public boolean hasPriorityOrders() {
        return hasPriorityOrders;
    }

    public int getNumOrders() {
        return orders.length;
    }

    public int getNextScheduledOrder() {
        return this.nextScheduledOrder;
    }

    public boolean stopsAtStation(ObjectKey station) {
        for (int i = 0; i < this.getNumOrders(); i++) {
            TrainOrdersModel order = this.getOrder(i);

            if (order.getStationNumber().equals(station)) {
                return true;
            }
        }

        return false;
    }

    public boolean equals(Object o) {
        if (o instanceof ImmutableSchedule) {
            ImmutableSchedule test = (ImmutableSchedule)o;

            return this.hasPriorityOrders == test.hasPriorityOrders &&
            this.nextScheduledOrder == test.nextScheduledOrder &&
            Arrays.equals(this.orders, test.orders);
        } else {
            return false;
        }
    }
}
