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
 * TrainOrders.java
 *
 * Created on 31 March 2003, 23:17
 */
package freerails.world.train;

import freerails.world.FreerailsSerializable;
import freerails.world.common.ImInts;

/**
 * This class encapsulates the orders for a train.
 *
 */
public class TrainOrdersModel implements FreerailsSerializable {
    private static final long serialVersionUID = 3616453397155559472L;

    private static final int MAXIMUM_NUMBER_OF_WAGONS = 6;

    /**
     *
     */
    public final boolean waitUntilFull;

    /**
     *
     */
    public final boolean autoConsist;

    /**
     * The wagon types to add; if null, then no change.
     */
    public final ImInts consist;

    /**
     *
     */
    public final int stationId; // The number of the station to goto.

    /**
     *
     * @param station
     * @param newConsist
     * @param wait
     * @param auto
     */
    public TrainOrdersModel(int station, ImInts newConsist, boolean wait,
                            boolean auto) {
        // If there are no wagons, set wait = false.
        wait = (null == newConsist || 0 == newConsist.size()) ? false : wait;

        this.waitUntilFull = wait;
        this.consist = newConsist;
        this.stationId = station;
        this.autoConsist = auto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TrainOrdersModel))
            return false;

        final TrainOrdersModel trainOrdersModel = (TrainOrdersModel) o;

        if (autoConsist != trainOrdersModel.autoConsist)
            return false;
        if (stationId != trainOrdersModel.stationId)
            return false;
        if (waitUntilFull != trainOrdersModel.waitUntilFull)
            return false;
        return consist != null ? consist.equals(trainOrdersModel.consist) : trainOrdersModel.consist == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = (waitUntilFull ? 1 : 0);
        result = 29 * result + (autoConsist ? 1 : 0);
        result = 29 * result + (consist != null ? consist.hashCode() : 0);
        result = 29 * result + stationId;
        return result;
    }

    /**
     * @return either (1) an array of cargo type ids or (2) null to represent
     * 'no change'.
     */
    public ImInts getConsist() {
        return this.consist;
    }

    /**
     *
     * @return
     */
    public int getStationID() {
        return stationId;
    }

    /**
     *
     * @return
     */
    public boolean isNoConsistChange() {
        return null == consist;
    }

    /**
     *
     * @return
     */
    public boolean getWaitUntilFull() {
        return waitUntilFull;
    }

    /**
     *
     * @return
     */
    public boolean orderHasWagons() {
        return null != consist && 0 != consist.size();
    }

    /**
     *
     * @return
     */
    public boolean hasLessThanMaxiumNumberOfWagons() {
        return null == consist || consist.size() < MAXIMUM_NUMBER_OF_WAGONS;
    }

    /**
     *
     * @return
     */
    public boolean isAutoConsist() {
        return autoConsist;
    }
}