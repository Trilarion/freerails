/*
 * Schedule.java
 *
 * Created on 22 January 2002, 20:14
 */

package jfreerails.world.train;

/** This class represents a train's schedule.  That is, which stations that the train should
 * visit and what wagons the engine should pull.
 *
 * @author  lindsal
 */
public class Schedule {
    
    public static final int MAX_NUMBER_OF_ORDERS=4;
    
    public static final int PRIORITY_ORDERS=0;
    
    private final TrainOrders[] orders = new TrainOrders[MAX_NUMBER_OF_ORDERS+1];
    
    int stationToGoto=1;
    
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
    
    public void setOrder(int orderNumber, TrainOrders order){
        
    }
    
}

