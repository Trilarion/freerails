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
package freerails.model.train.activity;

import java.io.Serializable;

/**
 * Mostly used for trains.
 *
 * An activity is something whose state
 * may be continually changing. An example is a train - it is an active entity
 * since while it is moving its position is continually changing.
 *
 * @param <E>
 */
public abstract class Activity<E extends Serializable> implements Serializable {

    private double startTime = Double.MIN_VALUE;

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    /**
     * @return
     */
    public abstract double getDuration();

    /**
     * @param time
     * @return
     */
    public abstract E getStateAtTime(double time);

}
