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
import java.util.*;

import org.railz.move.*;
import org.railz.controller.*;
import org.railz.world.common.*;
import org.railz.world.building.*;
import org.railz.world.player.*;
import org.railz.world.station.*;
import org.railz.world.terrain.*;
import org.railz.world.top.*;
import org.railz.world.track.*;
import org.railz.world.train.*;
import org.railz.world.train.TrainPathFunction.TrainPathSegment;

/**
 * Responsible for controlling the state of trains. This package controls the
 * starting and stopping of trains at train stations, and the changing of
 * train destinations.
 */
class TrainController {
    private HashMap lostTrains = new HashMap();

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
		Point head = new Point();
		GameTime now = (GameTime) world.get(ITEM.TIME,
			Player.AUTHORITATIVE);
		tm.getPosition(now).getHead(head);
		Point dest = getCurrentDestination(tm);
		if (dest == null) {
		    m = ChangeTrainMove.generateOutOfWaterMove(trainKey,
			    world, false, null, null);
		} else {
		    TrainPath pathToDestination = pathFinder.findPath(dest,
			    head);
		    TrainPathFunction pathFunction = buildPathFunction
			(pathToDestination, tm, false);
		    m = ChangeTrainMove.generateOutOfWaterMove(trainKey, world,
			    false, pathToDestination, pathFunction);
		}
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

    private static double ROOT_TWO = Math.sqrt(2.0);
    
    private static float getSMax(float s0, Point p1, Point p2) {
	if (p1.x != p2.x && p1.y != p2.y) {
	    return (float) (s0 + ROOT_TWO * TrackTile.DELTAS_PER_TILE);
	} else {
	    return (float) (s0 + TrackTile.DELTAS_PER_TILE);
	}
    }

    private TrainPathSegment getNewSegment(Point p1, Point p2,
	    float t0, float v0, float s0, int mass, float power, EngineType
	    et) {
	float sMax = getSMax(s0, p1, p1);
	return getNewSegment(p1, p2, t0, v0, s0, mass, power, sMax, et);
    }

    /** @return the TrainPathSegment for the traversal from the centre of tile
     * at point p1 to centre of tile at point p2 */
    private TrainPathSegment getNewSegment(Point p1, Point p2,
	    float t0, float v0, float s0, int mass, float power, float sMax,
	    EngineType et) {
	float maxTractiveForce = et.getMaxTractiveForce();
	FreerailsTile ft = world.getTile(p1);
	int ttn = ft.getTerrainTypeNumber();
	TerrainType tt1 = (TerrainType)
	    world.get(KEY.TERRAIN_TYPES, ttn, Player.AUTHORITATIVE);
	ft = world.getTile(p2);
	ttn = ft.getTerrainTypeNumber();
	TerrainType tt2 = (TerrainType) world.get(KEY.TERRAIN_TYPES, ttn,
		Player.AUTHORITATIVE);
	// check the track type to see whether it is a tunnel
	TrackRule trackType = (TrackRule) world.get(KEY.TRACK_RULES,
		ft.getTrackRule(), Player.AUTHORITATIVE);
	float effectiveIncline;
	if (trackType.isTunnel()) {
	    // if we are in a tunnel, then assume it's level
	    effectiveIncline = 0;
	} else {
	    effectiveIncline = (float) (tt2.getElevation() -
		    tt1.getElevation()); 
	    // if we are on a diagonal then our incline is divided by root 2
	    if (p1.x != p2.x && p1.y != p2.y) {
		effectiveIncline /= ROOT_TWO;
	    }
	    effectiveIncline += (float) (tt1.getRoughness() +
			tt2.getRoughness()) / 2;
	    effectiveIncline /= 100;
	}
	float a = et.getAcceleration(effectiveIncline, v0, mass);
	
	return new TrainPathSegment(t0, v0, a, s0, sMax);
    }

