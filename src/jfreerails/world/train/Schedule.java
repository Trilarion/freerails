/*
 * Schedule.java
 *
 * Created on 22 January 2002, 20:14
 */

package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;

/** This class represents a train's schedule.  That is, which stations that the train should
 * visit and what wagons the engine should pull.
 *
 * @author  lindsal
 */
public class Schedule implements FreerailsSerializable {
    
    public static final int MAX_NUMBER_OF_ORDERS=4;
    
    public static final int PRIORITY_ORDERS=0;
    
    private final TrainOrdersModel[] orders = new TrainOrdersModel[MAX_NUMBER_OF_ORDERS+1];
    
    private int stationToGoto=1;
    
    public Schedule(){
        
    }
    
    public void removeOrder(int orderNumber){
        if(PRIORITY_ORDERS == orderNumber){
            //If we are removing the prority stop.
            orders[PRIORITY_ORDERS]=null;
        }else{
            //Otherwise, remove the specified order, and shift the orders below up.
            orders[orderNumber]=null;
            
            for(int i = orderNumber; i< MAX_NUMBER_OF_ORDERS; i++){
                orders[i] = orders[i+1];
            }
        }
    }
    
    public void setOrder(int orderNumber, TrainOrdersModel order){
        orders[orderNumber]=order;
    }
    
    public TrainOrdersModel getOrder(int i){
        return orders[i];
    }
    
    /** Returns the number of the order the train is currently carry out. */
    public int getOrderToGoto(){
        return stationToGoto;
    }
    
    public void setOrderToGoto(int i){
        if(i<0 || i > MAX_NUMBER_OF_ORDERS){
            throw new IllegalArgumentException(String.valueOf(i));
        }
        stationToGoto = i;
    }
    
    /** Returns the station number of the next station the train is scheduled to stop at. */
    public int getStationToGoto() {
        return orders[stationToGoto].getStationNumber();
    }
    
    /** Returns the wagons to add at the next scheduled stop. */
    public int[] getWagonsToAdd(){
        return orders[stationToGoto].getConsist();
    }
    
    public void gotoNextStaton(){
        stationToGoto++;
        if(MAX_NUMBER_OF_ORDERS<stationToGoto){
            stationToGoto=1;
        }
    }
    
}

