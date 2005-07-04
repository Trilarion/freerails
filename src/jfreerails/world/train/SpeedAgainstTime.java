/*
 * Created on 03-Feb-2005
 *
 */
package jfreerails.world.train;

import static jfreerails.world.train.SpeedTimeAndStatus.Activity.READY;

import java.util.Arrays;

import jfreerails.util.Utils;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.GameTime;

/**
 * <p>
 * This class stores the values you could use to draw a graph of speed against
 * time. You specify the speed of an entity at certain points in time. Between
 * these times, the entity is assumed to be accelerating at a constant rate.
 * </p>
 * 
 * <p>
 * Example usage
 * </p>
 * <code><pre>
 * GameTime[] times = { new GameTime(0), new GameTime(10) };
 * int[] speeds = { 0, 20 };
 * SpeedAgainstTime sat = new SpeedAgainstTime(times, speeds);
 * int speed = sat.getSpeed(new GameTime(0)); //speed == 0;
 * speed = sat.getSpeed(new GameTime(5)); //speed == 10;
 * speed = sat.getSpeed(new GameTime(10)); //speed == 20;
 * int acceleration = sat.getAcceleration(new GameTime(7)); //acceleration == 2
 * 
 * </pre>
 * </code>
 * 
 * @author Luke
 * 
 */
public class SpeedAgainstTime implements FreerailsSerializable {

	private static final long serialVersionUID = 3618423722025891641L;

	public static final SpeedAgainstTime STOPPED = new SpeedAgainstTime();

	private final SpeedTimeAndStatus[] points;

	private final int totalDistance;

	private SpeedAgainstTime() {
		points = new SpeedTimeAndStatus[] {
				new SpeedTimeAndStatus(GameTime.BIG_BANG, 0, READY),
				new SpeedTimeAndStatus(GameTime.END_OF_THE_WORLD, 0, READY) };
		totalDistance = 0;
	}

	/**
	 * @param times
	 * @param speeds
	 * @throws NullPointerException
	 *             if(null == times || null == points)
	 * @throws IllegalArgumentException
	 *             if times.length < 2
	 * @throws IllegalArgumentException
	 *             if times.length != points.length
	 * @throws IllegalArgumentException
	 *             if any of the speed values are negative.
	 * @throws IllegalArgumentException
	 *             if times[i] >= times[i + 1] for any i.
	 * @throws NullPointerException
	 *             if any of the time values are null.
	 * 
	 */
	public SpeedAgainstTime(GameTime[] times, int[] speeds) {
		if (null == times || null == speeds)
			throw new NullPointerException();
		if (times.length != speeds.length)
			throw new IllegalArgumentException();
		if (times.length < 1)
			throw new IllegalArgumentException("times.length < 2");

		points = new SpeedTimeAndStatus[times.length];

		for (int speed : speeds) {
			if (speed < 0)
				throw new IllegalArgumentException("speed < 0");
		}
		points[0] = new SpeedTimeAndStatus(times[0], speeds[0],
				SpeedTimeAndStatus.Activity.READY);
		for (int i = 1; i < times.length; i++) {
			if (times[i - 1].getTicks() >= times[i].getTicks())
				throw new IllegalArgumentException(
						"IllegalArgumentException if times[i] >= times[i + 1] for any i.");

			points[i] = new SpeedTimeAndStatus(times[i], speeds[i],
					SpeedTimeAndStatus.Activity.READY);
		}
		totalDistance = calTotalDistance(points);
	}

