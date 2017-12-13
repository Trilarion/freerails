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
 * TrainOrders.java
 *
 * Created on 31 March 2003, 23:17
 */
package org.railz.world.train;

import java.util.Arrays;

import org.railz.world.common.FreerailsSerializable;
import org.railz.world.top.ObjectKey;

/**
 * This class encapsulates the orders for a train at a particular stop on its
 * schedule.
 * @author  Luke
 */
public class TrainOrdersModel implements FreerailsSerializable {
    static final long serialVersionUID = 2414702987622334721L;

    /**
     * The maximum number of wagons that a train may consist of
     */
    public static final int MAXIMUM_NUMBER_OF_WAGONS = 6;

    /**
     * Whether the train should wait for cargo at this stop.
     */
    public final boolean waitUntilFull;

    /**
     * Array of indices into the WAGON_TYPES table.
     */
    public final int[] consist;
    
    /**
     * The station at this stop
     */
    public final ObjectKey station;

    /**
     * Whether cargo should be loaded at this stop
     */
    public final boolean loadTrain;

    /**
     * Whether cargo should be unloaded at this stop
     */
    public final boolean unloadTrain;

    /**
     *  Creates a new instance of TrainOrders
     */
    public TrainOrdersModel(ObjectKey station, int[] newConsist, boolean wait,
	    boolean loadTrain, boolean unloadTrain) {
	assert newConsist != null;
        //If there are no wagons, set wait = false.
        wait = (0 == newConsist.length) ? false : wait;

        waitUntilFull = wait;
        consist = (int []) newConsist.clone();
        this.station = station;
	this.loadTrain = loadTrain;
	this.unloadTrain = unloadTrain;
    }

    public int[] getConsist() {
        return this.consist;
    }

    public ObjectKey getStationNumber() {
        return station;
    }

    public boolean getWaitUntilFull() {
        return waitUntilFull;
    }

    public boolean orderHasWagons() {
        return 0 != consist.length;
    }

    public boolean hasLessThanMaxiumNumberOfWagons() {
        return consist.length < MAXIMUM_NUMBER_OF_WAGONS;
    }

    public int hashCode() {
	return station.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj != null && obj instanceof TrainOrdersModel) {
            TrainOrdersModel test = (TrainOrdersModel)obj;

            return this.waitUntilFull == test.waitUntilFull &&
            this.station.equals(test.station) &&
	    unloadTrain == test.unloadTrain &&
	    loadTrain == test.loadTrain &&
            Arrays.equals(this.consist, test.consist);
        } else {
            return false;
        }
    }

    public String toString() {
	String s = "";
	s = "waitUntilFull=" + waitUntilFull + ", stationIndex=" + 
	    station.index + ", loadTrain=" + loadTrain + 
	    ", unloadTrain=" + unloadTrain + ", consist=(";
	for (int i = 0; i < consist.length; i++)  {
	    if (i > 0) {
		s += ", ";
	    }
	    s += consist[i];
	}
	s += ")";
	return s;
    }
}
