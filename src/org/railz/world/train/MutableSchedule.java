/*
 * Copyright (C) 2002 Luke Lindsay
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
 * Schedule.java
 *
 * Created on 22 January 2002, 20:14
 */
package org.railz.world.train;

import java.util.*;

import org.railz.world.common.FreerailsSerializable;
import org.railz.world.top.ObjectKey;

/**
 * This class represents a train's schedule.  That is, which stations that the
 * train should visit and what wagons the engine should pull.
 *
 * @author  lindsal
 */
public class MutableSchedule implements Schedule {
    private ArrayList orders;

    public MutableSchedule() {
	orders = new ArrayList();
    }

    public MutableSchedule(ImmutableSchedule s) {
	orders = new ArrayList();
	for (int i = 0; i < s.getNumOrders(); i++) {
	    orders.add(s.getOrder(i));
	}
    }

    public ImmutableSchedule toImmutableSchedule() {
	return new ImmutableSchedule((TrainOrdersModel[])
		orders.toArray(new TrainOrdersModel[orders.size()]));
    }

    /**
     * Removes the order with the specified id 
     */
    public void removeOrder(int orderNumber) {
	orders.remove(orderNumber);
    }

    /**
     * Inserts an order at the specified position.
     */
    public void addOrder(int index, TrainOrdersModel order) {
	orders.add(index, order);
    }

    /**
     * Inserts an order at the end of the list.
     */
    public void addOrder(TrainOrdersModel order) {
	orders.add(order);
    }

    public void setOrder(int orderNumber, TrainOrdersModel order) {
	orders.set(orderNumber, order);
    }

    public TrainOrdersModel getOrder(int i) {
        return ((TrainOrdersModel) orders.get(i));
    }

    public void removeAllStopsAtStation(ObjectKey stationNumber) {
	while (!orders.isEmpty())
	    orders.remove(0);
    }

    public int getNumOrders() {
	return orders.size();
    }
}
