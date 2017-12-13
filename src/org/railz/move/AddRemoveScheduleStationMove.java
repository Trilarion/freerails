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
package org.railz.move;

import java.util.*;

import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.train.*;

public class AddRemoveScheduleStationMove extends CompositeMove {
    private static final int ADD = 0;
    private static final int REMOVE = 1;
    private static final int CHANGE = 2;

    private ObjectKey mSchedule;
    private int mStationIndex;

    /**
     * The list of trains whose iterator index is greater than this schedules,
     * and need to be updated.
     */
    private ObjectKey[] mTrains;

    /** true if this is move adds, removes or changes a station */
    private int mOperation;

    private AddRemoveScheduleStationMove(ObjectKey aSchedule, int aStationIndex,
	    ArrayList aMoves, ObjectKey[] aTrains, int aOperation) {
	super(aMoves);
	mStationIndex = aStationIndex;
	mSchedule = aSchedule;

	mTrains = aTrains;
	mOperation = aOperation;
    }

    private static ArrayList identifyAffectedTrains(ReadOnlyWorld w,
	    FreerailsPrincipal p, ObjectKey aSchedule, int aStationIndex,
	    int operation) {
	NonNullElements i = new NonNullElements(KEY.TRAINS, w,
		p);
	ArrayList trainKeys = new ArrayList();
	if (operation == ADD) {
	    while (i.next()) {
		TrainModel tm = (TrainModel) i.getElement();
		ScheduleIterator si = tm.getScheduleIterator();
		if (si.getScheduleKey().equals(aSchedule) &&
			si.getCurrentOrderIndex() >= aStationIndex)
		    trainKeys.add(new ObjectKey(KEY.TRAINS, p, i.getIndex()));
	    }
	} else if (operation == REMOVE) {
	    while (i.next()) {
		TrainModel tm = (TrainModel) i.getElement();
		ScheduleIterator si = tm.getScheduleIterator();
		ObjectKey scheduleKey = si.getScheduleKey();
		Schedule s = (Schedule) w.get(scheduleKey.key,
		       	scheduleKey.index, scheduleKey.principal);
		if (si.getScheduleKey().equals(aSchedule) &&
			si.getCurrentOrderIndex() > aStationIndex ||
			s.getNumOrders() - 1 == si.getCurrentOrderIndex())
		    trainKeys.add(new ObjectKey(KEY.TRAINS, p, i.getIndex()));

	    }
	} else if (operation == CHANGE) {
	    while (i.next()) {
		TrainModel tm = (TrainModel) i.getElement();
		ScheduleIterator si = tm.getScheduleIterator();
		if (si.getScheduleKey().equals(aSchedule) &&
			si.getCurrentOrderIndex() == aStationIndex)
		    trainKeys.add(new ObjectKey(KEY.TRAINS, p, i.getIndex()));

	    }
	}
	return trainKeys;
    }

