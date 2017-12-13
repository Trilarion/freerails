/*
 * TrainConsistListModel.java
 *
 * Created on 22 August 2003, 20:03
 */

package jfreerails.client.view;

import javax.swing.*;
import jfreerails.world.top.*;
import jfreerails.world.train.*;

/**
 *
 * @author  Luke Lindsay
 */
public class TrainConsistListModel extends AbstractListModel {
    
    private ReadOnlyWorld w;
    
    private int trainNumber;      
    
    /** Creates a new instance of TrainConsistListModel */
    public TrainConsistListModel(ReadOnlyWorld w, int trainNumber) {
        this.w = w;
        this.trainNumber = trainNumber;
    }
    
    
    /** Returns an Integer, a negative value represents an engine type; a positive value represents
     *a wagon type.
     */
    public Object getElementAt(int index) {
         TrainModel train = getTrain();
         if(0 == index){
            return new TypeID(train.getEngineType(), KEY.ENGINE_TYPES);
         }else{
            return new TypeID(train.getWagon(index-1), KEY.WAGON_TYPES);
         }
    }
    
    public int getSize() {
        TrainModel train = getTrain();
        return train.getNumberOfWagons() + 1; //wagons + engine.
    }
    
    private TrainModel getTrain(){
        return (TrainModel)w.get(KEY.TRAINS, trainNumber);
    }
    
}
