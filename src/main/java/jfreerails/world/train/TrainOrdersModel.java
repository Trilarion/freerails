/*
 * TrainOrders.java
 *
 * Created on 31 March 2003, 23:17
 */
package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.ImInts;

/**
 * This class encapsulates the orders for a train.
 * 
 * @author Luke
 */
public class TrainOrdersModel implements FreerailsSerializable {
    private static final long serialVersionUID = 3616453397155559472L;

    private static final int MAXIMUM_NUMBER_OF_WAGONS = 6;

    public final boolean waitUntilFull;

    public final boolean autoConsist;

    /** The wagon types to add; if null, then no change. */
    public final ImInts consist;

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
        if (consist != null ? !consist.equals(trainOrdersModel.consist)
                : trainOrdersModel.consist != null)
            return false;

        return true;
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

    public final int stationId; // The number of the station to goto.

    public TrainOrdersModel(int station, ImInts newConsist, boolean wait,
            boolean auto) {
        // If there are no wagons, set wait = false.
        wait = (null == newConsist || 0 == newConsist.size()) ? false : wait;

        this.waitUntilFull = wait;
        this.consist = newConsist;
        this.stationId = station;
        this.autoConsist = auto;
    }

    /**
     * @return either (1) an array of cargo type ids or (2) null to represent
     *         'no change'.
     */
    public ImInts getConsist() {
        return this.consist;
    }

    public int getStationID() {
        return stationId;
    }

    public boolean isNoConsistChange() {
        return null == consist;
    }

    public boolean getWaitUntilFull() {
        return waitUntilFull;
    }

    public boolean orderHasWagons() {
        return null != consist && 0 != consist.size();
    }

    public boolean hasLessThanMaxiumNumberOfWagons() {
        return null == consist || consist.size() < MAXIMUM_NUMBER_OF_WAGONS;
    }

    public boolean isAutoConsist() {
        return autoConsist;
    }
}