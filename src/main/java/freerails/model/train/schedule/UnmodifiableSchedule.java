/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
package freerails.model.train.schedule;


import java.io.Serializable;
import java.util.List;

/**
 * Read-only access to the orders and current destination of a train schedule.
 */
public interface UnmodifiableSchedule extends Serializable {

    int PRIORITY_ORDERS = 0;
    int MAXIMUM_NUMBER_OF_ORDER = 6;

    /**
     * Returns the order at the specified schedule index.
     *
     * @param index zero-based order index
     * @return order at {@code index}
     * @throws IndexOutOfBoundsException if the index is outside the schedule
     */
    TrainOrder getOrder(int index);

    /**
     * Returns the index of the order the train is currently carrying out, or {@code -1}
     * when the schedule is empty.
     */
    int getCurrentOrderIndex();

    /**
     * Returns the station number of the next station the train is scheduled to
     * stop at, or {@code -1} when the schedule is empty.
     */
    int getNextStationId();

    /**
     * Returns the wagons to add at the next scheduled stop.
     *
     * @throws IllegalStateException if the schedule has no current order
     */

    List<Integer> getWagonsToAdd();

    /**
     * Returns the value for the auto consist flag at the next scheduled stop.
     *
     * @throws IllegalStateException if the schedule has no current order
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
    int getNumberOfOrders();

    /** Returns the index of the next scheduled (non-priority) order. */
    int getNextScheduledOrder();

    /**
     * Returns whether any order in the schedule visits the specified station.
     *
     * @param stationId station identifier
     * @return {@code true} if an order targets that station
     */
    boolean stopsAtStation(int stationId);
}