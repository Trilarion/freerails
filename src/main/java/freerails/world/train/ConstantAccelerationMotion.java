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
package freerails.world.train;

import java.io.Serializable;

/**
 *
 */
public strictfp class ConstantAccelerationMotion implements Serializable, Motion {

    /**
     *
     */
    private static final long serialVersionUID = -2180666310811530761L;
    public static final ConstantAccelerationMotion STOPPED = new ConstantAccelerationMotion(0, 0, 0, 0);
    private final double initialSpeed, acceleration, totalDistance, totalTime;

    /**
     * No checks necessary, because this is a private constructor.
     *
     * @param initialSpeed
     * @param acceleration
     * @param totalDistance
     * @param totalTime
     */
    private ConstantAccelerationMotion(double initialSpeed, double acceleration, double totalDistance, double totalTime) {
        this.initialSpeed = initialSpeed;
        this.acceleration = acceleration;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
    }

    /**
     * @param speed
     * @param acceleration
     * @param distance
     * @return
     */
    public static ConstantAccelerationMotion fromSpeedAccelerationDistance(double speed, double acceleration, double distance) {
        double time = calculateTime(speed, acceleration, distance);
        return new ConstantAccelerationMotion(speed, acceleration, distance, time);
    }

    /**
     * @param speed
     * @param acceleration
     * @param time
     * @return
     */
    public static ConstantAccelerationMotion fromSpeedAccelerationTime(double speed, double acceleration, double time) {
        double distance = calculateDistance(speed, acceleration, time);
        return new ConstantAccelerationMotion(speed, acceleration, distance, time);
    }

    /**
     *
     * @param speed
     * @param acceleration
     * @param distance
     * @return
     */
    private static double calculateTime(double speed, double acceleration, double distance) {
        assert(acceleration >= 0);
        assert(speed >= 0);
        assert(distance >= 0);
        // if distance > 0, either speed or acceleration must also be >0

        if (acceleration == 0) {
            return distance / speed;
        }

        // solving the quadratic equation and taking the positive solution
        return (StrictMath.sqrt(speed * speed + 2 * acceleration * distance) - speed) / acceleration;
    }

    private static double calculateDistance(double speed, double acceleration, double time) {
        assert(speed >= 0);
        assert(acceleration >= 0);
        assert(time >= 0);

        return speed * time + acceleration * time * time / 2;
    }

    public double calculateDistanceAtTime(double time) {
        checkValidTime(time);

        if (totalTime == time) {
            return totalDistance;
        }

        double distance = calculateDistance(initialSpeed, acceleration, time);
        return Math.min(this.totalDistance, distance);
    }

    public double calculateTimeAtDistance(double distance) {
        checkValidDistance(distance);

        if (totalDistance == distance) {
            return totalTime;
        }

        double time = calculateTime(initialSpeed, acceleration, distance);
        return Math.min(this.totalTime, time);
    }

    public double calculateSpeedAtTime(double time) {
        checkValidTime(time);
        return initialSpeed + acceleration * time;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ConstantAccelerationMotion)) return false;
        ConstantAccelerationMotion other = (ConstantAccelerationMotion) obj;
        return initialSpeed == other.initialSpeed && acceleration == other.acceleration && totalDistance == other.totalDistance && totalTime == other.totalTime;
    }

    public double calculateAccelerationAtTime(double time) {
        checkValidTime(time);
        return acceleration;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(initialSpeed);
        result = 29 * result + Double.hashCode(acceleration);
        result = 29 * result + Double.hashCode(totalDistance);
        result = 29 * result + Double.hashCode(totalTime);
        return result;
    }

    private void checkValidDistance(double distance) {
        if (distance < 0 || distance > this.totalDistance) {
            throw new IllegalArgumentException(distance + " < 0 || " + distance + " > " + this.totalDistance);
        }
    }

    private void checkValidTime(double time) {
        if (time < 0 || time > this.totalTime) {
            throw new IllegalArgumentException("(" + time + " < 0 || " + time + " > " + this.totalTime + ')');
        }
    }

    @Override
    public String toString() {
        return "ConstantAccelerationMotion [a=" + acceleration + ", u=" + initialSpeed + ", dt=" + totalTime + ']';
    }

}
