package jfreerails.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import jfreerails.move.ChangeCargoBundleMove;
import jfreerails.move.ChangeTrainMove;
import jfreerails.move.Move;
import jfreerails.move.TransferCargoAtStationMove;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.ImmutableCargoBundle;
import jfreerails.world.cargo.MutableCargoBundle;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.ImInts;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.ConvertedAtStation;
import jfreerails.world.station.DemandAtStation;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.Schedule;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainOrdersModel;
import jfreerails.world.train.WagonType;

/**
 * This class generates moves that transfer cargo between train and the stations
 * it stops at - it also handles cargo conversions that occur when cargo is
 * dropped off.
 * 
 * @author Scott Bennett
 * @author Luke Lindsay Date Created: 4 June 2003
 */
public class DropOffAndPickupCargoMoveGenerator {
	private final ReadOnlyWorld w;

	private final TrainModel train;

	private final int trainId;

	private int trainBundleId;

	private final int stationId;

	private int stationBundleId;

	private MutableCargoBundle stationAfter;

	private MutableCargoBundle stationBefore;

	private MutableCargoBundle trainAfter;

	private MutableCargoBundle trainBefore;

	private ArrayList<Move> moves;

	private final FreerailsPrincipal principal;

	private boolean waitingForFullLoad;

	private boolean autoConsist;

	private ImInts consist = new ImInts();

	/**
	 * Stores the type and quanity of cargo in a wagon.
	 * 
	 * @author Luke
	 */
	private static class WagonLoad implements Comparable<WagonLoad> {
		final int quantity;

		final int cargoType;

		public int compareTo(WagonLoad test) {
			return quantity - test.quantity;
		}

		WagonLoad(int q, int t) {
			quantity = q;
			cargoType = t;
		}
	}

	/**
	 * Contructor.
	 * 
	 * @param trainNo
	 *            ID of the train
	 * @param stationNo
	 *            ID of the station
	 * @param world
	 *            The world object
	 */
	public DropOffAndPickupCargoMoveGenerator(int trainNo, int stationNo,
			ReadOnlyWorld world, FreerailsPrincipal p, boolean waiting,
			boolean autoConsist) {
		principal = p;
		trainId = trainNo;
		stationId = stationNo;
		w = world;
		this.autoConsist = autoConsist;
		this.waitingForFullLoad = waiting;

		train = (TrainModel) w.get(principal, KEY.TRAINS, trainId);
		consist = train.getConsist();
		getBundles();

		processTrainBundle(); // ie. unload train / dropoff cargo

		if (autoConsist) {
			ArrayList<WagonLoad> wagonsAvailable = new ArrayList<WagonLoad>();

			assert (train
					.equals(world.get(principal, KEY.TRAINS, this.trainId)));
			Schedule schedule = (ImmutableSchedule) world.get(
					principal, KEY.TRAIN_SCHEDULES, train.getScheduleID());
			TrainOrdersModel order = schedule.getOrder(schedule
					.getOrderToGoto());

			int nextStationId = order.stationId;

			StationModel stationModel = (StationModel) w.get(principal,
					KEY.STATIONS, nextStationId);
			DemandAtStation demand = stationModel.getDemand();

			for (int i = 0; i < w.size(SKEY.CARGO_TYPES); i++) {
				// If this cargo is demanded at the next scheduled station.
				if (demand.isCargoDemanded(i)) {
					int amount = stationAfter.getAmount(i);

					while (amount > 0) {
						int amount2remove = Math.min(amount,
								WagonType.UNITS_OF_CARGO_PER_WAGON);
						amount -= amount2remove;

						// Don't bother with less than half a wagon load.
						if (amount2remove * 2 > WagonType.UNITS_OF_CARGO_PER_WAGON) {
							wagonsAvailable
									.add(new WagonLoad(amount2remove, i));
						}
					}
				}
			}

			Collections.sort(wagonsAvailable);

			int numWagons2add = Math.min(wagonsAvailable.size(), 3);

			int[] temp = new int[numWagons2add];
			for (int i = 0; i < numWagons2add; i++) {
				WagonLoad wagonload = wagonsAvailable.get(i);
				temp[i] = wagonload.cargoType;
			}
			consist = new ImInts(temp);
		}

		processStationBundle(); // ie. load train / pickup cargo
	}

	public Move generateMove() {
		// The methods that calculate the before and after bundles could be
		// called from here.
		ChangeCargoBundleMove changeAtStation = new ChangeCargoBundleMove(
				stationBefore.toImmutableCargoBundle(), stationAfter
						.toImmutableCargoBundle(), stationBundleId, principal);
		ChangeCargoBundleMove changeOnTrain = new ChangeCargoBundleMove(
				trainBefore.toImmutableCargoBundle(), trainAfter
						.toImmutableCargoBundle(), trainBundleId, principal);

		moves.add(TransferCargoAtStationMove.CHANGE_AT_STATION_INDEX,
				changeAtStation);
		moves.add(TransferCargoAtStationMove.CHANGE_ON_TRAIN_INDEX,
				changeOnTrain);

		if (autoConsist) {
			int engine = train.getEngineType();
			Move m = ChangeTrainMove.generateMove(this.trainId, train, engine,
					consist, principal);
			moves.add(m);
		}

		TransferCargoAtStationMove move = new TransferCargoAtStationMove(moves,
				waitingForFullLoad);

		assert move.getChangeAtStation() == changeAtStation;
		assert move.getChangeOnTrain() == changeOnTrain;

		return move;
	}

