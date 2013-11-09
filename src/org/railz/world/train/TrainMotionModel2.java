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

import java.io.IOException;
import java.io.ObjectInputStream;

import org.railz.world.common.FreerailsSerializable;
import org.railz.world.common.GameTime;
import org.railz.world.common.PathLength;

/**
 * This class encapsulates portion of a trains state required for the prediction
 * of its position at future points in time.
 * 
 * @author rtuck99@users.berlios.de
 */
public final class TrainMotionModel2 implements FreerailsSerializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 851040565807153271L;

	/** whether or not we are out of water */
	private final boolean outOfWater;

	/**
	 * The value of t0 in Ticks
	 */
	private final int t0;

	/**
	 * Describes the position of the train at time t0. The head of the path
	 * coincides with the head of the train. The tail of the path coincides with
	 * the end of the train.
	 */
	TrainPath trainPath;

	/**
	 * Describes the current planned path to this trains destination. The head
	 * of this path corresponds to the next scheduled stop of this train. The
	 * tail of this path corrsponds to the head of the train at time t0.
	 */
	private final TrainPath pathToDestination;

	/**
	 * Describes the distance along the pathToDestination as a function of time
	 */
	private final TrainPathFunction pathFunction;

	/**
	 * True if this train holds locks for all tracks it is on TODO mark as
	 * transient
	 */
	private boolean hasLock = false;

	public TrainMotionModel2(TrainMotionModel2 tmm) {
		this(tmm.pathToDestination, tmm.trainPath, tmm.t0, tmm.hasLock,
				tmm.outOfWater, tmm.pathFunction);
	}

	public TrainMotionModel2 clearPathToDestination(GameTime now) {
		return new TrainMotionModel2(null, getPosition(now), now.getTime(),
				hasLock, outOfWater, null);
	}

	public TrainMotionModel2(TrainMotionModel2 tmm,
			TrainPath pathToDestination, TrainPathFunction pathFunction,
			GameTime t0) {
		this(pathToDestination, tmm.getPosition(t0), t0.getTime(), tmm.hasLock,
				tmm.outOfWater, pathFunction);
	}

	public TrainMotionModel2(TrainPath pathToDestination, TrainPath posAtT0,
			GameTime t0, TrainPathFunction pathFunction) {
		this(pathToDestination, posAtT0, t0.getTime(), false, false,
				pathFunction);
	}

	private TrainMotionModel2(TrainPath pathToDestination, TrainPath posAtT0,
			int t0, boolean hasLock, boolean outOfWater,
			TrainPathFunction pathFunction) {
		this.t0 = t0;
		this.pathToDestination = pathToDestination == null ? null
				: new TrainPath(pathToDestination);
		this.trainPath = new TrainPath(posAtT0);
		this.hasLock = hasLock;
		this.outOfWater = outOfWater;
		this.pathFunction = pathFunction == null ? null
				: new TrainPathFunction(pathFunction);
	}

	/**
	 * Calculate the position of the train along the planned path using a
	 * function of distance with time.
	 * 
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
		pl.setLength(pathToDestination.getActualLength());
		int distanceToTarget = (int) (pl.getLength() - pathFunction
				.getDistance(t.getTime()));

		if (distanceToTarget < 0)
			distanceToTarget = 0;

		return distanceToTarget;
	}

	public boolean reachedDestination(GameTime t) {
		return !isLost() && distanceToDestination(t) == 0;
	}

	@Override
	public String toString() {
		return "TrainMotionModel2: t0=" + t0 + ", tp=" + trainPath + ", p2d="
				+ pathToDestination + ", hasLock=" + hasLock + ", outOfWater="
				+ outOfWater;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof TrainMotionModel2))
			return false;
		TrainMotionModel2 tmm = (TrainMotionModel2) o;
		return t0 == tmm.t0
				&&
				// hasLock == tmm.hasLock &&
				(trainPath == null ? tmm.trainPath == null : trainPath
						.equals(tmm.trainPath))
				&& (pathToDestination == null ? tmm.pathToDestination == null
						: pathToDestination.equals(tmm.pathToDestination))
				&& outOfWater == tmm.outOfWater;
	}

	@Override
	public int hashCode() {
		return t0;
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		hasLock = false;
	}

	/** @return the path 'cost' expended by time t from t0 */
	int getCostTraversed(GameTime t) {
		return isLost() ? 0 : t.getTime() - t0;
	}

	public TrainMotionModel2 setOutOfWater(boolean outOfWater, GameTime t0,
			TrainPath pathToDestination, TrainPathFunction pathFunction) {
		TrainPath newTP = getPosition(t0);
		TrainMotionModel2 tmm = new TrainMotionModel2(pathToDestination, newTP,
				t0.getTime(), hasLock, outOfWater, pathFunction);

		return tmm;
	}

	public boolean isOutOfWater() {
		return outOfWater;
	}

	public TrainPathFunction getPathFunction() {
		return pathFunction;
	}

	/**
	 * @return a new TrainMotionModel2 where the train is the specified length
	 */
	public TrainMotionModel2 setTrainPathLength(int length) {
		TrainPath newTrainPath = new TrainPath(trainPath);
		TrainMotionModel2 tmm;
		if (length > trainPath.getLength()) {
			// construct a path from the tail of the train to its destination,
			// which we can use to extend the trains path at t0
			TrainPath extension;
			if (pathToDestination != null) {
				extension = new TrainPath(pathToDestination);
				extension.append(new TrainPath(trainPath));
			} else {
				extension = new TrainPath(trainPath);
			}

			do {
				extension.reverse();
				newTrainPath.append(extension);
			} while (newTrainPath.getLength() < length);
		}

		newTrainPath.truncateTail(length);
		tmm = new TrainMotionModel2(this);
		tmm.trainPath = newTrainPath;
		return tmm;
	}
}
