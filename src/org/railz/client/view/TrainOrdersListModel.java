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
 * TrainOrdersListModel.java
 *
 * Created on 23 August 2003, 17:49
 */

package org.railz.client.view;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import org.railz.client.model.ModelRoot;
import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.train.*;

/**
 *
 * @author  Luke Lindsay
 */

public class TrainOrdersListModel extends AbstractListModel implements
    ListCellRenderer{
    /** ArrayList of TrainOrderJPanel */
    private ArrayList listCells = new ArrayList();

    private int trainNumber;     

    private ReadOnlyWorld w;

    private ModelRoot modelRoot;

    private Schedule schedule;
    private TrainModel trainModel;
    
    public static final int DONT_GOTO = 0;
    public static final int GOTO_NOW = 1;
    public static final int GOTO_AFTER_PRIORITY_ORDERS = 2;
    
    /** This class holds the values that are needed by the ListCellRender. TrainOrdersListModel.getElementAt(int index) returns
     an instance of this class. */
    public static class TrainOrdersListElement{        
        public final boolean isPriorityOrder; 
        public final int gotoStatus;
        public final TrainOrdersModel order;
        public final int trainNumber;
	public TrainOrdersListElement(boolean isPriorityOrder, int gotoStatus,
		TrainOrdersModel order, int trainNumber) {
            this.isPriorityOrder = isPriorityOrder; 
            this.gotoStatus = gotoStatus;
            this.order = order;      
            this.trainNumber = trainNumber;
        }
    }
    
    /** Creates a new instance of TrainOrdersListModel */
    public TrainOrdersListModel(ModelRoot mr, int trainNumber) {
	modelRoot = mr;
	w = mr.getWorld(); 
	this.trainNumber = trainNumber;
	trainModel = (TrainModel) w.get(KEY.TRAINS, trainNumber,
		modelRoot.getPlayerPrincipal());
	schedule = (Schedule) w.get(KEY.TRAIN_SCHEDULES,
		trainModel.getScheduleIterator().getScheduleKey().index,
	       trainModel.getScheduleIterator().getScheduleKey().principal);
	updateCellRenderers();
    }
    
    private void updateCellRenderers() {
	listCells.clear();
	for (int i = 0; i < getSize(); i++) {
	    TrainOrderJPanel toj = new TrainOrderJPanel();
	    toj.setup(modelRoot, null);
	    listCells.add(toj);
	}
    }

    public Component getListCellRendererComponent(JList list, Object value,
	    int index, boolean isSelected, boolean cellHasFocus) {
	TrainOrderJPanel toj = (TrainOrderJPanel) listCells.get(index);
	toj.update((TrainOrdersListModel.TrainOrdersListElement) value,
		isSelected, index);
	return toj;
    }

    public Object getElementAt(int index) {
	if (trainModel == null)
	    return null;

        ScheduleIterator si = trainModel.getScheduleIterator();
        int gotoStatus;
        if (si.getCurrentOrderIndex() == index) {
            if(si.hasPriorityOrder()){
                gotoStatus = GOTO_AFTER_PRIORITY_ORDERS;
            }else{
                gotoStatus = GOTO_NOW;
            }
        } else {
            if(si.hasPriorityOrder() && 0 == index){
                //These orders are the priority orders.
                gotoStatus = GOTO_NOW;
            } else {
                gotoStatus = DONT_GOTO;
            }
        }
        
        boolean isPriorityOrders = (0 == index && si.hasPriorityOrder());
	TrainOrdersModel order = schedule.getOrder(index);
	return new TrainOrdersListElement(isPriorityOrders, gotoStatus, order,
		trainNumber);
    }
    
    public int getSize() {
	if (schedule == null)
	    return 0;

        return schedule.getNumOrders();
    }
    
    public void fireRefresh() {
	Schedule s = schedule;
	TrainModel tm = trainModel;

	trainModel = (TrainModel) w.get(KEY.TRAINS, trainNumber,
		modelRoot.getPlayerPrincipal());
	
	schedule = (Schedule) w.get(KEY.TRAIN_SCHEDULES,
		tm.getScheduleIterator().getScheduleKey().index,
		tm.getScheduleIterator().getScheduleKey().principal);
	
	updateCellRenderers();

	if (s.getNumOrders() > schedule.getNumOrders()) {
	    fireIntervalRemoved(this, s.getNumOrders() - 1,
		    schedule.getNumOrders());
	} else if (s.getNumOrders() < schedule.getNumOrders()) {
	    fireIntervalAdded(this, schedule.getNumOrders() - 1,
		    s.getNumOrders());
	}
        super.fireContentsChanged(this, 0, schedule.getNumOrders());
    }
}
