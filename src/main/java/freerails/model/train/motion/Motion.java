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
 * Serializable motion profile relating elapsed time to distance, speed, and
 * acceleration.
 */
public interface Motion extends Serializable {

    /**
     * @return total duration of this motion
     */
    double getTotalTime();

    /**
     * @return total distance traveled
     */
    double getTotalDistance();

    /**
     * Returns the distance travelled at time t. The returned value, s,
     * satisfies the following conditions:
     * <ol>
     * <li>s &ge; 0</li>
     * <li>s &le; getDistance()</li>
     * <li>s = 0 if t = 0 </li>
     * <li>s = getDistance() if t = getCurrentTime()</li>
     * </ol>
     *
     * @param time elapsed time within this motion
     * @return distance traveled by that time
     * @throws IllegalArgumentException if t &lt; 0 or t &gt; getCurrentTime()
     */
    double calculateDistanceAtTime(double time);

    /**
     * Returns the time taken to travel distance s. The returned value, t,
     * satisfies the following conditions:
     * <ol>
     * <li>t &ge; 0</li>
     * <li>t &le; getCurrentTime()</li>
     * <li>t = 0 if s = 0 </li>
     * <li>t = getCurrentTime() if s = getDistance()</li>
     * </ol>
     *
     * @param distance distance from the beginning of this motion
     * @return elapsed time at which that distance is reached
     * @throws IllegalArgumentException if s &lt; 0 or s &gt; getDistance()
     */
    double calculateTimeAtDistance(double distance);

    /**
     * Returns the instantaneous speed at the specified time.
     *
     * @param time elapsed time within this motion
     * @return speed at that time
     * @throws IllegalArgumentException if time is outside this motion's duration
     */
    double calculateSpeedAtTime(double time);

    /**
     * Returns the instantaneous acceleration at the specified time.
     *
     * @param time elapsed time within this motion
     * @return acceleration at that time
     * @throws IllegalArgumentException if time is outside this motion's duration
     */
    double calculateAccelerationAtTime(double time);
}