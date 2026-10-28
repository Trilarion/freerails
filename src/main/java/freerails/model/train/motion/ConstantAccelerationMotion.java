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

import java.io.Serializable;

/**
 * Motion profile with constant nonnegative initial speed and acceleration over
 * a fixed time or distance interval.
 */
public class ConstantAccelerationMotion implements Serializable, Motion {

    private static final long serialVersionUID = -2180666310811530761L;
    /** Zero-distance motion for a stationary train. */
    public static final ConstantAccelerationMotion STOPPED = new ConstantAccelerationMotion(0, 0, 0, 0);
    private final double initialSpeed, acceleration, totalDistance, totalTime;

    /**
     * No checks necessary, because this is a private constructor.
     *
     * @param initialSpeed speed at the start
     * @param acceleration constant acceleration
     * @param totalDistance distance covered
     * @param totalTime duration
     */
    private ConstantAccelerationMotion(double initialSpeed, double acceleration, double totalDistance, double totalTime) {
        this.initialSpeed = initialSpeed;
        this.acceleration = acceleration;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
    }

    /**
     * Creates a motion with a specified distance and derives its duration.
     *
     * @param speed initial speed
     * @param acceleration constant acceleration
     * @param distance distance covered
     * @return motion ending after the time required to cover the distance
     */
    public static ConstantAccelerationMotion fromSpeedAccelerationDistance(double speed, double acceleration, double distance) {
        double time = calculateTime(speed, acceleration, distance);
        return new ConstantAccelerationMotion(speed, acceleration, distance, time);
    }

    /**
     * Creates a motion with a specified duration and derives its distance.
     *
     * @param speed initial speed
     * @param acceleration constant acceleration
     * @param time duration
     * @return motion covering the distance traveled during that duration
     */
    public static ConstantAccelerationMotion fromSpeedAccelerationTime(double speed, double acceleration, double time) {
        double distance = calculateDistance(speed, acceleration, time);
        return new ConstantAccelerationMotion(speed, acceleration, distance, time);
    }

    /**
     * Solves the constant-acceleration distance equation for elapsed time.
     *
     * @param speed initial speed
     * @param acceleration constant acceleration
     * @param distance traveled distance
     * @return elapsed time required to travel the distance
     */
    private static double calculateTime(double speed, double acceleration, double distance) {
        assert acceleration >= 0;
        assert speed >= 0;
        assert distance >= 0;
        // if distance > 0, either speed or acceleration must also be >0

        if (acceleration == 0) {
            return distance / speed;
        }

        // solving the quadratic equation and taking the positive solution
        return (StrictMath.sqrt(speed * speed + 2 * acceleration * distance) - speed) / acceleration;
    }

    /**
     * Computes the distance traveled under constant acceleration.
     *
     * @param speed initial speed
     * @param acceleration constant acceleration
     * @param time elapsed time
     * @return distance traveled
     */
    private static double calculateDistance(double speed, double acceleration, double time) {
        assert speed >= 0;
        assert acceleration >= 0;
        assert time >= 0;

        return speed * time + acceleration * time * time / 2;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if time is outside this motion's duration
     */
    @Override
    public double calculateDistanceAtTime(double time) {
        checkValidTime(time);

        if (totalTime == time) {
            return totalDistance;
        }

        double distance = calculateDistance(initialSpeed, acceleration, time);
        return Math.min(totalDistance, distance);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if distance is outside this motion's distance
     */
    @Override
    public double calculateTimeAtDistance(double distance) {
        checkValidDistance(distance);

        if (totalDistance == distance) {
            return totalTime;
        }

        double time = calculateTime(initialSpeed, acceleration, distance);
        return Math.min(totalTime, time);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if time is outside this motion's duration
     */
    @Override
    public double calculateSpeedAtTime(double time) {
        checkValidTime(time);
        return initialSpeed + acceleration * time;
    }

    /** Compares the initial speed, acceleration, total distance, and duration. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ConstantAccelerationMotion)) return false;
        ConstantAccelerationMotion other = (ConstantAccelerationMotion) obj;
        return initialSpeed == other.initialSpeed && acceleration == other.acceleration && totalDistance == other.totalDistance && totalTime == other.totalTime;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if time is outside this motion's duration
     */
    @Override
    public double calculateAccelerationAtTime(double time) {
        checkValidTime(time);
        return acceleration;
    }

    /** Returns the duration of this motion. */
    @Override
    public double getTotalTime() {
        return totalTime;
    }

    /** Returns the distance covered by this motion. */
    @Override
    public double getTotalDistance() {
        return totalDistance;
    }

    /** Returns a hash code consistent with {@link #equals(Object)}. */
    @Override
    public int hashCode() {
        int result = Double.hashCode(initialSpeed);
        result = 29 * result + Double.hashCode(acceleration);
        result = 29 * result + Double.hashCode(totalDistance);
        result = 29 * result + Double.hashCode(totalTime);
        return result;
    }

    /**
     * Validates a distance queried against this motion.
     *
     * @param distance distance to validate
     * @throws IllegalArgumentException if distance is negative or exceeds the total distance
     */
    private void checkValidDistance(double distance) {
        if (distance < 0 || distance > totalDistance) {
            throw new IllegalArgumentException(distance + " < 0 || " + distance + " > " + totalDistance);
        }
    }

    /**
     * Validates a time queried against this motion.
     *
     * @param time elapsed time to validate
     * @throws IllegalArgumentException if time is negative or exceeds the total duration
     */
    private void checkValidTime(double time) {
        if (time < 0 || time > totalTime) {
            throw new IllegalArgumentException("(" + time + " < 0 || " + time + " > " + totalTime + ')');
        }
    }

    /** Returns a textual summary of this constant-acceleration profile. */
    @Override
    public String toString() {
        return "ConstantAccelerationMotion [a=" + acceleration + ", u=" + initialSpeed + ", dt=" + totalTime + ']';
    }

}
