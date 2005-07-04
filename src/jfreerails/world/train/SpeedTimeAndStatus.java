/*
 * Created on 01-Jul-2005
 *
 */
package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.GameTime;

/**
 * Stores the speed and status of a train immediately after an instant of time.
 * 
 * @author Luke
 * 
 */
public class SpeedTimeAndStatus implements FreerailsSerializable {

	private static final long serialVersionUID = 1L;

	public enum Activity {
		STOPPED_AT_STATION, READY, WAITING_FOR_FULL_LOAD, STOPPED_AT_SIGNAL, CRASHED, NEEDS_UPDATING
	};

	private final GameTime t;

	private final int speed;

	private final Activity activity;

	public GameTime getTime() {
		return t;
	}

	public int getTicks() {
		return t.getTicks();
	}

	public int getSpeed() {
		return speed;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SpeedTimeAndStatus))
			return false;

		final SpeedTimeAndStatus speedTimeAndStatus = (SpeedTimeAndStatus) o;

		if (speed != speedTimeAndStatus.speed)
			return false;
		if (!activity.equals(speedTimeAndStatus.activity))
			return false;
		if (!t.equals(speedTimeAndStatus.t))
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = t.hashCode();
		result = 29 * result + speed;
		result = 29 * result + activity.hashCode();
		return result;
	}

	public SpeedTimeAndStatus(GameTime t, int speed, Activity activity) {
		this.t = t;
		this.speed = speed;
		this.activity = activity;
	}

	public Activity getActivity() {
		return activity;
	}

}
