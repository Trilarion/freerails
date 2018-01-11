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
 * Stores the speed and status of a train immediately after an instant of time.
 */
public class SpeedTimeAndStatus implements Serializable {

    private static final long serialVersionUID = 1769385261436134444L;
    /**
     *
     */
    public final double dt;

    /**
     *
     */
    public final double speed;

    /**
     *
     */
    public final double acceleration;

    /**
     *
     */
    public final double s;
    private final TrainActivity activity;

    SpeedTimeAndStatus(double acceleration, TrainActivity activity, double dt, double s, double speed) {
        if (dt < 0) throw new IllegalArgumentException(String.valueOf(dt));
        this.acceleration = acceleration;
        this.activity = activity;
        this.dt = dt;
        this.s = s;
        this.speed = speed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SpeedTimeAndStatus)) return false;

        final SpeedTimeAndStatus speedTimeAndStatus = (SpeedTimeAndStatus) obj;

        if (acceleration != speedTimeAndStatus.acceleration) return false;
        if (dt != speedTimeAndStatus.dt) return false;
        if (s != speedTimeAndStatus.s) return false;
        if (speed != speedTimeAndStatus.speed) return false;
        return activity != null ? activity == speedTimeAndStatus.activity : speedTimeAndStatus.activity == null;
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
