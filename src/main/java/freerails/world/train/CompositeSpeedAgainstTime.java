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

import freerails.util.ImList;
import freerails.world.Activity;

/**
 *
 */
public class CompositeSpeedAgainstTime implements Activity<SpeedTimeAndStatus>,
        SpeedAgainstTime {

    private static final long serialVersionUID = 3146586143114534610L;
    private final ImList<SpeedAgainstTime> values;
    private final double finalT, finalS;

    /**
     * @param accs
     */
    public CompositeSpeedAgainstTime(SpeedAgainstTime... accs) {
        values = new ImList<>(accs);
        values.checkForNulls();
        double tempDuration = 0, tempTotalDistance = 0;
        for (SpeedAgainstTime acc : accs) {
            tempDuration += acc.getTime();
            tempTotalDistance += acc.getDistance();
        }
        finalT = tempDuration;
        finalS = tempTotalDistance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CompositeSpeedAgainstTime))
            return false;

        final CompositeSpeedAgainstTime compositeSpeedAgainstTime = (CompositeSpeedAgainstTime) o;

        if (finalT != compositeSpeedAgainstTime.finalT)
            return false;
        if (finalS != compositeSpeedAgainstTime.finalS)
            return false;
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
        SpeedTimeAndStatus.TrainActivity activity = SpeedTimeAndStatus.TrainActivity.READY;
        double s;
        double speed;

        TandI tai = getIndex(dt);
        SpeedAgainstTime acc = values.get(tai.i);
        speed = acc.calcVelocity(tai.offset);
        acceleration = acc.calcAcceleration(tai.offset);
        s = acc.calculateDistance(tai.offset);

        return new SpeedTimeAndStatus(acceleration, activity, dt, s, speed);
    }

    public double calculateDistance(double time) {
        if (time == this.finalT)
            return this.finalS;
        checkT(time);
        TandI tai = getIndex(time);
        double s = 0;
        for (int i = 0; i < tai.i; i++) {
            SpeedAgainstTime acc = values.get(i);
            s += acc.getDistance();
        }
        SpeedAgainstTime acc = values.get(tai.i);
        if (tai.offset >= acc.getTime()) {
            // Note, it is possible for tai.offset > acc.getTransaction()
            // even though we called checkT(t) above
            s += acc.getDistance();
        } else {
            s += acc.calculateDistance(tai.offset);
        }
        return s;
    }

    public double calculateTime(double distance) {
        if (distance == this.finalS)
            return this.finalT;
        if (distance > finalS)
            throw new IllegalArgumentException(String.valueOf(distance));

        double sSoFar = 0;
        double tSoFar = 0;
        int i = 0;
        SpeedAgainstTime acc = values.get(i);

        while ((sSoFar + acc.getDistance()) < distance) {
            sSoFar += acc.getDistance();
            tSoFar += acc.getTime();
            i++;
            acc = values.get(i);
        }
        double sOffset = distance - sSoFar;
        if (sOffset >= acc.getDistance()) {
            tSoFar += acc.getTime();
        } else {
            tSoFar += acc.calculateTime(sOffset);
        }
        return tSoFar;
    }

    public double calcVelocity(double time) {
        checkT(time);
        TandI tai = getIndex(time);
        SpeedAgainstTime acc = values.get(tai.i);
        return acc.calcVelocity(tai.offset);
    }

    public double calcAcceleration(double time) {
        checkT(time);
        TandI tai = getIndex(time);
        SpeedAgainstTime acc = values.get(tai.i);
        return acc.calcAcceleration(tai.offset);
    }

    public double getTime() {
        return finalT;
    }

    public double getDistance() {
        return finalS;
    }

    private TandI getIndex(double t) {
        checkT(t);
        double tSoFar = 0;
        for (int i = 0; i < values.size(); i++) {
            SpeedAgainstTime acc = values.get(i);

            if (t <= (tSoFar + acc.getTime())) {
                double offset = t - tSoFar;
                return new TandI(i, offset);
            }
            tSoFar += acc.getTime();
        }
        // Should never happen since we call checkT() above!
        throw new IllegalStateException(String.valueOf(t));
    }

    void checkT(double t) {
        if (t < 0d || t > finalT)
            throw new IllegalArgumentException("t=" + t + ", but duration="
                    + finalT);
    }

    /**
     * Used to enable 2 values to be returned from the method getIndex(double t)
     */
    private static class TandI {
        final double offset;

        final int i;

        TandI(int i, double t) {
            this.i = i;
            this.offset = t;
        }

    }

}