    public static AddRemoveScheduleStationMove generateAddMove
	(ObjectKey aSchedule, int aStationIndex, ReadOnlyWorld w,
	 TrainOrdersModel aOrder) {
	    ImmutableSchedule before = (ImmutableSchedule)
	       	w.get(KEY.TRAIN_SCHEDULES, aSchedule.index,
		       	aSchedule.principal);
	MutableSchedule ms = new MutableSchedule(before);
	ms.addOrder(aStationIndex, aOrder);
	ImmutableSchedule after = ms.toImmutableSchedule();
	ChangeItemInListMove trainOrdersModelMove = new
	    ChangeTrainScheduleMove(KEY.TRAIN_SCHEDULES, aSchedule.index,
		    before, after, aSchedule.principal);
	ArrayList changeTrainMoves = new ArrayList();
	changeTrainMoves.add(trainOrdersModelMove);
	ArrayList trains = identifyAffectedTrains(w, aSchedule.principal,
		aSchedule, aStationIndex, ADD);
	ObjectKey[] trainKeys =  (ObjectKey[]) trains.toArray
	    (new ObjectKey[trains.size()]);
	GameTime now = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);
	for (int i = 0; i < trains.size(); i++) {
	    TrainModel tm = (TrainModel) w.get(KEY.TRAINS,
		    trainKeys[i].index, trainKeys[i].principal);
	    ScheduleIterator si = tm.getScheduleIterator();
	    ScheduleIterator newSi = si.nextIndex(after);
	    ChangeTrainMove ctm =
		ChangeTrainMove.generateMove(trainKeys[i].index,
			trainKeys[i].principal, tm, newSi, now);
	    changeTrainMoves.add(ctm);
	}
	return new AddRemoveScheduleStationMove(aSchedule, aStationIndex,
		changeTrainMoves, trainKeys, ADD);
    }
    
    public static AddRemoveScheduleStationMove generateChangeMove (ObjectKey
	    aSchedule, int aStationIndex, TrainOrdersModel newOrder,
	    ReadOnlyWorld w) {
	ImmutableSchedule before = (ImmutableSchedule)
	    w.get(KEY.TRAIN_SCHEDULES, aSchedule.index,
		    aSchedule.principal);
	MutableSchedule ms = new MutableSchedule(before);
	ms.setOrder(aStationIndex, newOrder);
	ImmutableSchedule after = ms.toImmutableSchedule();
	ChangeItemInListMove trainOrdersModelMove = new
	    ChangeTrainScheduleMove(KEY.TRAIN_SCHEDULES, aSchedule.index,
		    before, after, aSchedule.principal);
	ArrayList changeTrainMoves = new ArrayList();
	changeTrainMoves.add(trainOrdersModelMove);
	ArrayList trains = identifyAffectedTrains
	    (w, aSchedule.principal,
	     aSchedule, aStationIndex, CHANGE);
	ObjectKey[] trainKeys =  (ObjectKey[]) trains.toArray
	    (new ObjectKey[trains.size()]);
	GameTime now = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);
	for (int i = 0; i < trains.size(); i++) {
	    TrainModel tm = (TrainModel) w.get(KEY.TRAINS,
		    trainKeys[i].index, trainKeys[i].principal);
	    ScheduleIterator si = tm.getScheduleIterator();
	    // Generate the move even though scheduleIterator
	    // is unchanged in order to clear current path
	    // to destination of the train
	    ChangeTrainMove ctm =
		ChangeTrainMove.generateMove(trainKeys[i].index,
			trainKeys[i].principal, tm, si, now);
	    changeTrainMoves.add(ctm);
	}
	return new AddRemoveScheduleStationMove(aSchedule, aStationIndex,
		changeTrainMoves, trainKeys, CHANGE);
    }

    public static AddRemoveScheduleStationMove generateRemoveMove
	(ObjectKey aSchedule, int aStationIndex, ReadOnlyWorld w) {
	    ImmutableSchedule before = (ImmutableSchedule)
	       	w.get(KEY.TRAIN_SCHEDULES, aSchedule.index,
		       	aSchedule.principal);
	    MutableSchedule ms = new MutableSchedule(before);
	    ms.removeOrder(aStationIndex);
	    ImmutableSchedule after = ms.toImmutableSchedule();
	    ChangeItemInListMove trainOrdersModelMove = new
		ChangeTrainScheduleMove(KEY.TRAIN_SCHEDULES, aSchedule.index,
			before, after, aSchedule.principal);
	    ArrayList changeTrainMoves = new ArrayList();
	    changeTrainMoves.add(trainOrdersModelMove);
	    ArrayList trains = identifyAffectedTrains
		(w, aSchedule.principal,
		 aSchedule, aStationIndex, REMOVE);
	    ObjectKey[] trainKeys =  (ObjectKey[]) trains.toArray
		(new ObjectKey[trains.size()]);
	    GameTime now = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);
	    for (int i = 0; i < trains.size(); i++) {
		TrainModel tm = (TrainModel) w.get(KEY.TRAINS,
			trainKeys[i].index, trainKeys[i].principal);
		ScheduleIterator si = tm.getScheduleIterator();
		ScheduleIterator newSi = si.prevIndex(after);
		ChangeTrainMove ctm =
		    ChangeTrainMove.generateMove(trainKeys[i].index,
			    trainKeys[i].principal, tm, newSi, now);
		changeTrainMoves.add(ctm);
	    }
	    return new AddRemoveScheduleStationMove(aSchedule, aStationIndex,
		    changeTrainMoves, trainKeys, REMOVE);
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
	// verify the affected trains are still the same before applying move
	ObjectKey[] actualKeys = (ObjectKey[])
	    identifyAffectedTrains(w, p, mSchedule, mStationIndex, mOperation)
	    .toArray(new ObjectKey[mTrains.length]);

	if (! Arrays.equals(actualKeys, mTrains))
	    return MoveStatus.MOVE_FAILED;

	return super.tryDoMove(w, p);
    }

    public boolean equals(Object o) {
	if (! (o instanceof AddRemoveScheduleStationMove))
	    return false;

	AddRemoveScheduleStationMove arssm = (AddRemoveScheduleStationMove) o;
	return mSchedule.equals(arssm.mSchedule) &&
	    mStationIndex == arssm.mStationIndex &&
	    mOperation == arssm.mOperation &&
	    Arrays.equals(this.mTrains, arssm.mTrains) &&
	    super.equals(arssm);
    } 
}
