/*
 * TrainOrders.java
 *
 * Created on 31 March 2003, 23:17
 */
package jfreerails.world.train;

import java.util.Arrays;
import jfreerails.world.common.FreerailsSerializable;


/**
 * This class encapsulates the orders for a train.
 * @author  Luke
 */
public class TrainOrdersModel implements FreerailsSerializable {
    public static final int MAXIMUM_NUMBER_OF_WAGONS = 6;
    public final boolean waitUntilFull;
    public final int[] consist; //The wagon types to add; if null, then no change.
    public final int station; //The number of the station to goto.

    /** Creates a new instance of TrainOrders */
    public TrainOrdersModel(int station, int[] newConsist, boolean wait) {
        //If there are no wagons, set wait = false.
        wait = (null == newConsist || 0 == newConsist.length) ? false : wait;

        waitUntilFull = wait;
        consist = newConsist;
        this.station = station;
    }

    public int[] getConsist() {
        return this.consist;
    }

    public int getStationNumber() {
        return station;
    }

    public boolean isNoConsistChange() {
        return null == consist;
    }

    public boolean getWaitUntilFull() {
        return waitUntilFull;
    }

    public boolean orderHasWagons() {
        return null != consist && 0 != consist.length;
    }

    public boolean hasLessThanMaxiumNumberOfWagons() {
        return null == consist || consist.length < MAXIMUM_NUMBER_OF_WAGONS;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TrainOrdersModel) {
            TrainOrdersModel test = (TrainOrdersModel)obj;

            return this.waitUntilFull == test.waitUntilFull &&
            this.station == test.station &&
            Arrays.equals(this.consist, test.consist);
        } else {
            return false;
        }
    }
}