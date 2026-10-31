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
package freerails.model.operations.train.schedule;

import java.io.Serializable;

/** Read-only access to the ordered stops and current destination of a train schedule. */
public interface UnmodifiableSchedule extends Serializable {

    int MAXIMUM_NUMBER_OF_STOPS = 6;

    /** Returns the stop at the specified schedule index. */
    TrainOrder getOrder(int index);

    /** Returns the index of the current stop, or {@code -1} when the schedule is empty. */
    int getCurrentOrderIndex();

    /** Returns the station identifier of the current stop, or {@code -1} when empty. */
    int getNextStationId();

    /** Returns the total number of stops, including a one-shot urgent stop. */
    int getNumberOfOrders();

    /** Returns whether any stop visits the specified station. */
    boolean stopsAtStation(int stationId);
}
