/*
 * Schedule.java
 *
 * Created on 22 January 2002, 20:14
 */

package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;
import java.util.Vector;

/** This class represents a train's schedule.  That is, which stations that the
 * train should visit and what wagons the engine should pull.
 *
 * @author  lindsal
 */
public class Schedule implements FreerailsSerializable {
    
    /**
     * Index of the priority station. This station will be the next station on
     * the trains route if it is set. When the station has been reached, this
     * will be reset to null.
     */
    public static final int PRIORITY_ORDERS=0;
    
    /**
     * Vector of TrainOrdersModel
     */
    private final Vector orders = new Vector();
    
    private int stationToGoto=0;
    
    /**
     * Used to save state when we divert to PRIORITY_ORDERS
     */
    private int oldStation = 1;
    
    public Schedule(){
	orders.add(null);
    }
    
    /**
     * removes the order at the specified position
     */
    public void removeOrder(int orderNumber){
	System.out.println("Removing order " + orderNumber);
        if(PRIORITY_ORDERS == orderNumber){
            //If we are removing the prority stop.
            orders.set(0, null);
        }else{
	    //Otherwise, remove the specified order, and shift the orders below
	    //up.
            orders.remove(orderNumber);
        }
            
	/* shift current station down */
	if (stationToGoto > orderNumber)
	    stationToGoto--;
	if (orders.size() <= stationToGoto) {
	    stationToGoto = 1;
            }
        }
    
    /**
     * Inserts an order at the specified position. Note that PRIORITY_ORDERS
     * cannot be added.
     */
    public void addOrder(int orderNumber, TrainOrdersModel order) {
	System.out.println("Adding order number " + orderNumber + ":" + order);
	if (orderNumber == PRIORITY_ORDERS) {
	    throw new IllegalArgumentException("Tried to insert at the " +
	    "PRIORITY_ORDERS");
    }
	orders.add(orderNumber, order);
	if (stationToGoto >= orderNumber) 
	    stationToGoto++;

	if (oldStation >= orderNumber)
	    oldStation++;
    }
    
    public void setOrder(int orderNumber, TrainOrdersModel order){
	System.out.println("Setting order " + orderNumber + ":" + order);
	if (orderNumber >= orders.size()) {
	    orders.add(order);
	} else {
	    orders.set(orderNumber, order);
	}
    }
    
    public TrainOrdersModel getOrder(int i){
        return (TrainOrdersModel) orders.get(i);
    }
    
    /** Returns the number of the order the train is currently carry out. */
    public int getOrderToGoto(){
        return stationToGoto;
    }
    
    public void setOrderToGoto(int i){
	System.out.println ("Set order to go to " + i);
        if( i < 0 || i >= orders.size()){
            throw new IllegalArgumentException(String.valueOf(i));
        }
        stationToGoto = i;
    }
    
    /** Returns the station number of the next station the train is scheduled to
     * stop at. */
    public int getStationToGoto() {
        return ((TrainOrdersModel) orders.get(stationToGoto)).getStationNumber();
    }
    
    /** Returns the wagons to add at the next scheduled stop. */
    public int[] getWagonsToAdd(){
        return ((TrainOrdersModel) orders.get(stationToGoto)).getConsist();
    }
    
    public void gotoNextStaton(){
    System.out.println("Going to next station, current is " + stationToGoto +
    " old is " + oldStation + " size is " + orders.size());
	if (orders.get(PRIORITY_ORDERS) != null) {
	    if(stationToGoto != PRIORITY_ORDERS) {
		oldStation = stationToGoto;
		stationToGoto = PRIORITY_ORDERS;
		return;
	    } else {
		setOrder(PRIORITY_ORDERS, null);
		stationToGoto = oldStation;
	    }
	}
        stationToGoto++;
        if(orders.size() <= stationToGoto){
            stationToGoto=1;
        }
    }
    
    /**
     * @return Number of orders, including the priority order
     */
    public int getNumOrders() {
	return orders.size();
    }
}

