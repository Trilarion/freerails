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
 * TrainOrder.java
 *
 */
package freerails.model.train.schedule;

import java.io.Serializable;
import java.util.List;

/**
 * Encapsulates the orders for a train.
 */
public class TrainOrder implements Serializable {

    private static final long serialVersionUID = 3616453397155559472L;
    private static final int MAXIMUM_NUMBER_OF_WAGONS = 6;
    private final boolean waitUntilFull;
    private final boolean autoConsist;
    private final List<Integer> consist;
    private final int stationId;

    /**
     * @param station destination station identifier
     * @param newConsist wagon type identifiers to use, or {@code null} to leave the consist unchanged
     * @param wait whether to wait at the station until the train is full
     * @param auto whether to automatically choose the consist
     */
    public TrainOrder(int station, List<Integer> newConsist, boolean wait, boolean auto) {
        // If there are no wagons, set wait = false.
        wait = null == newConsist || 0 == newConsist.size() ? false : wait;

        waitUntilFull = wait;
        consist = newConsist == null ? null : List.copyOf(newConsist);
        stationId = station;
        autoConsist = auto;
    }

    /** Compares all order settings. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TrainOrder)) return false;

        final TrainOrder trainOrder = (TrainOrder) obj;

        if (isAutoConsist() != trainOrder.isAutoConsist()) return false;
        if (getStationId() != trainOrder.getStationId()) return false;
        if (isWaitUntilFull() != trainOrder.isWaitUntilFull()) return false;
        return getConsist() != null ? getConsist().equals(trainOrder.getConsist()) : trainOrder.getConsist() == null;
    }

    /** Returns a hash code based on the order settings. */
    @Override
    public int hashCode() {
        int result;
        result = isWaitUntilFull() ? 1 : 0;
        result = 29 * result + (isAutoConsist() ? 1 : 0);
        result = 29 * result + (getConsist() != null ? getConsist().hashCode() : 0);
        result = 29 * result + getStationId();
        return result;
    }

    /**
     * Returns the wagon types to use at this stop.
     *
     * @return wagon type identifiers, or {@code null} to leave the consist unchanged
     */
    public List<Integer> getConsist() {
        return consist;
    }

    /** Returns the destination station identifier (legacy capitalized alias). */
    public int getStationID() {
        return getStationId();
    }

    /** Returns whether the order requests waiting until the train is full. */
    public boolean getWaitUntilFull() {
        return isWaitUntilFull();
    }

    /** Returns whether this order explicitly specifies one or more wagons. */
    public boolean orderHasWagons() {
        return null != getConsist() && 0 != getConsist().size();
    }

    /** Returns whether the consist has fewer than the supported maximum number of wagons. */
    public boolean hasLessThanMaximumNumberOfWagons() {
        return null == getConsist() || getConsist().size() < MAXIMUM_NUMBER_OF_WAGONS;
    }

    /** Returns whether the order uses automatic consist selection. */
    public boolean isAutoConsist() {
        return autoConsist;
    }

    /** Returns whether the train should wait until full at this stop. */
    public boolean isWaitUntilFull() {
        return waitUntilFull;
    }

    /** Returns the identifier of the station to visit. */
    public int getStationId() {
        return stationId;
    }
}