	private static int calTotalDistance(SpeedTimeAndStatus[] thePoints) {
		int s = 0;
		for (int i = 1; i < thePoints.length; i++) {
			long start = thePoints[i - 1].getTicks();
			long end = thePoints[i].getTicks();

			long dt = end - start;
			s += (thePoints[i].getSpeed() + thePoints[i - 1].getSpeed()) * dt
					/ 2;

		}
		if (s < Integer.MAX_VALUE && s >= 0)
			return s;
		throw new ArithmeticException(String.valueOf(s));

	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SpeedAgainstTime))
			return false;

		final SpeedAgainstTime speedAgainstTime = (SpeedAgainstTime) o;

		if (!Arrays.equals(points, speedAgainstTime.points))
			return false;

		return true;
	}

	private void checkTime(GameTime time) {
		int t = time.getTicks();
		if (t < getStart().getTicks() || t > getEnd().getTicks())
			throw new IllegalArgumentException(String.valueOf(t));
	}

	public int getAcceleration(GameTime t) {
		for (int i = 1; i < points.length; i++) {
			int start = points[i - 1].getTicks();
			int end = points[i].getTicks();
			if (start <= t.getTicks() && end > t.getTicks()) {
				int dSpeed = points[i].getSpeed() - points[i - 1].getSpeed();
				int dt = end - start;
				return dSpeed / dt;
			}
		}
		throw new IllegalArgumentException(String.valueOf(t));
	}

	public int getDistance(GameTime t) {
		checkTime(t);
		long s = 0;// distance travelled.
		long time = t.getTicks();
		for (int i = 1; i < points.length; i++) {
			long start = points[i - 1].getTicks();
			long end = points[i].getTicks();

			if (time == start) {
				break;
			}
			assert (start < time);

			if (time >= end) {
				long dt = end - start;
				s += (points[i].getSpeed() + points[i - 1].getSpeed()) * dt / 2;
				if (time == end)
					break;
			} else {
				long dt = time - start;
				long a = getAcceleration(t);
				s += points[i - 1].getSpeed() * dt + a * dt * dt / 2; // s=ut+0.5at^2
				break;
			}
		}
		if (s < Integer.MAX_VALUE && s >= 0)
			return (int) s;
		throw new ArithmeticException(t.toString());
	}

	public GameTime getEnd() {
		return points[points.length - 1].getTime();
	}

	public int getSpeed(GameTime t) {
		int time = t.getTicks();
		for (int i = 1; i < points.length; i++) {
			int start = points[i - 1].getTicks();
			int end = points[i].getTicks();

			if (time == start)
				return points[i - 1].getSpeed();
			if (time == end)
				return points[i].getSpeed();

			if (start < time && end >= time) {
				int dSpeed = points[i].getSpeed() - points[i - 1].getSpeed();
				int dt = end - start;
				int speed = points[i - 1].getSpeed();
				speed += dSpeed * (time - start) / dt;
				return speed;
			}
		}
		throw new IllegalArgumentException(String.valueOf(t));
	}

	public SpeedTimeAndStatus.Activity getActivity(GameTime t) {
		int time = t.getTicks();
		for (int i = 1; i < points.length; i++) {
			int end = points[i].getTicks();

			if (time < end)
				return points[i - 1].getActivity();
			if (time == end)
				return points[i].getActivity();
		}
		throw new IllegalArgumentException(String.valueOf(t));
	}

	public GameTime getStart() {
		return points[0].getTime();
	}

	public GameTime getTime(int distance) {
		int s = 0;// distance travelled.

		for (int i = 1; i < points.length; i++) {
			int start = points[i - 1].getTicks();
			int end = points[i].getTicks();
			if (distance == s)
				return points[i - 1].getTime();
			assert (s < distance);
			int dt = end - start;
			int ds = (points[i].getSpeed() + points[i - 1].getSpeed()) * dt / 2;
			if (distance >= s + ds) {
				s += ds;
				if (distance == s)
					return points[i].getTime();
			} else {
				// Solve s = ut + 0.5at^2 for t.
				int acc = (points[i].getSpeed() - points[i - 1].getSpeed())
						/ (end - start);
				double a = acc * 0.5d;
				double b = points[i - 1].getSpeed();
				double c = s - distance;
				double t = Utils.solveQuadratic(a, b, c);
				return new GameTime(start + (int) Math.round(t));
			}
		}
		throw new IllegalArgumentException(String.valueOf(distance));

	}

	public int hashCode() {
		return points.length;
	}

	public SpeedAgainstTime subSection(GameTime from, GameTime to) {
		int arraySize = 2; // the minimum size.
		for (int i = 0; i < points.length; i++) {
			int t = points[i].getTicks();
			if (t > from.getTicks() && t < to.getTicks()) {
				arraySize++;
			}
		}

		SpeedTimeAndStatus[] newPoints = new SpeedTimeAndStatus[arraySize];

		newPoints[0] = new SpeedTimeAndStatus(from, getSpeed(from), READY);
		newPoints[arraySize - 1] = new SpeedTimeAndStatus(to, getSpeed(to),
				READY);
		int j = 1;
		for (int i = 0; i < points.length; i++) {
			int t = points[i].getTicks();
			if (t > from.getTicks() && t < to.getTicks()) {
				newPoints[j] = points[i];
				j++;
			}
		}

		return new SpeedAgainstTime(newPoints);
	}

	public SpeedAgainstTime(SpeedTimeAndStatus[] points) {
		this.points = points;
		totalDistance = this.getDistance(getEnd());
	}

	public int getTotalDistance() {
		return totalDistance;
	}
}
