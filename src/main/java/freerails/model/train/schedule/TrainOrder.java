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
     * @param station
     * @param newConsist
     * @param wait
     * @param auto
     */
    public TrainOrder(int station, List<Integer> newConsist, boolean wait, boolean auto) {
        // If there are no wagons, set wait = false.
        wait = (null == newConsist || 0 == newConsist.size()) ? false : wait;

        waitUntilFull = wait;
        consist = newConsist;
        stationId = station;
        autoConsist = auto;
    }

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

    @Override
    public int hashCode() {
        int result;
        result = (isWaitUntilFull() ? 1 : 0);
        result = 29 * result + (isAutoConsist() ? 1 : 0);
        result = 29 * result + (getConsist() != null ? getConsist().hashCode() : 0);
        result = 29 * result + getStationId();
        return result;
    }

    /**
     * The wagon types to add; if null, then no change.
     */ /**
     * @return either (1) an array of cargo type ids or (2) null to represent
     * 'no change'.
     */
    public List<Integer> getConsist() {
        return consist;
    }

    /**
     * @return
     */
    public int getStationID() {
        return getStationId();
    }

    /**
     * @return
     */
    public boolean getWaitUntilFull() {
        return isWaitUntilFull();
    }

    /**
     * @return
     */
    public boolean orderHasWagons() {
        return null != getConsist() && 0 != getConsist().size();
    }

    /**
     * @return
     */
    public boolean hasLessThanMaximumNumberOfWagons() {
        return null == getConsist() || getConsist().size() < MAXIMUM_NUMBER_OF_WAGONS;
    }

    /**
     * @return
     */
    public boolean isAutoConsist() {
        return autoConsist;
    }

    public boolean isWaitUntilFull() {
        return waitUntilFull;
    }

    /**
     * The number of the station to goto.
     */
    public int getStationId() {
        return stationId;
    }
}