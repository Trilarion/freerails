/*
 * TrainOrders.java
 *
 * Created on 31 March 2003, 23:17
 */

package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;

/**
 * This class encapsulates the orders for a train.  
 * @author  Luke
 */
public class TrainOrdersModel implements FreerailsSerializable {
    
    public final boolean waitUntilFull;
    
    public final int[] consist;;   //The wagon types to add; if null, then no change.
    
    public final int station; //The number of the station to goto.
    
    /** Creates a new instance of TrainOrders */
    public TrainOrdersModel(int station, int[] newConsist, boolean wait) {
        waitUntilFull=wait;
        consist= newConsist;
        this.station = station;
    }
    
    public int[] getConsist(){
        return this.consist;
    }
    
    public int getStationNumber(){
        return station;
    }    
}
