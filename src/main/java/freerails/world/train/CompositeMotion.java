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

import freerails.util.ImmutableList;
import freerails.world.Activity;

/**
 *
 */
public class CompositeMotion implements Activity<SpeedTimeAndStatus>, Motion {

    private static final long serialVersionUID = 3146586143114534610L;
    private final ImmutableList<Motion> values;
    private final double finalT, finalS;

    /**
     * @param accs
     */
    public CompositeMotion(Motion... accs) {
        values = new ImmutableList<>(accs);
        values.verifyNoneNull();
        double tempDuration = 0, tempTotalDistance = 0;
        for (Motion acc : accs) {
            tempDuration += acc.getTotalTime();
            tempTotalDistance += acc.getTotalDistance();
        }
        finalT = tempDuration;
        finalS = tempTotalDistance;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CompositeMotion)) return false;

        final CompositeMotion compositeSpeedAgainstTime = (CompositeMotion) obj;

        if (finalT != compositeSpeedAgainstTime.finalT) return false;
        if (finalS != compositeSpeedAgainstTime.finalS) return false;
        return values.equals(compositeSpeedAgainstTime.values);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = values.hashCode();
        temp = finalT != +0.0d ? Double.doubleToLongBits(finalT) : 0L;
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        temp = finalS != +0.0d ? Double.doubleToLongBits(finalS) : 0L;
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * @return
     */
    public double duration() {
        return finalT;
    }

    /**
     * @param dt
     * @return
     */
    public SpeedTimeAndStatus getState(final double dt) {
        checkT(dt);
        double acceleration;
        TrainActivity activity = TrainActivity.READY;
        double s;
        double speed;

        TandI tai = getIndex(dt);
        Motion acc = values.get(tai.i);
        speed = acc.calculateSpeedAtTime(tai.offset);
        acceleration = acc.calculateAccelerationAtTime(tai.offset);
        s = acc.calculateDistanceAtTime(tai.offset);

        return new SpeedTimeAndStatus(speed, acceleration, s, dt, activity);
    }

    public double calculateDistanceAtTime(double time) {
        if (time == finalT) return finalS;
        checkT(time);
        TandI tai = getIndex(time);
        double s = 0;
        for (int i = 0; i < tai.i; i++) {
            Motion acc = values.get(i);
            s += acc.getTotalDistance();
        }
        Motion acc = values.get(tai.i);
        if (tai.offset >= acc.getTotalTime()) {
            // Note, it is possible for tai.offset > acc.getTransaction()
            // even though we called checkT(t) above
            s += acc.getTotalDistance();
        } else {
            s += acc.calculateDistanceAtTime(tai.offset);
        }
        return s;
    }

    public double calculateTimeAtDistance(double distance) {
        if (distance == finalS) return finalT;
        if (distance > finalS) throw new IllegalArgumentException(String.valueOf(distance));

        double sSoFar = 0;
        double tSoFar = 0;
        int i = 0;
        Motion acc = values.get(i);

        while ((sSoFar + acc.getTotalDistance()) < distance) {
            sSoFar += acc.getTotalDistance();
            tSoFar += acc.getTotalTime();
            i++;
            acc = values.get(i);
        }
        double sOffset = distance - sSoFar;
        if (sOffset >= acc.getTotalDistance()) {
            tSoFar += acc.getTotalTime();
        } else {
            tSoFar += acc.calculateTimeAtDistance(sOffset);
        }
        return tSoFar;
    }

    public double calculateSpeedAtTime(double time) {
        checkT(time);
        TandI tai = getIndex(time);
        Motion acc = values.get(tai.i);
        return acc.calculateSpeedAtTime(tai.offset);
    }

    public double calculateAccelerationAtTime(double time) {
        checkT(time);
        TandI tai = getIndex(time);
        Motion acc = values.get(tai.i);
        return acc.calculateAccelerationAtTime(tai.offset);
    }

    public double getTotalTime() {
        return finalT;
    }

    public double getTotalDistance() {
        return finalS;
    }

    private TandI getIndex(double t) {
        checkT(t);
        double tSoFar = 0;
        for (int i = 0; i < values.size(); i++) {
            Motion acc = values.get(i);

            if (t <= (tSoFar + acc.getTotalTime())) {
                double offset = t - tSoFar;
                return new TandI(i, offset);
            }
            tSoFar += acc.getTotalTime();
        }
        // Should never happen since we call checkT() above!
        throw new IllegalStateException(String.valueOf(t));
    }

    private void checkT(double t) {
        if (t < 0.0d || t > finalT) throw new IllegalArgumentException("t=" + t + ", but duration=" + finalT);
    }

    /**
     * Used to enable 2 values to be returned from the method getIndex(double t)
     */
    private static class TandI {
        private final double offset;

        private final int i;

        private TandI(int i, double t) {
            this.i = i;
            offset = t;
        }
    }

}
