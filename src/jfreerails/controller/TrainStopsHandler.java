/*
 * Created on 18-Feb-2005
 *
 */
package jfreerails.controller;

import java.io.Serializable;
import java.util.logging.Logger;

import jfreerails.move.ChangeTrainMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.WorldDiffMove;
import jfreerails.world.cargo.ImmutableCargoBundle;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.ImInts;
import jfreerails.world.common.ImPoint;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.WorldDiffs;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.MutableSchedule;
import jfreerails.world.train.Schedule;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainOrdersModel;
import jfreerails.world.train.WagonType;

/**
 * @author Luke
 * 
 */
public class TrainStopsHandler implements Serializable {

	private static final Logger logger = Logger
			.getLogger(TrainStopsHandler.class.getName());

	private static final int NOT_AT_STATION = -1;

	private static final long serialVersionUID = 3257567287094882872L;

	private FreerailsSerializable lastCargoBundleAtStation = null;

	private final FreerailsPrincipal principal;

	private GameTime timeLoadingFinished = new GameTime(0);

	private final int trainId;

	private boolean waiting4FullLoad = false;

	private final WorldDiffs worldDiffs;

	public TrainStopsHandler(int id, FreerailsPrincipal p, WorldDiffs w) {
		trainId = id;
		principal = p;
		worldDiffs = w;		
	}

	public ImPoint arrivesAtPoint(int x, int y) {
		TrainAccessor ta = new TrainAccessor(worldDiffs, principal, trainId);

		ImPoint targetPoint = ta.getTarget();

		if (x == targetPoint.x && y == targetPoint.y) {
			updateTarget();
			targetPoint = ta.getTarget();
		} else {
			int stationNumber = getStationID(x, y);
			if (NOT_AT_STATION != stationNumber) {
				loadAndUnloadCargo(stationNumber, false, false);
			}
		}
		return targetPoint;
	}

	/**
	 * @return the number of the station the train is currently at, or -1 if no
	 *         current station.
	 */
	public int getStationID(int x, int y) {
		// loop thru the station list to check if train is at the same Point
		// as
		// a station
		for (int i = 0; i < worldDiffs.size(principal, KEY.STATIONS); i++) {
			StationModel tempPoint = (StationModel) worldDiffs.get(principal, KEY.STATIONS,
					i);

			if (null != tempPoint && (x == tempPoint.x) && (y == tempPoint.y)) {
				return i; // train is at the station at location tempPoint
			}
		}

		return -1;
		// there are no stations that exist where the train is currently
	}
	
	

	private boolean isTrainFull() {
		TrainModel train = (TrainModel) worldDiffs.get(principal, KEY.TRAINS,
				this.trainId);
		ImmutableCargoBundle bundleOnTrain = (ImmutableCargoBundle) worldDiffs.get(
				principal, KEY.CARGO_BUNDLES, train.getCargoBundleID());

		// This array will store the amount of space available on the train for
		// each cargo type.
		final int NUM_CARGO_TYPES = worldDiffs.size(SKEY.CARGO_TYPES);
		int[] spaceAvailable = new int[NUM_CARGO_TYPES];

		// First calculate the train's total capacity.
		for (int j = 0; j < train.getNumberOfWagons(); j++) {
			int cargoType = train.getWagon(j);
			spaceAvailable[cargoType] += WagonType.UNITS_OF_CARGO_PER_WAGON;
		}

		for (int cargoType = 0; cargoType < NUM_CARGO_TYPES; cargoType++) {
			if (bundleOnTrain.getAmount(cargoType) < spaceAvailable[cargoType]) {
				return false;
			}
		}

		return true;
	}

	public boolean isTrainMoving() {
		if (refreshWaitingForFullLoad()) {
			return false;
		}
		GameTime time = worldDiffs.currentTime();

		return time.getTicks() > this.timeLoadingFinished.getTicks();
	}

	public boolean refreshWaitingForFullLoad() {
		if (!waiting4FullLoad) {
			return false;
		}
		/* Check to see if the orders have changed */
		TrainModel train = (TrainModel) worldDiffs.get(principal, KEY.TRAINS,
				this.trainId);
		Schedule schedule = (ImmutableSchedule) worldDiffs.get(principal,
				KEY.TRAIN_SCHEDULES, train.getScheduleID());
		TrainOrdersModel order = schedule.getOrder(schedule.getOrderToGoto());

		if (!order.waitUntilFull) {
			updateSchedule();

			return false;
		}
		/* Add any cargo that is waiting. */
		loadAndUnloadCargo(schedule.getStationToGoto(), true, false);

		if (isTrainFull()) {
			updateSchedule();

			return false;
		}

		return true;
	}

