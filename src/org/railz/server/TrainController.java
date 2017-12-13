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
package org.railz.server;

import java.awt.Point;

import org.railz.move.*;
import org.railz.controller.*;
import org.railz.world.common.*;
import org.railz.world.building.*;
import org.railz.world.player.*;
import org.railz.world.station.*;
import org.railz.world.top.*;
import org.railz.world.track.*;
import org.railz.world.train.*;

/**
 * Responsible for controlling the state of trains. This package controls the
 * starting and stopping of trains at train stations, and the changing of
 * train destinations.
 */
class TrainController {
    /**
     * Time to wait in station whilst unloading cargo
     */
    private static final int UNLOADING_DELAY = 30;
    
    /**
     * Time to wait in station whilst loading cargo
     */
    private static final int LOADING_DELAY = 30;
    
    private ReadOnlyWorld world;
    private AuthoritativeMoveExecuter moveReceiver;
    private DropOffAndPickupCargoMoveGenerator dopucmg;
    private TrainPathFinder pathFinder;

    public TrainController(ReadOnlyWorld w, AuthoritativeMoveExecuter mr) {
	world = w;
	moveReceiver = mr;
	dopucmg = new DropOffAndPickupCargoMoveGenerator(world, moveReceiver);
	pathFinder = new TrainPathFinder(world);
	trainModelViewer = new TrainModelViewer(world);
    }

    public void updateTrains() {
	NonNullElements i = new NonNullElements(KEY.PLAYERS, world,
		Player.AUTHORITATIVE);
	GameTime now = (GameTime) world.get(ITEM.TIME, Player.AUTHORITATIVE);
	while (i.next()) {
	    FreerailsPrincipal p = ((Player) i.getElement()).getPrincipal();
	    NonNullElements j = new NonNullElements(KEY.TRAINS, world, p);
	    while (j.next()) {
		TrainModel tm = (TrainModel) j.getElement();
		updateTrain(tm, now, p, j.getIndex());
	    }
	}
    }

    private void updateTrain(TrainModel tm, GameTime now, FreerailsPrincipal
	    p, int trainIndex) {
	int state = tm.getState();
	/* Is the train lost and we are trying to go somewhere ? */
	if (tm.getTrainMotionModel().isLost() && state ==
		TrainModel.STATE_RUNNABLE) {
	   if (tm.getPosition(now) != null && !tm.isBlocked()) {
	       int newState = setPathToDestination(new ObjectKey(KEY.TRAINS, p,
			   trainIndex), tm);
	       if (newState != state)
		   setState(trainIndex, p, newState);
	       tm = (TrainModel) world.get(KEY.TRAINS, trainIndex, p);
	   }
	   if (tm.getTrainMotionModel().isLost())
	       return;
	}

	TrainOrdersModel tom;
	switch (state) {
	    case TrainModel.STATE_LOADING:
		if (tm.getStateLastChangedTime().getTime() + LOADING_DELAY <
			now.getTime())
		    loadTrain(tm, p, trainIndex);
		return;
	    case TrainModel.STATE_UNLOADING:
		if (tm.getStateLastChangedTime().getTime() + UNLOADING_DELAY <
			now.getTime()) {
		    unloadTrain(trainIndex, p, tm);
		    changeWagons(new ObjectKey(KEY.TRAINS,
				p, trainIndex));
		}
		return;
	    case TrainModel.STATE_RUNNABLE:
		if (checkWater(new ObjectKey(KEY.TRAINS, p, trainIndex), tm))
		    tm = (TrainModel) world.get(KEY.TRAINS, trainIndex, p); 

		/* check to see whether the train has reached its destination
		 */
		if (tm.getTrainMotionModel().reachedDestination(now)) {
		    if(tm.getScheduleIterator().getCurrentOrder(world).
			    unloadTrain) {
			setState(trainIndex, p, TrainModel.STATE_UNLOADING);
			return;
		    }
		    changeWagons(new ObjectKey(KEY.TRAINS,
				p, trainIndex));
		    if (tm.getScheduleIterator().getCurrentOrder(world).
			    loadTrain) {
			setState(trainIndex, p, TrainModel.STATE_LOADING);
			return;
		    }
		    setState(trainIndex, p, TrainModel.STATE_RUNNABLE);
		}
		return;
	    default:
		return;
	}
    }

    private ObjectKey getStationKey(ObjectKey trainKey, TrainModel tm) {
	GameTime t = (GameTime) world.get(ITEM.TIME, Player.AUTHORITATIVE);
	/* get the details of the station the train is at */
	Point point = new Point();
	tm.getPosition(t).getHead(point);
	TrackTile.deltasToTileCoords(point);
	FreerailsPrincipal sp = null;
	NonNullElements j = new NonNullElements(KEY.PLAYERS, world,
		Player.AUTHORITATIVE);
	NonNullElements i = null;
	boolean flag = false;
	StationModel sm;
	while (j.next() && ! flag) {
	    sp = ((Player) j.getElement()).getPrincipal();
	    i = new NonNullElements(KEY.STATIONS, world, sp);
	    while (i.next()) {
		sm = (StationModel) i.getElement();
		if (sm.getStationX() == point.x &&
			sm.getStationY() == point.y) {
		    flag = true;
		    break;
		}
	    }
	}
	if (flag)
	    return new ObjectKey(KEY.STATIONS, sp, i.getIndex());

	return null;
    }

    private void unloadTrain(int trainIndex, FreerailsPrincipal p,
	    TrainModel tm) {
	ObjectKey trainKey = new ObjectKey(KEY.TRAINS, p, trainIndex);
	ObjectKey stationKey = getStationKey(trainKey, tm);
	if (stationKey != null)
	    dopucmg.unloadTrain(trainKey, stationKey);

	setState(trainIndex, p, TrainModel.STATE_LOADING);
    }

