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
import java.util.Map.Entry;

import org.railz.controller.*;
import org.railz.move.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.station.*;
import org.railz.world.top.*;
import org.railz.world.train.*;
import org.railz.world.track.*;

/**
 * Responsible for moving the trains.
 *
 * @author Robert Tuck 12-Apr-2004
 */
public final class AuthoritativeTrainMover {
    private World world;
    private TrainPathFinder pathFinder;
    private ArrayList[] trainLists = new ArrayList[TrainModel.PRIORITY_EXPRESS];
    private MoveReceiver moveReceiver;

    public AuthoritativeTrainMover(World w, MoveReceiver mr) {
	world = w;
	pathFinder = new TrainPathFinder(w);
	moveReceiver = mr;
    }

    private void sortTrainsByPriority() {
	for (int i = TrainModel.PRIORITY_SLOW; i <=
		TrainModel.PRIORITY_EXPRESS; i++) {
	    trainLists[i - 1] = new ArrayList();
	}
	NonNullElements i = new NonNullElements(KEY.PLAYERS, world,
		Player.AUTHORITATIVE);
	while (i.next()) {
	    FreerailsPrincipal p = ((Player) i.getElement()).getPrincipal();
	    NonNullElements j = new NonNullElements(KEY.TRAINS, world, p);
	    while (j.next()) {
		TrainModel tm = (TrainModel) j.getElement();
		int priority = tm.getPriority();
		trainLists[priority - 1]
		    .add(new ObjectKey(KEY.TRAINS, p, j.getIndex()));
	    }
	}
    }

    /**
     * Moves all the trains if appropriate
     */
    public void moveTrains() {
	GameTime t = (GameTime) world.get(ITEM.TIME, Player.AUTHORITATIVE);
	GameTime tPlus1 = new GameTime(t.getTime() + 1);
	sortTrainsByPriority();
	for (int i = trainLists.length - 1; i >= 0; i--) {
	    for (int j = trainLists[i].size() - 1; j >= 0; j--) {
		ObjectKey ok = (ObjectKey) trainLists[i].get(j);
		FreerailsPrincipal p = ok.principal;
		TrainModel tm = (TrainModel) world.get(ok.key, ok.index,
			ok.principal);
		/*
		 * If we don't know the trains current position, initialise it
		 */
		if (tm.getPosition(t) == null) {
		    setInitialPosition(tm, p, ok.index, t);
		    tm = (TrainModel) world.get(ok.key, ok.index,
			    ok.principal);
		}

		/*
		 * If the train is runnable, attempt to grab locks for the
		 * train.
		 */
		if (tm.getState() == TrainModel.STATE_RUNNABLE)
		    checkTrainLocks(tm, t, tPlus1, ok);
	    }
	}
	/*
	 * release all locks
	 */
	for (int i = trainLists.length - 1; i >= 0; i--) {
	    for (int j = trainLists[i].size() - 1; j >= 0; j--) {
		ObjectKey ok = (ObjectKey) trainLists[i].get(j);
		TrainModel tm = (TrainModel) world.get(ok.key, ok.index,
			ok.principal);
		if (tm.getTrainMotionModel() != null &&
			tm.getTrainMotionModel().hasLock()) {
		    releaseAllLocks(world, tm.getPosition(tPlus1), tm);
		}
	    }
	}
    }

    private void checkTrainLocks(TrainModel tm, GameTime t, GameTime tPlus1,
	    ObjectKey ok) {
	final PathLength pl = new PathLength();
	TrainPath pos = tm.getPosition(tPlus1);
	if (pos == null)
	    return;
	if (! acquireAllLocks(world, tm, pos)) {
	    if (! tm.isBlocked()) {
		Move m = ChangeTrainMove.generateBlockedMove(ok, tm, t, true);
		moveReceiver.processMove(m);
	    }
	} else {
	    if (tm.isBlocked()) {
		Move m = ChangeTrainMove.generateBlockedMove(ok, tm, t,
			false);
		moveReceiver.processMove(m);
	    }
	}
    }