	void loadAndUnloadCargo(int stationId, boolean waiting, boolean autoConsist) {
		/*
		 * We only want to generate a move if the station's cargo bundle is not
		 * the last one we looked at.
		 */
		StationModel station = (StationModel) worldDiffs.get(principal,
				KEY.STATIONS, stationId);
		int cargoBundleId = station.getCargoBundleID();
		FreerailsSerializable currentCargoBundleAtStation = worldDiffs.get(
				principal, KEY.CARGO_BUNDLES, cargoBundleId);

		if (currentCargoBundleAtStation != this.lastCargoBundleAtStation) {
			// train is at a station so do the cargo processing
			DropOffAndPickupCargoMoveGenerator transfer = new DropOffAndPickupCargoMoveGenerator(
					trainId, stationId, worldDiffs, principal, waiting, autoConsist);
			Move m = transfer.generateMove();
			MoveStatus ms = m.doMove(worldDiffs, principal);
			if(!ms.ok)
				throw new IllegalStateException(ms.message);
			
			this.lastCargoBundleAtStation = currentCargoBundleAtStation;
		}
	}

	void makeTrainWait(int ticks) {
		GameTime currentTime = worldDiffs.currentTime();
		timeLoadingFinished = new GameTime(currentTime.getTicks() + ticks);
	}

	private void scheduledStop() {

		TrainModel train = (TrainModel) worldDiffs.get(principal, KEY.TRAINS,
				this.trainId);
		Schedule schedule = (ImmutableSchedule) worldDiffs.get(principal,
				KEY.TRAIN_SCHEDULES, train.getScheduleID());

		ImInts wagonsToAdd = schedule.getWagonsToAdd();

		// Loading and unloading cargo takes time, so we make the train wait for
		// a few ticks.
		makeTrainWait(50);

		boolean autoConsist = schedule.autoConsist();

		if (null != wagonsToAdd) {
			int engine = train.getEngineType();
			Move m = ChangeTrainMove.generateMove(this.trainId, train, engine,
					wagonsToAdd, principal);
			m.doMove(worldDiffs, principal);
		}
		updateSchedule();
		loadAndUnloadCargo(schedule.getStationToGoto(), true, autoConsist);
	}

	void updateSchedule() {
		TrainModel train = (TrainModel) worldDiffs.get(principal, KEY.TRAINS,
				this.trainId);
		int scheduleID = train.getScheduleID();
		ImmutableSchedule currentSchedule = (ImmutableSchedule) worldDiffs.get(
				principal, KEY.TRAIN_SCHEDULES, scheduleID);
		MutableSchedule schedule = new MutableSchedule(currentSchedule);
		StationModel station = null;

		TrainOrdersModel order = schedule.getOrder(schedule.getOrderToGoto());
		waiting4FullLoad = order.waitUntilFull && !isTrainFull();

		if (!waiting4FullLoad) {
			schedule.gotoNextStaton();

			ImmutableSchedule newSchedule = schedule.toImmutableSchedule();
			worldDiffs.set(principal, KEY.TRAIN_SCHEDULES, scheduleID, newSchedule);
		

			int stationNumber = schedule.getStationToGoto();
			station = (StationModel) worldDiffs.get(principal, KEY.STATIONS,
					stationNumber);

			if (null == station) {
				logger.warning("null == station, train " + trainId
						+ " doesn't know where to go next!");
			}
		}
	}

	/**
	 * Issues a ChangeTrainScheduleMove to set the train to move to the next
	 * station.
	 */
	public void updateTarget() {
		scheduledStop();
	}
	
	public int getTrainLength(){
		TrainAccessor ta = new TrainAccessor(worldDiffs, principal, trainId);
		return ta.getTrain().getLength();
	}
	
	public Move getMoves(){
		Move m = WorldDiffMove.generate(worldDiffs, WorldDiffMove.Cause.TrainArrives);
		worldDiffs.reset();
		return m;
	}

	public boolean isWaiting4FullLoad() {
		return waiting4FullLoad;
	}

	public void setWaiting4FullLoad(boolean waiting4FullLoad) {
		this.waiting4FullLoad = waiting4FullLoad;
	}

}
