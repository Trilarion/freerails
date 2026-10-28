/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package freerails.model.train.motion;


import freerails.model.train.activity.Activity;
import freerails.model.train.TrainState;
import freerails.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A motion profile formed by executing component motions in sequence. */
public class CompositeMotion extends Activity<SpeedTimeAndStatus> implements Motion {

    private static final long serialVersionUID = 3146586143114534610L;
    private final List<Motion> motions;
    private final double totalTime, totalDistance;

    /**
     * @param motions component motions in execution order
     * @throws NullPointerException if the array or any component motion is null
     */
    public CompositeMotion(Motion... motions) {
        this.motions = new ArrayList<>(Arrays.asList(motions));
        Utils.verifyNoneNull(this.motions);
        double time = 0, distance = 0;
        for (Motion motion : motions) {
            time += motion.getTotalTime();
            distance += motion.getTotalDistance();
        }
        totalTime = time;
        totalDistance = distance;
    }

    /** Compares the ordered component motions and their aggregate totals. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CompositeMotion)) return false;
        final CompositeMotion other = (CompositeMotion) obj;

        if (totalTime != other.totalTime) return false;
        if (totalDistance != other.totalDistance) return false;
        return motions.equals(other.motions);
    }

    /** Returns a hash code based on the component motions and aggregate totals. */
    @Override
    public int hashCode() {
        int result = motions.hashCode();
        result += 29 * Double.hashCode(totalTime);
        result += 29 * Double.hashCode(totalDistance);
        return result;
    }

    /** Returns the total motion duration as this activity's duration. */
    @Override
    public double getDuration() {
        return totalTime;
    }

    /**
     * {@inheritDoc}
     *
     * @param time elapsed time within this composite motion
     * @return state sample at that time
     * @throws IllegalArgumentException if time is outside the motion duration
     */
    @Override
    public SpeedTimeAndStatus getStateAtTime(final double time) {
        checkTime(time);
        double acceleration;
        double distance;
        double speed;

        SubTime subTime = getIndex(time);
        Motion motion = motions.get(subTime.idx);
        speed = motion.calculateSpeedAtTime(subTime.time);
        acceleration = motion.calculateAccelerationAtTime(subTime.time);
        distance = motion.calculateDistanceAtTime(subTime.time);

        return new SpeedTimeAndStatus(speed, acceleration, distance, time, TrainState.READY);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if time is outside the motion duration
     */
    @Override
    public double calculateDistanceAtTime(double time) {
        checkTime(time);
        if (time == totalTime) return totalDistance;
        SubTime subTime = getIndex(time);
        // first add up all total distances of completed motions
        double distance = 0;
        for (int i = 0; i < subTime.idx; i++) {
            Motion motion = motions.get(i);
            distance += motion.getTotalDistance();
        }
        // then add fractional distance of the actual motion
        Motion motion = motions.get(subTime.idx);
        distance += motion.calculateDistanceAtTime(subTime.time);
        return distance;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if distance is outside the motion distance
     */
    @Override
    public double calculateTimeAtDistance(double distance) {
        if (distance == totalDistance) return totalTime;
        // TODO checkDistance
        if (distance > totalDistance) throw new IllegalArgumentException(String.valueOf(distance));

        double distanceSoFar = 0;
        double timeSoFar = 0;

        // determine how many full completed motions there are
        int idx = 0;
        Motion motion = motions.get(idx);
        while (distanceSoFar + motion.getTotalDistance() < distance) {
            distanceSoFar += motion.getTotalDistance();
            timeSoFar += motion.getTotalTime();
            idx++;
            motion = motions.get(idx);
        }
        // there is an uncompleted motion, get time for it and add
        double sOffset = distance - distanceSoFar;
        if (sOffset >= motion.getTotalDistance()) {
            timeSoFar += motion.getTotalTime();
        } else {
            timeSoFar += motion.calculateTimeAtDistance(sOffset);
        }
        return timeSoFar;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if time is outside the motion duration
     */
    @Override
    public double calculateSpeedAtTime(double time) {
        checkTime(time);
        SubTime subTime = getIndex(time);
        Motion motion = motions.get(subTime.idx);
        return motion.calculateSpeedAtTime(subTime.time);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if time is outside the motion duration
     */
    @Override
    public double calculateAccelerationAtTime(double time) {
        checkTime(time);
        SubTime subTime = getIndex(time);
        Motion motion = motions.get(subTime.idx);
        return motion.calculateAccelerationAtTime(subTime.time);
    }

    /** Returns the sum of the component motion durations. */
    @Override
    public double getTotalTime() {
        return totalTime;
    }

    /** Returns the sum of the component motion distances. */
    @Override
    public double getTotalDistance() {
        return totalDistance;
    }

    /**
     * Locates the component motion and local time corresponding to the given time.
     *
     * @param time elapsed time within the composite motion
     * @return component index and its local elapsed time
     * @throws IllegalArgumentException if time is outside the motion duration
     */
    private SubTime getIndex(double time) {
        checkTime(time);
        double timeSoFar = 0;
        for (int idx = 0; idx < motions.size(); idx++) {
            Motion motion = motions.get(idx);

            if (time <= timeSoFar + motion.getTotalTime()) {
                double offset = time - timeSoFar;
                return new SubTime(idx, offset);
            }
            timeSoFar += motion.getTotalTime();
        }
        // Should never happen since we call checkT() above!
        throw new IllegalStateException(String.valueOf(time));
    }

    /**
     * Ensures a requested time lies within this motion's duration.
     *
     * @param time elapsed time to validate
     * @throws IllegalArgumentException if time is outside the motion duration
     */
    private void checkTime(double time) {
        if (time < 0.0d || time > totalTime) throw new IllegalArgumentException("t=" + time + ", but duration=" + totalTime);
    }

    /** Holds a component motion index and the corresponding local elapsed time. */
    private static class SubTime {

        private final double time;
        private final int idx;

        /**
         * Creates a component-motion time location.
         *
         * @param idx component motion index
         * @param time elapsed time within that component
         */
        private SubTime(int idx, double time) {
            this.idx = idx;
            this.time = time;
        }
    }

}
