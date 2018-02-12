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

import freerails.util.Utils;

import java.io.Serializable;

/**
 * Stores the speed and success of a train immediately after an instant of time.
 */
// TODO only used once, do we really need it
public class SpeedTimeAndStatus implements Serializable {

    private static final long serialVersionUID = 1769385261436134444L;
    private final double dt;
    private final double speed;
    private final double acceleration;
    private final double s;
    private final TrainState activity;

    SpeedTimeAndStatus(double speed, double acceleration, double distance, double time, TrainState activity) {
        if (time < 0) throw new IllegalArgumentException(String.valueOf(time));
        this.acceleration = acceleration;
        this.dt = time;
        this.s = distance;
        this.speed = speed;
        this.activity = Utils.verifyNotNull(activity);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SpeedTimeAndStatus)) return false;
        final SpeedTimeAndStatus other = (SpeedTimeAndStatus) obj;

        if (acceleration != other.acceleration) return false;
        if (dt != other.dt) return false;
        if (s != other.s) return false;
        if (speed != other.speed) return false;
        return activity != null ? activity == other.activity : other.activity == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = dt != +0.0d ? Double.doubleToLongBits(dt) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = speed != +0.0d ? Double.doubleToLongBits(speed) : 0L;
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        temp = acceleration != +0.0d ? Double.doubleToLongBits(acceleration) : 0L;
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        temp = s != +0.0d ? Double.doubleToLongBits(s) : 0L;
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        result = 29 * result + (activity != null ? activity.hashCode() : 0);
        return result;
    }

}
