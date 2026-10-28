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

import freerails.util.Utils;
import freerails.model.train.TrainState;

import java.io.Serial;
import java.io.Serializable;

/**
 * Stores a train's speed, acceleration, traveled distance, elapsed time, and state.
 */
// TODO only used once, do we really need it
public class SpeedTimeAndStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 1769385261436134444L;
    private final double dt;
    private final double speed;
    private final double acceleration;
    private final double s;
    private final TrainState activity;

    /**
     * Creates a state sample for a train motion.
     *
     * @param speed speed at the sample time
     * @param acceleration acceleration at the sample time
     * @param distance distance traveled by the sample time
     * @param time elapsed time represented by the sample
     * @param activity train state at the sample
     * @throws IllegalArgumentException if time is negative
     * @throws NullPointerException if activity is null
     */
    public SpeedTimeAndStatus(double speed, double acceleration, double distance, double time, TrainState activity) {
        if (time < 0) throw new IllegalArgumentException(String.valueOf(time));
        this.acceleration = acceleration;
        dt = time;
        s = distance;
        this.speed = speed;
        this.activity = Utils.verifyNotNull(activity);
    }

    /** Compares the sampled values and train state. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SpeedTimeAndStatus other)) return false;

        if (acceleration != other.acceleration) return false;
        if (dt != other.dt) return false;
        if (s != other.s) return false;
        if (speed != other.speed) return false;
        return activity != null ? activity == other.activity : other.activity == null;
    }

    /** Returns a hash code consistent with {@link #equals(Object)}. */
    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = dt != +0.0d ? Double.doubleToLongBits(dt) : 0L;
        result = Long.hashCode(temp);
        temp = speed != +0.0d ? Double.doubleToLongBits(speed) : 0L;
        result = 29 * result + Long.hashCode(temp);
        temp = acceleration != +0.0d ? Double.doubleToLongBits(acceleration) : 0L;
        result = 29 * result + Long.hashCode(temp);
        temp = s != +0.0d ? Double.doubleToLongBits(s) : 0L;
        result = 29 * result + Long.hashCode(temp);
        result = 29 * result + (activity != null ? activity.hashCode() : 0);
        return result;
    }

}
