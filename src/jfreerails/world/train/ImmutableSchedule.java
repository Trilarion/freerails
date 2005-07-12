/*
 * Created on 24-Aug-2003
 *
 */
package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.ImInts;
import jfreerails.world.common.ImList;

/**
 * A Schedule that is immutable.
 * 
 * @author Luke Lindsay
 * 
 */
public class ImmutableSchedule implements Schedule, FreerailsSerializable {
	private static final long serialVersionUID = 3977858458324318264L;

	private final ImList<TrainOrdersModel> m_orders;

	private final int nextScheduledOrder;

	public int hashCode() {
		int result;
		result = nextScheduledOrder;
		result = 29 * result + (m_hasPriorityOrders ? 1 : 0);

		return result;
	}

	private final boolean m_hasPriorityOrders;

	public ImmutableSchedule(TrainOrdersModel[] orders, int gotoStation,
			boolean hasPriorityOrders) {
		m_orders = new ImList<TrainOrdersModel>(orders);
		nextScheduledOrder = gotoStation;
		m_hasPriorityOrders = hasPriorityOrders;
	}

	public TrainOrdersModel getOrder(int i) {
		return m_orders.get(i);
	}

	public int getOrderToGoto() {
		return m_hasPriorityOrders ? 0 : nextScheduledOrder;
	}

	public int getStationToGoto() {
		int orderToGoto = getOrderToGoto();

		if (-1 == orderToGoto) {
			return -1;
		}
		TrainOrdersModel order = m_orders.get(orderToGoto);
		return order.getStationID();
	}

	public ImInts getWagonsToAdd() {
		TrainOrdersModel order = m_orders.get(getOrderToGoto());
		return order.consist;
	}

	public boolean hasPriorityOrders() {
		return m_hasPriorityOrders;
	}

	public int getNumOrders() {
		return m_orders.size();
	}

	public int getNextScheduledOrder() {
		return this.nextScheduledOrder;
	}

	public boolean stopsAtStation(int stationNumber) {
		for (int i = 0; i < this.getNumOrders(); i++) {
			TrainOrdersModel order = this.getOrder(i);

			if (order.getStationID() == stationNumber) {
				return true;
			}
		}

		return false;
	}

	public boolean equals(Object o) {
		if (o instanceof ImmutableSchedule) {
			ImmutableSchedule test = (ImmutableSchedule) o;

			return this.m_hasPriorityOrders == test.m_hasPriorityOrders
					&& this.nextScheduledOrder == test.nextScheduledOrder
					&& this.m_orders.equals(test.m_orders);
		}
		return false;
	}

	public boolean autoConsist() {
		TrainOrdersModel order = m_orders.get(getOrderToGoto());
		return order.autoConsist;
	}
}