    private TrainPathFunction buildPathFunction(TrainPath tp, TrainModel tm,
	    boolean outOfWater) {
	trainModelViewer.setTrainModel(tm);
	ArrayList segList = new ArrayList();
	ArrayList mapCoords = tp.getMapCoordArray();
	// tail of PathToDestination coincides with head of train
	IntLine il = tp.getLastSegment();
	Point p1 = new Point();
	Point p2 = new Point();
	p1.setLocation(il.x2, il.y2);
	p2.setLocation(il.x1, il.y1);
	TrackTile.deltasToTileCoords(p1);
	TrackTile.deltasToTileCoords(p2);
	float s0 = 0;
	int tBase = ((GameTime) world.get(ITEM.TIME,
		    Player.AUTHORITATIVE)).getTime(); 
	float t0 = 0.0f;
	int mass = trainModelViewer.getTotalMass();
	EngineType et = (EngineType) world.get(KEY.ENGINE_TYPES,
		    tm.getEngineType(), Player.AUTHORITATIVE);
	float power = et.getPowerOutput();
	
	if (outOfWater)
	    power /= 2;
	
	float maxTractiveForce = et.getMaxTractiveForce();
	float v0 = 0;
       if (tm.getTrainMotionModel().getPathFunction() != null)
	    v0 = tm.getTrainMotionModel().getPathFunction()
		.getSpeed(tBase);
	// add segment for path from current pos to centre of tile
	if (p1.equals(p2)) {
	    /* TODO do this in smaller chunks */
	    // Last segment is from train head to centre of tile
	    TrainPathSegment seg = getNewSegment(p1, p2, t0, v0, s0, mass,
		    power, (float) il.getLength().getLength(), et);
	    segList.add(seg);
	    t0 = seg.getTMax();
	    if (t0 < 0) {
		// train doesn't have enough power/traction to clmb hill!
		return null;
	    }
	    s0 = seg.getDistance(t0);
	    v0 = seg.getSpeed(t0);
	    p1.setLocation(p2);
	}

	int prefChunks = 1;
	for (int i = mapCoords.size() - 1; i >= 0; i--) {
	    p2 = (Point) mapCoords.get(i);
	    float sMax = getSMax(s0, p1, p2);
	    int currentChunk = 0;
	    int maxChunks = prefChunks;
	    float currentS0 = s0;
	    do {
		TrainPathSegment seg = getNewSegment(p1, p2, t0, v0,
			currentS0, mass, power, s0 + (sMax - s0) *
			((float) (currentChunk + 1) / maxChunks), et);
		float newT0;
		newT0 = seg.getTMax();
		float vDiff = Math.abs(seg.getSpeed(newT0) -
				seg.getSpeed(t0));
		float a = Math.abs(seg.getAcceleration());
		if (prefChunks <= maxChunks && (newT0 < t0 || (vDiff > a * 4 &&
				vDiff > 0.0001))) {
		    // need a better approximation
		    // try smaller chunks
		    maxChunks <<= 1;
		    currentChunk <<= 1;
		    if (maxChunks > 4096) {
			// hill is too steep
			return null;
		    }
		    continue;
		} else if (prefChunks >= maxChunks && 
			(currentChunk % 2) == 0 &&
			maxChunks > 1 && vDiff <= a) {
		    // try larger chunks
		    maxChunks >>= 1;
		    currentChunk >>= 1;
		    continue;
		}
		t0 = newT0;
		segList.add(seg);
		currentS0 = seg.getDistance(t0);
		v0 = seg.getSpeed(t0);
		currentChunk++;
		prefChunks = maxChunks;
	    } while (currentChunk < maxChunks);
	    s0 = currentS0;
	    p1.setLocation(p2);
	}
	return new TrainPathFunction(tBase, segList);
    }