    private void setInitialPosition(TrainModel tm, FreerailsPrincipal
	    trainPrincipal, int trainIndex, GameTime t) {
	ScheduleIterator si = tm.getScheduleIterator();
	TrainOrdersModel departsOrder = si.getCurrentOrder(world);
	StationModel departsStation = (StationModel) world.get(KEY.STATIONS,
		departsOrder.getStationNumber().index,
		departsOrder.getStationNumber().principal); 
	/* don't update the world model with the new schedule since our state
	 * is STATE_UNLOADING */
	si = si.nextOrder(world);
	// tm = new TrainModel (tm, si = si.nextOrder(world));
	// world.set(KEY.TRAINS, trainIndex, tm, trainPrincipal);
	TrainOrdersModel arrivesOrder = si.getCurrentOrder(world);
	StationModel arrivesStation = (StationModel) world.get(KEY.STATIONS,
		arrivesOrder.getStationNumber().index,
		arrivesOrder.getStationNumber().principal);

	Point departStationCoords = new Point(departsStation.getStationX(),
		    departsStation.getStationY());
	Point arriveStationCoords = new Point(arrivesStation.getStationX(),
		    arrivesStation.getStationY());
	departStationCoords =
	    TrackTile.tileCoordsToDeltas(departStationCoords);
	arriveStationCoords =
	    TrackTile.tileCoordsToDeltas(arriveStationCoords);
	/* XXX arguments are reversed since we want the tail of the TrainPath
	 * to be the head of the train */
	TrainPath pathToNextStation = pathFinder.findPath(arriveStationCoords,
		    departStationCoords);
	if (pathToNextStation == null) {
	    /* Couldn't find a path to the next station */
	    return;
	}
	TrainPath currentPos = new TrainPath(pathToNextStation);
	currentPos.reverse();
	int trainLength = tm.getLength();
	int pathToNextStationLength = pathToNextStation.getLength() -
	    trainLength;
	if (pathToNextStationLength < 0)
	    /* XXX help! what should we do here? */
	    throw new IllegalStateException("Stations are too close to place "
		    + "train");
	currentPos.truncateTail(trainLength);
	Point tail = new Point();
	currentPos.getTail(tail);
	TrainPath pathTraversedSinceLastSync = new TrainPath(new IntLine[] 
		{new IntLine(tail.x, tail.y,
		    tail.x, tail.y) });

	Move m = ChangeTrainMove.generateMove(trainIndex, trainPrincipal,
		currentPos, t, world);
	moveReceiver.processMove(m);
    }

    private static byte directionFromDelta(int dx, int dy) {
	switch (dx) {
	    case 1:
		switch (dy) {
		    case 1:
			return CompassPoints.SOUTHEAST;
		    case 0:
			return CompassPoints.EAST;
		    case -1:
			return CompassPoints.NORTHEAST;
		    default:
			throw new IllegalArgumentException();
		}
	    case 0:
		switch (dy) {
		    case 1:
			return CompassPoints.SOUTH;
		    case -1:
			return CompassPoints.NORTH;
		    default:
			throw new IllegalArgumentException();
		}
	    case -1:
		switch (dy) {
		    case -1:
			return CompassPoints.NORTHWEST;
		    case 0:
			return CompassPoints.WEST;
		    case 1:
			return CompassPoints.SOUTHWEST;
		    default:
			throw new IllegalArgumentException();
		}
	    default:
		throw new IllegalArgumentException();
	}
    }

    public void releaseAllLocks(World world, TrainPath pos, TrainModel tm) {
	HashMap mapCoords = new HashMap();
	pos.getMapCoordsAndDirections(mapCoords);
	Iterator i = mapCoords.entrySet().iterator();
	while (i.hasNext()) {
	    Entry e = (Entry) i.next();
	    world.getTile((Point) e.getKey()).getTrackTile().releaseLock
		(((Byte) e.getValue()).byteValue());
	}
	tm.getTrainMotionModel().setLock(false);
    }
    
    public boolean acquireAllLocks(World w, TrainModel tm, TrainPath pos) {
	HashMap mapCoords = new HashMap();
	pos.getMapCoordsAndDirections(mapCoords);
	final HashMap undoList = new HashMap();
	undoList.clear();
	Iterator i = mapCoords.entrySet().iterator();
	while (i.hasNext()) {
	    Entry e = (Entry) i.next();
	    Point p = (Point) e.getKey();
	    Byte b = (Byte) e.getValue();
	    TrackTile tt = w.getTile(p).getTrackTile();
	    if (tt == null || !tt.getLock(b.byteValue())) {
		/*
		 * XXX TODO if we delete the track from under a train, we
		 * should handle this correctly 
		 */
		i = undoList.entrySet().iterator();
		while (i.hasNext()) {
		    e = (Entry) i.next();
		    tt = w.getTile((Point) e.getKey()).getTrackTile();
		    tt.releaseLock(((Byte) e.getValue()).byteValue());
		}
		return false;
	    }
	    undoList.put(p, b);
	}
	tm.getTrainMotionModel().setLock(true);
	return true;
    }

}

