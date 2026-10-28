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
package freerails.model.train.activity;

import java.io.Serializable;

/**
 * A time-bounded entity activity whose serializable state can be queried as it
 * progresses, such as a train's motion.
 *
 * @param <E> serializable state value exposed by this activity
 */
public abstract class Activity<E extends Serializable> implements Serializable {

    private double startTime = Double.MIN_VALUE;

    /**
     * Creates an activity with its default start time.
     */
    public Activity() {
    }

    /**
     * Returns the activity's start time.
     *
     * @return activity start time in game ticks
     */
    public double getStartTime() {
        return startTime;
    }

    /**
     * Sets the activity's start time.
     *
     * @param startTime start time in game ticks
     */
    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the activity's duration in game ticks.
     *
     * @return activity duration
     */
    public abstract double getDuration();

    /**
     * Returns the activity state at the specified time relative to its start.
     *
     * @param time elapsed time in game ticks
     * @return state at that time
     */
    public abstract E getStateAtTime(double time);

}
