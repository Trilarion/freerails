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

import freerails.util.ImmutableList;

/**
 * Defines methods to access a train's schedule.
 */
public interface Schedule {

    int PRIORITY_ORDERS = 0;
    int MAXIMUM_NUMBER_OF_ORDER = 6;

    /**
     * @param i
     * @return
     */
    TrainOrders getOrder(int i);

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

    ImmutableList<Integer> getWagonsToAdd();

    /**
     * Returns the value for the autoconsist flag at the next scheduled stop.
     */
    boolean autoConsist();

    /**
     * @return
     */
    boolean hasPriorityOrders();

    /**
     * Returns number of non priority orders + number of priority orders.
     *
     * @return Number of orders.
     */
    int getNumOrders();

    /**
     * @return
     */
    int getNextScheduledOrder();
}