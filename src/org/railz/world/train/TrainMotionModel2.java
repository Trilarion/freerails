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

import java.awt.Point;
import java.io.*;
import java.util.HashMap;

import org.railz.world.common.*;
import org.railz.world.track.*;
/**
 * This class encapsulates portion of a trains state required for the
 * prediction of its position at future points in time.
 * @author rtuck99@users.berlios.de
 */
public final class TrainMotionModel2 implements FreerailsSerializable {
    /**
     * The value of t0 in Ticks
     */
    private int t0;

    /**
     * Describes the position of the train at time t0. The head of the path
     * coincides with the head of the train. The tail of the path coincides
     * with the end of the train.
     */
    TrainPath trainPath;

    /**
     * This is the trains speed in deltas per Tick. Initially the model will
     * assume constant speed over the projected course, however at a later
     * point in time we will substitute for some function of the traversed
     * distance.
     */
    private float speed;

    /**
     * Describes the current planned path to this trains destination. The head
     * of this path corresponds to the next scheduled stop of this train. The
     * tail of this path corrsponds to the head of the train at time t0.
     */
    private TrainPath pathToDestination;

    /**
     * True if this train holds locks for all tracks it is on
     * TODO mark as transient
     */
    private boolean hasLock = false;
    
    public TrainMotionModel2(TrainMotionModel2 tmm) {
	this(tmm.pathToDestination, tmm.trainPath, tmm.t0, tmm.speed,
		tmm.hasLock);
    }

    public TrainMotionModel2 clearPathToDestination(GameTime now) {
	return new TrainMotionModel2(null, getPosition(now), now.getTime(),
	       	speed, hasLock);
    }

    public TrainMotionModel2(TrainMotionModel2 tmm, TrainPath
	    pathToDestination, GameTime t0) {
	this(pathToDestination, tmm.getPosition(t0), t0.getTime(), tmm.speed,
		tmm.hasLock);
    }

    /**
     * @param maxSpeed speed pf the train in tiles per BigTick
     */
    public TrainMotionModel2(TrainPath pathToDestination, TrainPath posAtT0,
	    GameTime t0, int maxSpeed) {
	this(pathToDestination, posAtT0, t0.getTime(),
	       	((float) (maxSpeed / EngineType.TILE_HOURS_PER_MILE_BIGTICKS)) *
	       	TrackTile.DELTAS_PER_TILE / GameTime.TICKS_PER_BIG_TICK, false);
    }

    private TrainMotionModel2(TrainPath pathToDestination, TrainPath posAtT0,
	    int t0, float speed, boolean hasLock) {
	this.t0 = t0;
	this.pathToDestination = pathToDestination == null ? null : 
	    new TrainPath(pathToDestination);
	this.speed =  speed;
	this.trainPath = new TrainPath(posAtT0);
	this.hasLock = hasLock;
    }

    /**
     * Calculate the position of the train from the planned path using a
     * constant-speed formula.
     * @return The position of the train as predicted by this model at time t1
     */
    TrainPath getPosition(GameTime t1) {
	if (pathToDestination == null) {
	    /* don't know where we're going, return original pos */
	    return trainPath;
	}

	int distanceToTarget = distanceToDestination(t1);

	/* can happen due to rounding */
	if (distanceToTarget > pathToDestination.getLength()) {
	    return new TrainPath(trainPath);
	}

	TrainPath tp = new TrainPath(pathToDestination);
	tp = tp.truncateTail(distanceToTarget);
	TrainPath pos = new TrainPath(trainPath);
	pos.moveHeadTo(tp);
	return pos;
    }

    /**
     * @return true if the train could not find track while traversing its path
     */
    public boolean isLost() {
	return pathToDestination == null || trainPath == null;
    }

    public boolean hasLock() {
	return hasLock;
    }

    public void setLock(boolean b) {
	hasLock = b;
    }

    private int distanceToDestination(GameTime t) {
	assert pathToDestination != null;
	if (t.getTime() < t0) {
	    /* time is going backwards ! */
	    throw new IllegalArgumentException();
	}

	final PathLength pl = new PathLength();
	/* work out where we should be */
	int ticksSinceLastSync = t.getTime() - t0;

	pl.setLength(pathToDestination.getActualLength());
	int distanceToTarget = (int) (pl.getLength() - speed *
	    ticksSinceLastSync);
	if (distanceToTarget < 0)
	    distanceToTarget = 0;

	return distanceToTarget;
    }

    public boolean reachedDestination(GameTime t) {
	return !isLost() && distanceToDestination(t) == 0;
    }

    public String toString() {
	return "TrainMotionModel2: t0=" + t0 + ", tp=" + trainPath + ", speed="
	   + speed + ", p2d=" + pathToDestination + ", hasLock=" + hasLock; 
    }

    public boolean equals(Object o) {
	if (o == null || !(o instanceof TrainMotionModel2))
	    return false;
	TrainMotionModel2 tmm = (TrainMotionModel2) o;
	return t0 == tmm.t0 &&
	    speed == tmm.speed &&
	    //hasLock == tmm.hasLock &&
	    (trainPath == null ? tmm.trainPath == null :
	    trainPath.equals(tmm.trainPath)) &&
	    (pathToDestination == null ? tmm.pathToDestination == null :
	    pathToDestination.equals(tmm.pathToDestination));
    }

    public int hashCode() {
	return t0;
    }

    private void readObject(ObjectInputStream in) throws IOException,
    ClassNotFoundException {
	in.defaultReadObject();
	hasLock = false;
    }
}
