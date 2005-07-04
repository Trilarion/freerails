/*
 * Created on 04-Mar-2005
 *
 */
package jfreerails.controller;

import java.awt.Point;

import jfreerails.world.cargo.ImmutableCargoBundle;
import jfreerails.world.common.GameTime;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.SpeedTimeAndStatus;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainMotion;

/**
 * Provides convenience methods to access the properties of a train from the
 * world object.
 * 
 * @author Luke
 * 
 */
public class TrainAccessor {

	private final ReadOnlyWorld w;

	private final FreerailsPrincipal p;

	private final int id;

	public TrainAccessor(final ReadOnlyWorld w, final FreerailsPrincipal p,
			final int id) {
		this.w = w;
		this.p = p;
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public TrainMotion findCurrentMotion(GameTime time) {
		int t = time.getTicks();
		TrainMotion motionA, motionB;
		motionA = (TrainMotion) w.get(KEY.TRAIN_MOTION1, id, p);
		motionB = (TrainMotion) w.get(KEY.TRAIN_MOTION2, id, p);
		TrainMotion first, second;
		int startA = motionA.getStart().getTicks();
		int startB = motionB.getStart().getTicks();
		if (startA < startB) {
			first = motionA;
			second = motionB;
		} else {
			first = motionB;
			second = motionA;
		}
		int start = first.getStart().getTicks();
		int end = second.getEnd().getTicks();

		if (t > end)
			throw new IllegalArgumentException();
		if (t < start)
			throw new IllegalArgumentException();

		int secondStart = second.getStart().getTicks();

		TrainMotion currentMotion = secondStart > t ? first : second;
		return currentMotion;
	}

	public KEY getFirstKEY() {
		TrainMotion motionA, motionB;
		motionA = (TrainMotion) w.get(KEY.TRAIN_MOTION1, id, p);
		motionB = (TrainMotion) w.get(KEY.TRAIN_MOTION2, id, p);
		int startA = motionA.getStart().getTicks();
		int startB = motionB.getStart().getTicks();
		if (startA < startB) {
			return KEY.TRAIN_MOTION1;
		}
		return KEY.TRAIN_MOTION2;
	}

	public KEY getLastKEY() {
		TrainMotion motionA, motionB;
		motionA = (TrainMotion) w.get(KEY.TRAIN_MOTION1, id, p);
		motionB = (TrainMotion) w.get(KEY.TRAIN_MOTION2, id, p);
		int startA = motionA.getStart().getTicks();
		int startB = motionB.getStart().getTicks();
		if (startA < startB) {
			return KEY.TRAIN_MOTION2;
		}
		return KEY.TRAIN_MOTION1;
	}

	public TrainModel getTrain() {
		return (TrainModel) w.get(KEY.TRAINS, id, p);
	}

	public ImmutableSchedule getSchedule() {
		TrainModel train = getTrain();
		return (ImmutableSchedule) w.get(KEY.TRAIN_SCHEDULES, train
				.getScheduleID(), p);
	}

	public ImmutableCargoBundle getCargoBundle() {
		TrainModel train = getTrain();
		return (ImmutableCargoBundle) w.get(KEY.CARGO_BUNDLES, train
				.getCargoBundleID(), p);
	}

	public SpeedTimeAndStatus.Activity getActivity(GameTime time) {
		TrainMotion motion = findCurrentMotion(time);
		return motion.getActivity(time);
	}

	/**
	 * @return the location of the station the train is currently heading
	 *         towards.
	 */
	public Point getTarget() {
		TrainModel train = (TrainModel) w.get(KEY.TRAINS, id, p);
		int scheduleID = train.getScheduleID();
		ImmutableSchedule schedule = (ImmutableSchedule) w.get(
				KEY.TRAIN_SCHEDULES, scheduleID, p);
		int stationNumber = schedule.getStationToGoto();

		if (-1 == stationNumber) {
			// There are no stations on the schedule.
			return new Point(0, 0);
		}

		StationModel station = (StationModel) w.get(KEY.STATIONS,
				stationNumber, p);

		return new Point(station.x, station.y);
	}

}
