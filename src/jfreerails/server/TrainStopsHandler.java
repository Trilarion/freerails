/*
 * Created on 18-Feb-2005
 *
 */
package jfreerails.server;

import java.util.logging.Logger;

import jfreerails.controller.TrainAccessor;
import jfreerails.move.ChangeTrainMove;
import jfreerails.move.ChangeTrainScheduleMove;
import jfreerails.move.Move;
import jfreerails.network.MoveReceiver;
import jfreerails.world.cargo.ImmutableCargoBundle;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.ImInts;
import jfreerails.world.common.ImPoint;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
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
public class TrainStopsHandler implements ServerAutomaton {

	private static final Logger logger = Logger
			.getLogger(TrainStopsHandler.class.getName());

	private static final int NOT_AT_STATION = -1;

	private static final long serialVersionUID = 3257567287094882872L;

	private FreerailsSerializable lastCargoBundleAtStation = null;

	private transient MoveReceiver moveReceiver;

	private final FreerailsPrincipal principal;

	private GameTime timeLoadingFinished = new GameTime(0);

	private final int trainId;

	private boolean waiting4FullLoad = false;

	private final ReadOnlyWorld world;

	public TrainStopsHandler(int id, FreerailsPrincipal p, ReadOnlyWorld w,
			MoveReceiver mr) {
		trainId = id;
		principal = p;
		world = w;
		moveReceiver = mr;
	}

	ImPoint arrivesAtPoint(int x, int y) {
		TrainAccessor ta = new TrainAccessor(world, principal, trainId);

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
	int getStationID(int x, int y) {
		// loop thru the station list to check if train is at the same ImPoint
		// as
		// a station
		for (int i = 0; i < world.size(KEY.STATIONS, principal); i++) {
			StationModel tempPoint = (StationModel) world.get(KEY.STATIONS, i,
					principal);

			if (null != tempPoint && (x == tempPoint.x) && (y == tempPoint.y)) {
				return i; // train is at the station at location tempPoint
			}
		}

		return -1;
		// there are no stations that exist where the train is currently
	}

	// /**
	// * @return the location of the station the train is currently heading
	// * towards.
	// */
	// ImPoint getTarget() {
	// TrainModel train = (TrainModel) world.get(KEY.TRAINS, this.trainId,
	// principal);
	// int scheduleID = train.getScheduleID();
	// ImmutableSchedule schedule = (ImmutableSchedule) world.get(
	// KEY.TRAIN_SCHEDULES, scheduleID, principal);
	// int stationNumber = schedule.getStationToGoto();
	//
	// if (-1 == stationNumber) {
	// // There are no stations on the schedule.
	// return new ImPoint(0, 0);
	// }
	//
	// StationModel station = (StationModel) world.get(KEY.STATIONS,
	// stationNumber, principal);
	//
	// return new ImPoint(station.x, station.y);
	// }

	public void initAutomaton(MoveReceiver mr) {
		moveReceiver = mr;

	}

	private boolean isTrainFull() {
		TrainModel train = (TrainModel) world.get(KEY.TRAINS, this.trainId,
				principal);
		ImmutableCargoBundle bundleOnTrain = (ImmutableCargoBundle) world.get(
				KEY.CARGO_BUNDLES, train.getCargoBundleID(), principal);

		// This array will store the amount of space available on the train for
		// each cargo type.
		final int NUM_CARGO_TYPES = world.size(SKEY.CARGO_TYPES);
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

	boolean isTrainMoving() {
		if (isWaitingForFullLoad()) {
			return false;
		}
		GameTime time = world.currentTime();

		return time.getTicks() > this.timeLoadingFinished.getTicks();
	}

	boolean isWaitingForFullLoad() {
		if (!waiting4FullLoad) {
			return false;
		}
		/* Check to see if the orders have changed */
		TrainModel train = (TrainModel) world.get(KEY.TRAINS, this.trainId,
				principal);
		Schedule schedule = (ImmutableSchedule) world.get(KEY.TRAIN_SCHEDULES,
				train.getScheduleID(), principal);
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
		StationModel station = (StationModel) world.get(KEY.STATIONS,
				stationId, principal);
		int cargoBundleId = station.getCargoBundleID();
		FreerailsSerializable currentCargoBundleAtStation = world.get(
				KEY.CARGO_BUNDLES, cargoBundleId, principal);

		if (currentCargoBundleAtStation != this.lastCargoBundleAtStation) {
			// train is at a station so do the cargo processing
			DropOffAndPickupCargoMoveGenerator transfer = new DropOffAndPickupCargoMoveGenerator(
					trainId, stationId, world, principal, waiting, autoConsist);
			Move m = transfer.generateMove();
			moveReceiver.processMove(m);
			this.lastCargoBundleAtStation = currentCargoBundleAtStation;
		}
	}

	void makeTrainWait(int ticks) {
		GameTime currentTime = world.currentTime();
		timeLoadingFinished = new GameTime(currentTime.getTicks() + ticks);
	}

	private void scheduledStop() {

		TrainModel train = (TrainModel) world.get(KEY.TRAINS, this.trainId,
				principal);
		Schedule schedule = (ImmutableSchedule) world.get(KEY.TRAIN_SCHEDULES,
				train.getScheduleID(), principal);

		ImInts wagonsToAdd = schedule.getWagonsToAdd();

		// Loading and unloading cargo takes time, so we make the train wait for
		// a few ticks.
		makeTrainWait(50);

		boolean autoConsist = schedule.autoConsist();

		if (null != wagonsToAdd) {
			int engine = train.getEngineType();
			Move m = ChangeTrainMove.generateMove(this.trainId, train, engine,
					wagonsToAdd, principal);
			moveReceiver.processMove(m);
		}
		updateSchedule();
		loadAndUnloadCargo(schedule.getStationToGoto(), true, autoConsist);
	}

	void updateSchedule() {
		TrainModel train = (TrainModel) world.get(KEY.TRAINS, this.trainId,
				principal);
		int scheduleID = train.getScheduleID();
		ImmutableSchedule currentSchedule = (ImmutableSchedule) world.get(
				KEY.TRAIN_SCHEDULES, scheduleID, principal);
		MutableSchedule schedule = new MutableSchedule(currentSchedule);
		StationModel station = null;

		TrainOrdersModel order = schedule.getOrder(schedule.getOrderToGoto());
		waiting4FullLoad = order.waitUntilFull && !isTrainFull();

		if (!waiting4FullLoad) {
			schedule.gotoNextStaton();

			ImmutableSchedule newSchedule = schedule.toImmutableSchedule();
			ChangeTrainScheduleMove move = new ChangeTrainScheduleMove(
					scheduleID, currentSchedule, newSchedule, principal);
			moveReceiver.processMove(move);

			int stationNumber = schedule.getStationToGoto();
			station = (StationModel) world.get(KEY.STATIONS, stationNumber,
					principal);

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
	void updateTarget() {
		scheduledStop();
	}

}
