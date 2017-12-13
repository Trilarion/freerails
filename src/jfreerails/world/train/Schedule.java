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

import jfreerails.world.top.ObjectKey;

/**
 * @author Luke Lindsay
 *
 */
public interface Schedule {
    public static final int MAXIMUM_NUMBER_OF_ORDER = 6;

    TrainOrdersModel getOrder(int i);

    /** Returns the number of the order the train is currently carry out. */
    int getOrderToGoto();

    /** Returns the station number of the next station the train is scheduled to
     * stop at. */
    ObjectKey getStationToGoto();

    /** Returns the wagons to add at the next scheduled stop. */
    int[] getWagonsToAdd();

    boolean hasPriorityOrders();

    /**
     * Returns number of non priority orders + number of priority orders.
     * @return Number of orders.
     */
    int getNumOrders();

    int getNextScheduledOrder();
}
