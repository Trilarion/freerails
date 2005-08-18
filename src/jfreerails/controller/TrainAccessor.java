/*
 * Created on 04-Mar-2005
 *
 */
package jfreerails.controller;

import jfreerails.world.cargo.ImmutableCargoBundle;
import jfreerails.world.common.ActivityIterator;
import jfreerails.world.common.ImPoint;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.SpeedTimeAndStatus;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainMotion;
import jfreerails.world.train.TrainPositionOnMap;

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
	
	public SpeedTimeAndStatus.Activity getStatus(double time){
		TrainMotion tm = findCurrentMotion(time);
		return tm.getActivity();
	}
	
	public TrainPositionOnMap findPosition(double time) {
		ActivityIterator ai = w.getActivities(p, id);
		boolean afterFinish = ai.getFinishTime() < time;
		while (afterFinish && ai.hasNext()) {
			ai.nextActivity();
			afterFinish = ai.getFinishTime() < time;
		}		
		double dt = time - ai.getStartTime();
		dt = Math.min(dt, ai.getDuration());
		TrainMotion tm = (TrainMotion) ai.getActivity();
		return tm.getState(dt);
	}

	public TrainMotion findCurrentMotion(double time) {
		ActivityIterator ai = w.getActivities(p, id);
		boolean afterFinish = ai.getFinishTime() < time;
		while (afterFinish && ai.hasNext()) {
			ai.nextActivity();
		}
		return (TrainMotion) ai.getActivity();
	}

	public TrainModel getTrain() {
		return (TrainModel) w.get(p, KEY.TRAINS, id);
	}

	public ImmutableSchedule getSchedule() {
		TrainModel train = getTrain();
		return (ImmutableSchedule) w.get(p, KEY.TRAIN_SCHEDULES, train
						.getScheduleID());
	}

	public ImmutableCargoBundle getCargoBundle() {
		TrainModel train = getTrain();
		return (ImmutableCargoBundle) w.get(p, KEY.CARGO_BUNDLES, train
						.getCargoBundleID());
	}

	/**
	 * @return the location of the station the train is currently heading
	 *         towards.
	 */
	public ImPoint getTarget() {
		TrainModel train = (TrainModel) w.get(p, KEY.TRAINS, id);
		int scheduleID = train.getScheduleID();
		ImmutableSchedule schedule = (ImmutableSchedule) w.get(
				p, KEY.TRAIN_SCHEDULES, scheduleID);
		int stationNumber = schedule.getStationToGoto();

		if (-1 == stationNumber) {
			// There are no stations on the schedule.
			return new ImPoint(0, 0);
		}

		StationModel station = (StationModel) w.get(p,
				KEY.STATIONS, stationNumber);

		return new ImPoint(station.x, station.y);
	}

}
