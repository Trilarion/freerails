/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
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
package freerails.world.train.motion;

import freerails.util.ImmutableList;
import freerails.world.Activity;
import freerails.world.train.TrainState;

/**
 *
 */
public class CompositeMotion implements Activity<SpeedTimeAndStatus>, Motion {

    private static final long serialVersionUID = 3146586143114534610L;
    private final ImmutableList<Motion> motions;
    private final double totalTime, totalDistance;

    /**
     * @param motions
     */
    public CompositeMotion(Motion... motions) {
        this.motions = new ImmutableList<>(motions);
        this.motions.verifyNoneNull();
        double time = 0, distance = 0;
        for (Motion motion : motions) {
            time += motion.getTotalTime();
            distance += motion.getTotalDistance();
        }
        totalTime = time;
        totalDistance = distance;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CompositeMotion)) return false;
        final CompositeMotion other = (CompositeMotion) obj;

        if (totalTime != other.totalTime) return false;
        if (totalDistance != other.totalDistance) return false;
        return motions.equals(other.motions);
    }

    @Override
    public int hashCode() {
        int result = motions.hashCode();
        result = 29 * Double.hashCode(totalTime);
        result = 29 * Double.hashCode(totalDistance);
        return result;
    }

    /**
     * @return
     */
    public double duration() {
        return totalTime;
    }

    /**
     * @param time
     * @return
     */
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

    public double calculateTimeAtDistance(double distance) {
        if (distance == totalDistance) return totalTime;
        // TODO checkDistance
        if (distance > totalDistance) throw new IllegalArgumentException(String.valueOf(distance));

        double distanceSoFar = 0;
        double timeSoFar = 0;

        // determine how many full completed motions there are
        int idx = 0;
        Motion motion = motions.get(idx);
        while ((distanceSoFar + motion.getTotalDistance()) < distance) {
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

    public double calculateSpeedAtTime(double time) {
        checkTime(time);
        SubTime subTime = getIndex(time);
        Motion motion = motions.get(subTime.idx);
        return motion.calculateSpeedAtTime(subTime.time);
    }

    public double calculateAccelerationAtTime(double time) {
        checkTime(time);
        SubTime subTime = getIndex(time);
        Motion motion = motions.get(subTime.idx);
        return motion.calculateAccelerationAtTime(subTime.time);
    }

    public double getTotalTime() {
        return totalTime;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    /**
     *
     * @param time
     * @return
     */
    private SubTime getIndex(double time) {
        checkTime(time);
        double timeSoFar = 0;
        for (int idx = 0; idx < motions.size(); idx++) {
            Motion motion = motions.get(idx);

            if (time <= (timeSoFar + motion.getTotalTime())) {
                double offset = time - timeSoFar;
                return new SubTime(idx, offset);
            }
            timeSoFar += motion.getTotalTime();
        }
        // Should never happen since we call checkT() above!
        throw new IllegalStateException(String.valueOf(time));
    }

    private void checkTime(double time) {
        if (time < 0.0d || time > totalTime) throw new IllegalArgumentException("t=" + time + ", but duration=" + totalTime);
    }

    /**
     * Used to enable 2 values to be returned from the method getIndex(double t)
     */
    private static class SubTime {

        private final double time;
        private final int idx;

        private SubTime(int idx, double time) {
            this.idx = idx;
            this.time = time;
        }
    }

}
