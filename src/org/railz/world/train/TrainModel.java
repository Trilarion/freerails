/*
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.io.*;

import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.track.*;

public class TrainModel implements FreerailsSerializable {
    /** Path length traversed since we last loaded water, as of the last
     * resync */
    private int costTraversedSinceLoadingWater;

    /**
     * The time at which the state of the train was last changed. Used for
     * determining delays at stations when loading/unloading etc.
     */
    private GameTime stateLastChanged;

    /**
     * The train is in transit between stops. (but may be blocked by other
     * trains on the track.
     */
    public static final int STATE_RUNNABLE = 0;
    /**
     * The train is stopped by the user.
     */
    public static final int STATE_STOPPED = 1;
    /**
     * The train is loading cargo at a station.
     */
    public static final int STATE_LOADING = 2;
    /**
     * The train is unloading cargo at a station
     */
    public static final int STATE_UNLOADING = 3;

    public static final int PRIORITY_SLOW = 1;
    public static final int PRIORITY_NORMAL = 2;
    public static final int PRIORITY_EXPRESS = 3;

    public static final int MAX_NUMBER_OF_WAGONS = 10;
    private ScheduleIterator scheduleIterator;

    private TrainMotionModel2 trainMotionModel;

    private int engineType = 0;
    private int[] wagonTypes;
    private int cargoBundleNumber;
    private GameTime creationDate;

    /**
     * The number of ticks this train has been in the RUNNABLE state,
     * at the time of the last state change
     */
    private long ticksInService;

    /**
     * Whether this train is blocked or not
     */
    private boolean isBlocked;

    private int state;
    private int priority;

    public String toString() {
	String s = "";
	for (int i = 0; i < wagonTypes.length; i++)
	    s += wagonTypes[i] + ", ";

	return "TrainModel " + super.toString() +
	    ": stateLastChanged = " + stateLastChanged + 
	    ", scheduleIterator = " + scheduleIterator +
	    ", state = " + state + ", isBlocked=" + isBlocked +
	    ", TMM: " + trainMotionModel +
	    ", engineType=" + engineType +
	    ", wagons=" + s + 
	    ", cargoBundleNo=" + cargoBundleNumber + 
	    ", priority=" + priority +
	    ", ticksInService=" + ticksInService +
	    ", ptslw=" + costTraversedSinceLoadingWater;
    }

    /**
     * Constructor for a new train.
     * @param engine type of the engine
     * @param wagons array of indexes into the WAGON_TYPES table
     * @param bundleId index into the CARGO_BUNDLES table
     * @param creationDate time the train was created
     */
    public TrainModel(int engine, int[] wagons, int bundleId,
	    GameTime creationDate) {
	this(engine, wagons, bundleId, creationDate,
		STATE_UNLOADING, null, null, PRIORITY_NORMAL, false,
		creationDate, 0, 0);
    }

    /**
     * Copy constructor but with a new route to the destination
     */
    public TrainModel(TrainModel trainModel, TrainPath pathToDestination,
	    TrainPathFunction pathFunction, GameTime now) {
	this(trainModel.engineType, trainModel.wagonTypes,
		trainModel.cargoBundleNumber,
		trainModel.creationDate, trainModel.state,
		trainModel.scheduleIterator, null,
		trainModel.priority, trainModel.isBlocked,
		trainModel.stateLastChanged, trainModel.ticksInService,
		trainModel.costTraversedSinceLoadingWater);
	if (trainMotionModel != null)
	    costTraversedSinceLoadingWater +=
		trainMotionModel.getCostTraversed(now);

	TrainMotionModel2 tmm = trainModel.trainMotionModel == null ? null :
	    new TrainMotionModel2(trainModel.trainMotionModel,
		    pathToDestination, pathFunction, now);
	trainMotionModel = tmm;
    }

    /**
     * Copy constructor with a new schedule
     */
    public TrainModel (TrainModel tm, ScheduleIterator si, GameTime t) {
	this(tm.engineType, tm.wagonTypes, tm.cargoBundleNumber,
		tm.creationDate, tm.state, si,
		tm.trainMotionModel == null ? null :
		tm.trainMotionModel.clearPathToDestination(t),
		tm.priority, tm.isBlocked, tm.stateLastChanged,
		tm.ticksInService, tm.getCostTraversedSinceLoadingWater(t));
    }

    /**
     * Copy constructor
     */
    public TrainModel(TrainModel tm) {
	this(tm.engineType, tm.wagonTypes, tm.cargoBundleNumber,
		tm.creationDate, tm.state, tm.scheduleIterator,
		tm.trainMotionModel, tm.priority, tm.isBlocked,
		tm.stateLastChanged, tm.ticksInService,
		tm.costTraversedSinceLoadingWater);
    }

    /**
     * @return a new TrainModel with the new priority
     */
    public TrainModel setPriority(int priority) {
	return new TrainModel(engineType, wagonTypes,
		cargoBundleNumber, creationDate, state, scheduleIterator,
		trainMotionModel, priority, isBlocked, stateLastChanged,
		ticksInService, costTraversedSinceLoadingWater);
    }

    public int getPriority() {
	return priority;
    }

    /**
     * copy constructor with new state
     */
    public TrainModel(TrainModel tm, GameTime now, int state) {
	this(tm.engineType, tm.wagonTypes, tm.cargoBundleNumber,
		tm.creationDate, state, tm.scheduleIterator,
		(tm.trainMotionModel == null ? null :
	       	tm.trainMotionModel.clearPathToDestination(now)), tm.priority,
		false, now,
	       	tm.state == STATE_RUNNABLE ? tm.ticksInService + now.getTime() -
		tm.stateLastChanged.getTime() : tm.ticksInService,
		tm.getCostTraversedSinceLoadingWater(now));
    }

    /**
     * copy constructor with original schedule, cargo, position, but new
     * engine and wagons
     */
    public TrainModel getNewInstance(int newEngine, int[] newWagons) {
	TrainModel tm = new TrainModel(newEngine, newWagons,
		this.getCargoBundleNumber(), creationDate, state,
		scheduleIterator, trainMotionModel, priority, isBlocked,
		stateLastChanged, ticksInService,
		costTraversedSinceLoadingWater);
	tm.trainMotionModel =
	    tm.trainMotionModel.setTrainPathLength(tm.getLength());
	return tm;
    }

    /**
     * @return the date at which the engine was created
     */
    public GameTime getCreationDate() {
	return creationDate;
    }

    private TrainModel(int engine, int[] wagons,
	    int bundleId, GameTime creationDate,
	    int state, ScheduleIterator
	    scheduleIterator, TrainMotionModel2 motionModel, int priority,
	    boolean isBlocked, GameTime stateLastChanged, long ticksInService,
	    int costTraversedSinceLoadingWater) {
	engineType = engine;
	wagonTypes = wagons;
	cargoBundleNumber = bundleId;
	this.creationDate = creationDate;
	this.state = state;
	if (scheduleIterator != null)
	    this.scheduleIterator = new ScheduleIterator(scheduleIterator);
	this.priority = priority;
	this.isBlocked = isBlocked;
	trainMotionModel = motionModel == null ? null : new
	    TrainMotionModel2(motionModel);
	this.stateLastChanged = stateLastChanged;
	this.ticksInService = ticksInService;
	this.costTraversedSinceLoadingWater = costTraversedSinceLoadingWater;
    }

    /**
     * @return train length in Deltas
     */
    public int getLength() {
       	//Engine + wagons.
        return (1 + wagonTypes.length) * TrackTile.DELTAS_PER_TILE;
    }

    public boolean canAddWagon() {
        return wagonTypes.length < MAX_NUMBER_OF_WAGONS;
    }

    public int getNumberOfWagons() {
        return wagonTypes.length;
    }

    /**
     * @return Index into WAGON_TYPES table of the ith wagon in the train
     */
    public int getWagon(int i) {
        return wagonTypes[i];
    }

    /**
     * @return an index into the ENGINE_TYPES database
     */
    public int getEngineType() {
        return engineType;
    }

    public int getCargoBundleNumber() {
        return cargoBundleNumber;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TrainModel) {
            TrainModel test = (TrainModel)obj;
            boolean b = this.cargoBundleNumber == test.cargoBundleNumber &&
                this.engineType == test.engineType &&
                Arrays.equals(this.wagonTypes, test.wagonTypes) &&
		(scheduleIterator != null ?
		this.scheduleIterator.equals(test.scheduleIterator) :
		test.scheduleIterator == null) &&
		(trainMotionModel == null ? test.trainMotionModel == null :
		trainMotionModel.equals(test.trainMotionModel)) &&
		(stateLastChanged == null ? test.stateLastChanged == null :
		stateLastChanged.equals(test.stateLastChanged)) &&
		state == test.state &&
		isBlocked == test.isBlocked &&
		priority == test.priority &&
		ticksInService == test.ticksInService &&
		costTraversedSinceLoadingWater ==
		test.costTraversedSinceLoadingWater;

	    if (b == false) {
	    }
            return b;
        } else {
            return false;
        }
    }

    public int getState() {
	return state;
    }

    public GameTime getStateLastChangedTime() {
	return stateLastChanged;
    }

    public ScheduleIterator getScheduleIterator() {
	return scheduleIterator;
    }

    public TrainMotionModel2 getTrainMotionModel() {
	return trainMotionModel;
    }

    public boolean isBlocked() {
	return isBlocked;
    }

    public TrainModel setBlocked(boolean blocked, GameTime now) {
	return new TrainModel(engineType, wagonTypes, cargoBundleNumber,
		creationDate, state, scheduleIterator,
	       trainMotionModel == null ? null :
	       trainMotionModel.clearPathToDestination(now),
       	       priority, blocked, stateLastChanged, ticksInService,
	       getCostTraversedSinceLoadingWater(now));
    }
	    
    public TrainPath getPosition(GameTime t) {
	return trainMotionModel == null ? null :
	    trainMotionModel.getPosition(t);
    }

    public TrainModel setPosition(TrainPath position, GameTime t) {
	return new TrainModel(engineType, wagonTypes, cargoBundleNumber,
		creationDate, state, scheduleIterator, new
		TrainMotionModel2(null, position, t, null), priority,
		isBlocked, stateLastChanged, ticksInService,
		costTraversedSinceLoadingWater);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
	out.writeObject(stateLastChanged);
	out.writeObject(scheduleIterator);
	out.writeUnshared(trainMotionModel);
	out.writeInt(engineType);
	out.writeObject(wagonTypes);
	out.writeInt(cargoBundleNumber);
	out.writeObject(creationDate);
	out.writeBoolean(isBlocked);
	out.writeInt(state);
	out.writeInt(priority);
	out.writeLong(ticksInService);
	out.writeInt(costTraversedSinceLoadingWater);
	out.flush();
    }

    private void readObject(ObjectInputStream in) throws IOException,
    ClassNotFoundException {
	stateLastChanged = (GameTime) in.readObject();
	scheduleIterator = (ScheduleIterator) in.readObject();
	trainMotionModel = (TrainMotionModel2) in.readUnshared();
	engineType = in.readInt();
	wagonTypes = (int[]) in.readObject();
	cargoBundleNumber = in.readInt();
	creationDate = (GameTime) in.readObject();
	isBlocked = in.readBoolean();
	state = in.readInt();
	priority = in.readInt();
	ticksInService = in.readLong();
	costTraversedSinceLoadingWater = in.readInt();
    }

    public long getTicksInService() {
	return ticksInService;
    }

    public TrainModel resetTicksInService() {
	return new TrainModel(engineType, wagonTypes, cargoBundleNumber,
		creationDate, state, scheduleIterator, trainMotionModel,
		priority, isBlocked, stateLastChanged, 0,
		costTraversedSinceLoadingWater);
    }

    /** @return the total path cost expended since loading water */
    int getCostTraversedSinceLoadingWater(GameTime now) {
	if (trainMotionModel == null)
	    return costTraversedSinceLoadingWater;

	return costTraversedSinceLoadingWater +
	    trainMotionModel.getCostTraversed(now);
    }

    /** Change the full/empty state of the trains water */
    public TrainModel loadWater(GameTime t0, boolean empty, TrainPath
	    pathToDestination, TrainPathFunction pathFunction) {
	TrainMotionModel2 tmm = trainMotionModel.setOutOfWater(empty, t0,
		pathToDestination, pathFunction);
	return new TrainModel(engineType, wagonTypes, cargoBundleNumber,
		creationDate, state, scheduleIterator, tmm,
		priority, isBlocked, stateLastChanged, ticksInService,
		empty ? costTraversedSinceLoadingWater : 0);
    }
}
