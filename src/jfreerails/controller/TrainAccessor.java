/*
 * Created on 04-Mar-2005
 *
 */
package jfreerails.controller;

import jfreerails.world.cargo.ImmutableCargoBundle;
import jfreerails.world.common.ImPoint;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.AKEY;
import jfreerails.world.top.ActivityIterator;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.ImmutableSchedule;
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

	public TrainMotion findCurrentMotion(double time) {
		ActivityIterator ai = w.getActivities(AKEY.TRAIN_POSITIONS, id, p);
		boolean afterFinish = ai.getFinishTime() < time;
		while (afterFinish && ai.hasNext()) {
			ai.nextActivity();
		}
		return (TrainMotion) ai.getActivity();
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

	/**
	 * @return the location of the station the train is currently heading
	 *         towards.
	 */
	public ImPoint getTarget() {
		TrainModel train = (TrainModel) w.get(KEY.TRAINS, id, p);
		int scheduleID = train.getScheduleID();
		ImmutableSchedule schedule = (ImmutableSchedule) w.get(
				KEY.TRAIN_SCHEDULES, scheduleID, p);
		int stationNumber = schedule.getStationToGoto();

		if (-1 == stationNumber) {
			// There are no stations on the schedule.
			return new ImPoint(0, 0);
		}

		StationModel station = (StationModel) w.get(KEY.STATIONS,
				stationNumber, p);

		return new ImPoint(station.x, station.y);
	}

}
