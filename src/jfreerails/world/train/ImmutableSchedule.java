/*
 * Created on 24-Aug-2003
 *
  */
package jfreerails.world.train;

import java.util.Arrays;

import jfreerails.world.common.FreerailsSerializable;

/**
 * @author Luke Lindsay
 *
 */
public class ImmutableSchedule implements Schedule, FreerailsSerializable {

	private final TrainOrdersModel[] orders;

	private final int nextScheduledOrder;

	private final boolean hasPriorityOrders;

	public ImmutableSchedule(
		TrainOrdersModel[] orders,
		int gotoStation,
		boolean hasPriorityOrders) {
		this.orders = (TrainOrdersModel[]) orders.clone();
		this.nextScheduledOrder = gotoStation;
		this.hasPriorityOrders = hasPriorityOrders;
	}

	public TrainOrdersModel getOrder(int i) {
		return orders[i];
	}

	public int getOrderToGoto() {
		return hasPriorityOrders ? 0 : nextScheduledOrder;
	}

	public int getStationToGoto() {
		return orders[getOrderToGoto()].getStationNumber();
	}

	public int[] getWagonsToAdd() {
		return orders[getOrderToGoto()].consist;
	}

	public boolean hasPriorityOrders() {
		return hasPriorityOrders;
	}

	public int getNumOrders() {
		return orders.length;
	}

	public int getNextScheduledOrder() {
		return this.nextScheduledOrder;
	}

	
	public boolean equals(Object o) {
		if(o instanceof ImmutableSchedule){
			ImmutableSchedule test = (ImmutableSchedule)o;
			
			return this.hasPriorityOrders == test.hasPriorityOrders 
			&& this.nextScheduledOrder == test.nextScheduledOrder
			&& Arrays.equals(this.orders, test.orders);
		}else{
			return false;
		}
		
	}

}