	private void getBundles() {
		TrainModel trainModel = ((TrainModel) w.get(principal, KEY.TRAINS,
				trainId));
		trainBundleId = trainModel.getCargoBundleID();
		trainBefore = getCopyOfBundle(trainBundleId);
		trainAfter = getCopyOfBundle(trainBundleId);

		StationModel stationModel = ((StationModel) w.get(principal,
				KEY.STATIONS, stationId));
		stationBundleId = stationModel.getCargoBundleID();
		stationAfter = getCopyOfBundle(stationBundleId);
		stationBefore = getCopyOfBundle(stationBundleId);
	}

	private MutableCargoBundle getCopyOfBundle(int id) {
		FreerailsSerializable fs = w.get(principal, KEY.CARGO_BUNDLES, id);
		ImmutableCargoBundle ibundle = (ImmutableCargoBundle) fs;

		return new MutableCargoBundle(ibundle);
	}

	private void processTrainBundle() {
		Iterator<CargoBatch> batches = trainAfter.toImmutableCargoBundle()
				.cargoBatchIterator();
		StationModel station = (StationModel) w.get(principal, KEY.STATIONS,
				stationId);
		MutableCargoBundle cargoDroppedOff = new MutableCargoBundle();

		// Unload the cargo that the station demands
		while (batches.hasNext()) {
			CargoBatch cb = batches.next();

			// if the cargo is demanded and its not from this station
			// originally...
			DemandAtStation demand = station.getDemand();
			int cargoType = cb.getCargoType();

			if ((demand.isCargoDemanded(cargoType))
					&& (stationId != cb.getStationOfOrigin())) {
				int amount = trainAfter.getAmount(cb);
				cargoDroppedOff.addCargo(cb, amount);

				// Now perform any conversions..
				ConvertedAtStation converted = station.getConverted();

				if (converted.isCargoConverted(cargoType)) {
					int newCargoType = converted.getConversion(cargoType);
					CargoBatch newCargoBatch = new CargoBatch(newCargoType,
							station.x, station.y, 0, stationId);
					stationAfter.addCargo(newCargoBatch, amount);
				}

				trainAfter.setAmount(cb, 0);
			}
		}

		moves = ProcessCargoAtStationMoveGenerator.processCargo(w,
				cargoDroppedOff, this.stationId, principal, trainId);

		// Unload the cargo that there isn't space for on the train regardless
		// of whether the station
		// demands it.
		int[] spaceAvailable = this.getSpaceAvailableOnTrain();

		for (int cargoType = 0; cargoType < spaceAvailable.length; cargoType++) {
			if (spaceAvailable[cargoType] < 0) {
				int amount2transfer = -spaceAvailable[cargoType];
				transferCargo(cargoType, amount2transfer, trainAfter,
						stationAfter);
			}
		}
	}

	private void processStationBundle() {
		int[] spaceAvailable = getSpaceAvailableOnTrain();

		// Third, transfer cargo from the station to the train subject to the
		// space available on the train.
		for (int cargoType = 0; cargoType < w.size(SKEY.CARGO_TYPES); cargoType++) {
			int amount2transfer = Math.min(spaceAvailable[cargoType],
					stationAfter.getAmount(cargoType));
			transferCargo(cargoType, amount2transfer, stationAfter, trainAfter);
		}
	}

	private int[] getSpaceAvailableOnTrain() {
		// This array will store the amount of space available on the train for
		// each cargo type.
		int[] spaceAvailable = new int[w.size(SKEY.CARGO_TYPES)];

		// First calculate the train's total capacity.
		for (int j = 0; j < consist.size(); j++) {
			int cargoType = consist.get(j);

			spaceAvailable[cargoType] += WagonType.UNITS_OF_CARGO_PER_WAGON;
		}

		// Second, subtract the space taken up by cargo that the train is
		// already carrying.
		for (int cargoType = 0; cargoType < w.size(SKEY.CARGO_TYPES); cargoType++) {
			spaceAvailable[cargoType] -= trainAfter.getAmount(cargoType);
		}

		return spaceAvailable;
	}

	/**
	 * Move the specified quantity of the specifed cargotype from one bundle to
	 * another.
	 */
	private static void transferCargo(int cargoTypeToTransfer,
			int amountToTransfer, MutableCargoBundle from, MutableCargoBundle to) {
		if (0 == amountToTransfer) {
			return;
		}
		Iterator<CargoBatch> batches = from.toImmutableCargoBundle()
				.cargoBatchIterator();
		int amountTransferedSoFar = 0;

		while (batches.hasNext() && amountTransferedSoFar < amountToTransfer) {
			CargoBatch cb = batches.next();

			if (cb.getCargoType() == cargoTypeToTransfer) {
				int amount = from.getAmount(cb);
				int amountOfThisBatchToTransfer;

				if (amount < amountToTransfer - amountTransferedSoFar) {
					amountOfThisBatchToTransfer = amount;
					from.setAmount(cb, 0);
				} else {
					amountOfThisBatchToTransfer = amountToTransfer
							- amountTransferedSoFar;
					from.addCargo(cb, -amountOfThisBatchToTransfer);
				}

				to.addCargo(cb, amountOfThisBatchToTransfer);
				amountTransferedSoFar += amountOfThisBatchToTransfer;
			}
		}
	}
}