/*
 * Created on 09-Jul-2005
 *
 */
package jfreerails.world.train;

import jfreerails.world.common.Activity;
import jfreerails.world.common.ImList;

public class CompositeSpeedAgainstTime implements Activity<SpeedTimeAndStatus>,
		SpeedAgainstTime {

	private static final long serialVersionUID = 3146586143114534610L;

	private final ImList<SpeedAgainstTime> values;

	private final double duration, totalDistance;

	public CompositeSpeedAgainstTime(SpeedAgainstTime... accs) {
		values = new ImList<SpeedAgainstTime>(accs);
		values.checkForNulls();
		double tempDuration = 0, tempTotalDistance = 0;
		for (int i = 0; i < accs.length; i++) {
			tempDuration += accs[i].getT();
			tempTotalDistance += accs[i].getS();
		}
		duration = tempDuration;
		totalDistance = tempTotalDistance;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CompositeSpeedAgainstTime))
			return false;

		final CompositeSpeedAgainstTime compositeSpeedAgainstTime = (CompositeSpeedAgainstTime) o;

		if (duration != compositeSpeedAgainstTime.duration)
			return false;
		if (totalDistance != compositeSpeedAgainstTime.totalDistance)
			return false;
		if (!values.equals(compositeSpeedAgainstTime.values))
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		long temp;
		result = values.hashCode();
		temp = duration != +0.0d ? Double.doubleToLongBits(duration) : 0l;
		result = 29 * result + (int) (temp ^ (temp >>> 32));
		temp = totalDistance != +0.0d ? Double.doubleToLongBits(totalDistance)
				: 0l;
		result = 29 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	public double duration() {
		return duration;
	}

	public SpeedTimeAndStatus getState(final double dt) {
		checkT(dt);
		double acceleration;
		SpeedTimeAndStatus.Activity activity = SpeedTimeAndStatus.Activity.READY;
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
		checkT(t);
		TandI tai = getIndex(t);
		double s = 0;
		for (int i = 0; i < tai.i; i++) {
			SpeedAgainstTime acc = values.get(i);
			s += acc.getS();
		}
		SpeedAgainstTime acc = values.get(tai.i);
		s += acc.calcS(tai.offset);
		return s;
	}

	public double calcT(double s) {
		if (s > totalDistance)
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
		tSoFar += acc.calcT(sOffset);
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
		return duration;
	}

	public double getS() {
		return totalDistance;
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

	/** Used to enable 2 values to be returned from the method getIndex(double t) */
	private static class TandI {
		final double offset;

		final int i;

		TandI(int i, double t) {
			this.i = i;
			this.offset = t;
		}

	}

	void checkT(double t) {
		if (t < 0d || t > duration)
			throw new IllegalArgumentException("t="+t+", but duration="+duration);
	}

}
