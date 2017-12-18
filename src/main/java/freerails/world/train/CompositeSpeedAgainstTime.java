/*
 * Created on 09-Jul-2005
 *
 */
package freerails.world.train;

import freerails.world.common.Activity;
import freerails.world.common.ImList;

/**
 *
 * @author jkeller1
 */
public class CompositeSpeedAgainstTime implements Activity<SpeedTimeAndStatus>,
        SpeedAgainstTime {

    private static final long serialVersionUID = 3146586143114534610L;

    private final ImList<SpeedAgainstTime> values;

    private final double finalT, finalS;

    /**
     *
     * @param accs
     */
    public CompositeSpeedAgainstTime(SpeedAgainstTime... accs) {
        values = new ImList<>(accs);
        values.checkForNulls();
        double tempDuration = 0, tempTotalDistance = 0;
        for (SpeedAgainstTime acc : accs) {
            tempDuration += acc.getT();
            tempTotalDistance += acc.getS();
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
     *
     * @return
     */
    public double duration() {
        return finalT;
    }

    /**
     *
     * @param dt
     * @return
     */
    public SpeedTimeAndStatus getState(final double dt) {
        checkT(dt);
        double acceleration;
        SpeedTimeAndStatus.TrainActivity activity = SpeedTimeAndStatus.TrainActivity.READY;
        double s = 0;
        double speed;

        TandI tai = getIndex(dt);
        SpeedAgainstTime acc = values.get(tai.i);
        speed = acc.calcV(tai.offset);
        acceleration = acc.calcA(tai.offset);
        s = acc.calcS(tai.offset);

        return new SpeedTimeAndStatus(acceleration, activity, dt, s, speed);
    }

    public double calcS(double t) {
        if (t == this.finalT)
            return this.finalS;
        checkT(t);
        TandI tai = getIndex(t);
        double s = 0;
        for (int i = 0; i < tai.i; i++) {
            SpeedAgainstTime acc = values.get(i);
            s += acc.getS();
        }
        SpeedAgainstTime acc = values.get(tai.i);
        if (tai.offset >= acc.getT()) {
            // Note, it is possible for tai.offset > acc.getT()
            // even though we called checkT(t) above
            s += acc.getS();
        } else {
            s += acc.calcS(tai.offset);
        }
        return s;
    }

    public double calcT(double s) {
        if (s == this.finalS)
            return this.finalT;
        if (s > finalS)
            throw new IllegalArgumentException(String.valueOf(s));

        double sSoFar = 0;
        double tSoFar = 0;
        int i = 0;
        SpeedAgainstTime acc = values.get(i);

        while ((sSoFar + acc.getS()) < s) {
            sSoFar += acc.getS();
            tSoFar += acc.getT();
            i++;
            acc = values.get(i);
        }
        double sOffset = s - sSoFar;
        if (sOffset >= acc.getS()) {
            tSoFar += acc.getT();
        } else {
            tSoFar += acc.calcT(sOffset);
        }
        return tSoFar;
    }

    public double calcV(double t) {
        checkT(t);
        TandI tai = getIndex(t);
        SpeedAgainstTime acc = values.get(tai.i);
        return acc.calcV(tai.offset);
    }

    public double calcA(double t) {
        checkT(t);
        TandI tai = getIndex(t);
        SpeedAgainstTime acc = values.get(tai.i);
        return acc.calcA(tai.offset);
    }

    public double getT() {
        return finalT;
    }

    public double getS() {
        return finalS;
    }

    private TandI getIndex(double t) {
        checkT(t);
        double tSoFar = 0;
        for (int i = 0; i < values.size(); i++) {
            SpeedAgainstTime acc = values.get(i);

            if (t <= (tSoFar + acc.getT())) {
                double offset = t - tSoFar;
                return new TandI(i, offset);
            }
            tSoFar += acc.getT();
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
