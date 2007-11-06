/*
 * Created on 04-Mar-2005
 *
 */
package jfreerails.controller;

import java.util.HashSet;

import jfreerails.world.cargo.ImmutableCargoBundle;
import jfreerails.world.common.ActivityIterator;
import jfreerails.world.common.ImInts;
import jfreerails.world.common.ImPoint;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.common.Step;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.WorldImpl.ActivityIteratorImpl;
import jfreerails.world.track.TrackSection;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.PathOnTiles;
import jfreerails.world.train.SpeedTimeAndStatus;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainMotion;
import jfreerails.world.train.TrainOrdersModel;
import jfreerails.world.train.TrainPositionOnMap;
import jfreerails.world.train.WagonType;
import jfreerails.world.train.SpeedTimeAndStatus.TrainActivity;

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
	
	public SpeedTimeAndStatus.TrainActivity getStatus(double time){
		TrainMotion tm = findCurrentMotion(time);
		return tm.getActivity();
	}
	
	/**
	 * @return the id of the station the train is currently at, or -1 if no
	 *         current station.
	 *         
	 */
	public int getStationId(double time){
				
		TrainMotion tm = findCurrentMotion(time);
		PositionOnTrack pot = tm.getFinalPosition();
		int x = pot.getX();
		int y = pot.getY();
		
		//loop thru the station list to check if train is at the same Point
		// as
		// a station
		for (int i = 0; i < w.size(p, KEY.STATIONS); i++) {
			StationModel tempPoint = (StationModel) w.get(p, KEY.STATIONS, i);

			if (null != tempPoint && (x == tempPoint.x) && (y == tempPoint.y)) {
				return i; // train is at the station at location tempPoint
			}
		}

		return -1;	
	}
	
	public TrainPositionOnMap findPosition(double time) {
		ActivityIterator ai = w.getActivities(p, id);
		
		// goto last
	//	int count = 0;
		ai.gotoLastActivity();
		// search backwards
		while(ai.getFinishTime() >= time && ai.hasPrevious()) {
		    ai.previousActivity();
		//    count++;
		}
		boolean afterFinish = ai.getFinishTime() < time;
		while (afterFinish && ai.hasNext()) {
			ai.nextActivity();
			afterFinish = ai.getFinishTime() < time;
         //   count++;
		}	
		//System.out.println(count);
		double dt = time - ai.getStartTime();
		dt = Math.min(dt, ai.getDuration());
		TrainMotion tm = (TrainMotion) ai.getActivity();
		return tm.getState(dt);
	}

	public TrainMotion findCurrentMotion(double time) {
		ActivityIterator ai = w.getActivities(p, id);
		boolean afterFinish = ai.getFinishTime() < time;
		if(afterFinish) {
		    ai.gotoLastActivity();
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
	 * Returns true iff all the following hold.
	 * <ol>
	 * <li>The train is waiting for a full load at some station X.</li>
	 * <li>The current train order tells the train to goto station X.</li>
	 * <li>The current train order tells the train to wait for a full load.</li>
	 * <li>The current train order specifies a consist that matches the train's current consist.</li>
	 * </ol>
	 * 
	 */
	public boolean keepWaiting(){
		double time = w.currentTime().getTicks();
		int stationId = getStationId(time);
		if (stationId == -1)
			return false;
		SpeedTimeAndStatus.TrainActivity act = getStatus(time);
		if (act != TrainActivity.WAITING_FOR_FULL_LOAD)
			return false;
		ImmutableSchedule shedule = getSchedule();
		TrainOrdersModel order = shedule.getOrder(shedule.getOrderToGoto());
		if (order.stationId != stationId)
			return false;
		if (!order.waitUntilFull)
			return false;
		TrainModel train = getTrain();
		return order.getConsist().equals(train.getConsist());				
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
	
	public HashSet<TrackSection> occupiedTrackSection(double time){
		TrainMotion tm = findCurrentMotion(time);
		PathOnTiles path = tm.getPath();
		 HashSet<TrackSection>  sections = new  HashSet<TrackSection>();
		ImPoint start = path.getStart();
		int x = start.x;
		int y = start.y;
		for (int i = 0; i < path.steps(); i++) {
			Step s = path.getStep(i);
			ImPoint tile = new ImPoint(x, y);
			x+=s.deltaX;
			y+=s.deltaY;
			sections.add(new  TrackSection(s, tile));			
		}		
		return sections;
	}
	
	public boolean isMoving(double time){
		TrainMotion tm = findCurrentMotion(time);
		double speed = tm.getSpeedAtEnd();
		return speed != 0;
	}
	
	/** The space available on the train measured in cargo units.*/
	public ImInts spaceAvailable(){
		
		TrainModel train = (TrainModel) w.get(p, KEY.TRAINS, id);
		ImmutableCargoBundle bundleOnTrain = (ImmutableCargoBundle) w.get(p,
				KEY.CARGO_BUNDLES, train.getCargoBundleID());
		return spaceAvailable2(w, bundleOnTrain, train.getConsist());
	
	}
	
	public static ImInts spaceAvailable2(ReadOnlyWorld row, ImmutableCargoBundle onTrain, ImInts consist){
		// This array will store the amount of space available on the train for
		// each cargo type.
		final int NUM_CARGO_TYPES = row.size(SKEY.CARGO_TYPES);
		int[] spaceAvailable = new int[NUM_CARGO_TYPES];

		// First calculate the train's total capacity.
		for (int j = 0; j < consist.size(); j++) {
			int cargoType = consist.get(j);
			spaceAvailable[cargoType] += WagonType.UNITS_OF_CARGO_PER_WAGON;
		}

		for (int cargoType = 0; cargoType < NUM_CARGO_TYPES; cargoType++) {
			spaceAvailable[cargoType]= spaceAvailable[cargoType] - onTrain.getAmount(cargoType);			
		}
		return new ImInts(spaceAvailable);
		
	}

}
