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
 * Created on 24-Aug-2003
 *
  */
package org.railz.world.train;

import java.util.*;

import org.railz.world.top.ObjectKey;
import org.railz.world.common.FreerailsSerializable;

/**
 * @author Luke Lindsay
 *
 */
public class ImmutableSchedule implements Schedule, FreerailsSerializable {
    static final long serialVersionUID = 3543587943412566094L;

    protected TrainOrdersModel[] orders;

    /** TODO remove this */
    protected int firstId;

    /**
     * @param orders Array of TrainOrdersModel representing stations to go to.
     */
    public ImmutableSchedule(TrainOrdersModel[] orders) {
	this.orders = new TrainOrdersModel[orders.length];
	for (int i = 0; i < orders.length; i++) {
	    this.orders[i] = orders[i];
	}
    }

    public int getNumOrders() {
        return orders.length;
    }

    public TrainOrdersModel getOrder(int i) {
	return orders[i];
    }

    public boolean equals(Object o) {
        if (o instanceof ImmutableSchedule) {
            ImmutableSchedule test = (ImmutableSchedule)o;
	    return Arrays.equals(orders, test.orders);
        }
	return false;
    }

    public int hashCode() {
	/* probably a very bad hash! */
	return orders.length;
    }

    public String toString() {
	int i = 0;
	String s = "";
	while (i < orders.length) {
	    if (i > 0)
		s +=", ";
	    s += "order " + i + ": ";
	    s += orders[i].toString();
	    i++;
	}
	return s;
    }
}