    private int setPathToDestination(ObjectKey trainKey, TrainModel tm) {
	GameTime t = (GameTime) world.get(ITEM.TIME, Player.AUTHORITATIVE);
	Integer timeOfLastAttempt = (Integer) lostTrains.get(trainKey);
	if (timeOfLastAttempt != null) {
	    GameCalendar gc = (GameCalendar) world.get(ITEM.CALENDAR,
		    Player.AUTHORITATIVE);
	    if (timeOfLastAttempt.intValue() + gc.getTicksPerDay() 
		    > t.getTime())
		return tm.getState();
	}


	final Point head = new Point();
	
	Point stationCoords = getCurrentDestination(tm);
	if (stationCoords == null) {
	    /* no orders */
	    ChangeTrainMove ctm = ChangeTrainMove.generateMove(trainKey.index,
		    trainKey.principal, tm, (TrainPath) null, null, t);
	    moveReceiver.processMove(ctm);
	    return TrainModel.STATE_STOPPED;
	}
	tm.getPosition(t).getHead(head);
	TrainPath tp = pathFinder.findPath(stationCoords, head);
	if (tp == null) {
	    // no path to destination
	    return TrainModel.STATE_RUNNABLE;
	}

	TrainPathFunction tpf = buildPathFunction(tp, tm,
		tm.getTrainMotionModel().isOutOfWater());
	ChangeTrainMove ctm;
	if (tpf == null) {
	    // train can't climb hill
	    // train is already lost, so no new move required
	    lostTrains.put(trainKey, new Integer(t.getTime()));
	    return TrainModel.STATE_RUNNABLE;
	} else {
	    ctm = ChangeTrainMove.generateMove(trainKey.index,
		    trainKey.principal, tm, tp, tpf, t);
	    lostTrains.remove(trainKey);
	}
	moveReceiver.processMove(ctm);

	return TrainModel.STATE_RUNNABLE;
    }

    private TrainModelViewer trainModelViewer;

    /** @return the trains destination, in deltas from the origin, or null if
     * there is no current destination */
    private Point getCurrentDestination(TrainModel tm) {
	GameTime now = (GameTime) world.get(ITEM.TIME,
		Player.AUTHORITATIVE);
	TrainOrdersModel tom = tm.getScheduleIterator()
	    .getCurrentOrder(world);
	if (tom == null)
	    return null;

	ObjectKey stationKey = tom.getStationNumber();
	StationModel station = (StationModel) world.get
	    (stationKey.key, stationKey.index, stationKey.principal);
	Point p = new Point(station.getStationX(), station.getStationY());
	return TrackTile.tileCoordsToDeltas(p);
    }

    /** @return true if we changed the trains state */
    private boolean checkWater(ObjectKey trainKey, TrainModel train) {
	trainModelViewer.setTrainModel(train);
	// get current water state

	boolean isOutOfWater = train.getTrainMotionModel().isOutOfWater();

	if (!isOutOfWater && trainModelViewer.getWaterRemaining() == 0) {
	    // send a move to set out of water
	    Point head = new Point();
	    GameTime now = (GameTime) world.get(ITEM.TIME,
		    Player.AUTHORITATIVE);
	    TrainPath trainPos = train.getPosition(now);
	    trainPos.getHead(head);
	    Point dest = getCurrentDestination(train);
	    Move m;
	    if (dest == null) {
		m = ChangeTrainMove.generateOutOfWaterMove(trainKey, world,
			true, null, null);
	    } else {
		TrainPath newPathToDestination =
		    pathFinder.findPath(dest, head);
		TrainPathFunction pathFunction = buildPathFunction
		    (newPathToDestination, train, true);
		if (pathFunction == null) {
		    m = ChangeTrainMove.generateOutOfWaterMove(trainKey,
			    world, true, null, null);
		} else {
		    m = ChangeTrainMove.generateOutOfWaterMove(trainKey, world,
			    true, newPathToDestination, pathFunction);
		}
	    }
	    moveReceiver.processMove(m);
	    return true;
	}
	return false;
    }
}