    private void loadTrain(TrainModel tm, FreerailsPrincipal p,
	    int trainIndex) {
	ObjectKey trainKey = new ObjectKey(KEY.TRAINS, p, trainIndex);
	ObjectKey stationKey = getStationKey(trainKey, tm);

	/* only load the train if there is sufficient cargo at the station */
	if (stationKey != null &&
	       	dopucmg.checkCargoAtStation(trainKey, stationKey)) {
	    dopucmg.loadTrain(trainKey, stationKey);
	
	    setState(trainIndex, p, TrainModel.STATE_RUNNABLE);
	}

	return;
    }

    /** Change wagons and load water if any */
    private void changeWagons(ObjectKey trainKey) {
	TrainModel tm = (TrainModel) world.get(trainKey.key, trainKey.index,
		trainKey.principal);

	ObjectKey stationKey = getStationKey(trainKey, tm);
	if (stationKey != null) {
	    ScheduleIterator si = tm.getScheduleIterator();
	    TrainOrdersModel tom = si.getCurrentOrder(world);
	    Move m = ChangeTrainMove.generateMove(trainKey.index,
		    trainKey.principal, tm, tm.getEngineType(),
		    tom.getConsist());
	    moveReceiver.processMove(m);

	    // dump or sell any surplus cargo
	    dopucmg.dumpSurplusCargo(trainKey, stationKey);

	    // check to see if there is a water tower
	    StationModel sm = (StationModel) world.get(KEY.STATIONS,
		    stationKey.index, stationKey.principal);
	    if (sm.hasImprovement(WorldConstants.SI_WATER_TOWER)) {
		m = ChangeTrainMove.generateOutOfWaterMove(trainKey, world,
			false);
		moveReceiver.processMove(m);
	    }
	}
    }

    private void setState(int trainIndex, FreerailsPrincipal p, int newState) {
	TrainModel tm = (TrainModel) world.get(KEY.TRAINS, trainIndex, p);

	/* set new destination if current state is anything other than
	 * STOPPED, and our new state is RUNNABLE */
	if (tm.getState() != TrainModel.STATE_STOPPED &&
		newState == TrainModel.STATE_RUNNABLE) {
	    ObjectKey trainKey = new ObjectKey(KEY.TRAINS, p, trainIndex);
	    // set the trains new destination
	    GameTime t = (GameTime) world.get(ITEM.TIME, Player.AUTHORITATIVE);
	    Move ctdm = ChangeTrainMove.generateMove(trainKey.index,
		    trainKey.principal, tm,
		    tm.getScheduleIterator().nextOrder(world), t);
	    moveReceiver.processMove(ctdm);
	}

	// get the updated train model
	tm = (TrainModel) world.get(KEY.TRAINS, trainIndex, p);

	ChangeTrainMove m  = ChangeTrainMove.generateMove(trainIndex, p, tm,
		newState, (GameTime) world.get(ITEM.TIME,
		    Player.AUTHORITATIVE));
	moveReceiver.processMove(m);
    }

    private static String stateToString(int state) {
	switch (state) {
	    case TrainModel.STATE_RUNNABLE:
		return "Runnable";
	    case TrainModel.STATE_STOPPED:
		return "Stopped";
	    case TrainModel.STATE_LOADING:
		return "Loading";
	    case TrainModel.STATE_UNLOADING:
		return "Unloading";
	    default:
		throw new IllegalArgumentException();
	}
    }

    private int setPathToDestination(ObjectKey trainKey, TrainModel tm) {
	final Point head = new Point();
	Point stationCoords = new Point();
	ScheduleIterator si = tm.getScheduleIterator();
	TrainOrdersModel tom = si.getCurrentOrder(world);
	GameTime t = (GameTime) world.get(ITEM.TIME, Player.AUTHORITATIVE);
	
	if (tom == null) {
	    /* no orders */
	    ChangeTrainMove ctm = ChangeTrainMove.generateMove(trainKey.index,
		    trainKey.principal, tm, (TrainPath) null, t);
	    moveReceiver.processMove(ctm);
	    return TrainModel.STATE_STOPPED;
	}
	ObjectKey stationKey = tom.getStationNumber();
	StationModel station = (StationModel) world.get(stationKey.key,
		stationKey.index, stationKey.principal);
	tm.getPosition(t).getHead(head);
	stationCoords.x = station.getStationX();
	stationCoords.y = station.getStationY();
	stationCoords = TrackTile.tileCoordsToDeltas(stationCoords);
	TrainPath tp = pathFinder.findPath(stationCoords, head);
	ChangeTrainMove ctm = ChangeTrainMove.generateMove(trainKey.index,
		trainKey.principal, tm, tp, t);
	moveReceiver.processMove(ctm);

	return TrainModel.STATE_RUNNABLE;
    }

    private TrainModelViewer trainModelViewer;

    /** @return true if we changed the trains state */
    private boolean checkWater(ObjectKey trainKey, TrainModel train) {
	trainModelViewer.setTrainModel(train);
	// get current water state

	boolean isOutOfWater = train.getTrainMotionModel().isOutOfWater();

	if (!isOutOfWater && trainModelViewer.getWaterRemaining() == 0) {
	    // send a move to set out of water
	    Move m = ChangeTrainMove.generateOutOfWaterMove(trainKey, world,
		    true);
	    moveReceiver.processMove(m);
	    return true;
	}
	return false;
    }
}
