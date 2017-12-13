/*
 * Copyright (C) 2004 Robert Tuck
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
package org.railz.world.train;

import org.railz.world.common.FreerailsSerializable;
import org.railz.world.player.*;
import org.railz.world.top.*;
/**
 * @author rtuck99@users.berlios.de
 */
public class ScheduleIterator implements FreerailsSerializable {
    public static final int NO_CURRENT_ORDER = -1;

    private int currentOrder = NO_CURRENT_ORDER;
    private ObjectKey scheduleKey;
    private TrainOrdersModel priorityOrder;

    public String toString() {
	return "currentOrder = " + currentOrder + ", scheduleKey = " +
	    scheduleKey + ", priorityOrder = " + priorityOrder;
    }

    public ScheduleIterator(ObjectKey scheduleKey, int currentOrder) {
	assert scheduleKey != null;
	this.scheduleKey = scheduleKey;
	this.currentOrder = currentOrder;
    }

    /**
     * @return the next regular scheduled order index
     */
    public int getCurrentOrderIndex() {
	return  currentOrder;
    }

    /**
     * @return true if a priority order is scheduled
     */
    public boolean hasPriorityOrder() {
	return (priorityOrder != null);
    }

    /**
     * @return the current order or null if there is no current order
     */
    public TrainOrdersModel getCurrentOrder(ReadOnlyWorld w) {
	if (priorityOrder != null) {
	    return priorityOrder;
	}
	if (currentOrder == NO_CURRENT_ORDER)
	    return null;

	Schedule s = (Schedule) w.get(KEY.TRAIN_SCHEDULES, scheduleKey.index,
		scheduleKey.principal);
	return s.getOrder(currentOrder);
    }

    public ScheduleIterator(ScheduleIterator i) {
	assert i.scheduleKey != null;
	currentOrder = i.currentOrder;
	scheduleKey = i.scheduleKey;
	priorityOrder = i.priorityOrder;
    }

    public ScheduleIterator(ScheduleIterator i, TrainOrdersModel
	    priorityOrder) {
	assert i.scheduleKey != null;
	currentOrder = i.currentOrder;
	scheduleKey = i.scheduleKey;
	this.priorityOrder = priorityOrder;
    }

    public ScheduleIterator nextOrder(ReadOnlyWorld w) {
	if (priorityOrder != null) {
	    return new ScheduleIterator(this, null);
	} else {
	    Schedule s = (Schedule) w.get(KEY.TRAIN_SCHEDULES,
		    scheduleKey.index, scheduleKey.principal);
	    if (s.getNumOrders() <= currentOrder + 1) {
		return new ScheduleIterator(scheduleKey, 0);
	    }
	    return new ScheduleIterator(scheduleKey, currentOrder + 1);
	}
    }

    /**
     * Used to update the iterator when the underlying schedule is changed
     */
    public ScheduleIterator prevIndex(Schedule s) {
	int index = currentOrder <= 0 ? s.getNumOrders() - 1 : currentOrder -
	    1;
	ScheduleIterator si = new ScheduleIterator(this, priorityOrder);
	si.currentOrder = index;
	return si;
    }

    /**
     * Used to update the iterator when the underlying schedule is changed
     */
    public ScheduleIterator nextIndex(Schedule s) {
	int index = currentOrder + 1;
	if (index >= s.getNumOrders())
	    index = s.getNumOrders() > 0 ? 0 : NO_CURRENT_ORDER;
	ScheduleIterator si = new ScheduleIterator(this, priorityOrder);
	si.currentOrder = index;
	return si;
    }

    public ObjectKey getScheduleKey() {
	return scheduleKey;
    }

    public boolean equals(Object o) {
	if (o == null || !(o instanceof ScheduleIterator))
	    return false;

	ScheduleIterator i = (ScheduleIterator) o;

	return currentOrder == i.currentOrder &&
	    scheduleKey.equals(i.scheduleKey) &&
	    ((priorityOrder == null) ? (i.priorityOrder == null) :
	    (priorityOrder.equals(i.priorityOrder)));
    }

    public int hashCode() {
	return currentOrder ^ scheduleKey.index;
    }
}
