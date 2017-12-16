package freerails.controller;

import freerails.move.ChangeCargoBundleMove;
import freerails.move.ChangeTrainMove;
import freerails.move.Move;
import freerails.move.TransferCargoAtStationMove;
import freerails.world.cargo.CargoBatch;
import freerails.world.cargo.ImmutableCargoBundle;
import freerails.world.cargo.MutableCargoBundle;
import freerails.world.common.FreerailsSerializable;
import freerails.world.common.ImInts;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.ConvertedAtStation;
import freerails.world.station.Demand4Cargo;
import freerails.world.station.StationModel;
import freerails.world.top.KEY;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;
import freerails.world.train.Schedule;
import freerails.world.train.TrainModel;
import freerails.world.train.TrainOrdersModel;
import freerails.world.train.WagonType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

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

    private final TrainAccessor train;

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
     * @param trainNo   ID of the train
     * @param stationNo ID of the station
     * @param world     The world object
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
        train = new TrainAccessor(w, principal, trainNo);
        consist = train.getTrain().getConsist();
        getBundles();

        processTrainBundle(); // ie. unload train / dropoff cargo

        if (autoConsist) {
            ArrayList<WagonLoad> wagonsAvailable = new ArrayList<WagonLoad>();

            assert (train
                    .equals(world.get(principal, KEY.TRAINS, this.trainId)));
            Schedule schedule = train.getSchedule();
            TrainOrdersModel order = schedule.getOrder(schedule
                    .getOrderToGoto());

            int nextStationId = order.stationId;

            StationModel stationModel = (StationModel) w.get(principal,
                    KEY.STATIONS, nextStationId);
            Demand4Cargo demand = stationModel.getDemand();

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
            int engine = train.getTrain().getEngineType();
            Move m = ChangeTrainMove.generateMove(this.trainId, train
                    .getTrain(), engine, consist, principal);
            moves.add(m);
        } else if (waitingForFullLoad) {
            // Only generate a move if there is some cargo to add..
            if (changeOnTrain.beforeEqualsAfter())
                return null;
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
            Demand4Cargo demand = station.getDemand();
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
        ImInts spaceAvailable = train.spaceAvailable();

        for (int cargoType = 0; cargoType < spaceAvailable.size(); cargoType++) {
            int quantity = spaceAvailable.get(cargoType);
            if (quantity < 0) {
                int amount2transfer = -quantity;
                transferCargo(cargoType, amount2transfer, trainAfter,
                        stationAfter);
            }
        }
    }

    /**
     * Transfer cargo from the station to the train subject to the space
     * available on the train.
     */
    private void processStationBundle() {
        ImInts spaceAvailable = TrainAccessor.spaceAvailable2(w, trainAfter
                .toImmutableCargoBundle(), consist);
        for (int cargoType = 0; cargoType < spaceAvailable.size(); cargoType++) {
            int quantity = spaceAvailable.get(cargoType);
            int amount2transfer = Math.min(quantity, stationAfter
                    .getAmount(cargoType));
            transferCargo(cargoType, amount2transfer, stationAfter, trainAfter);
        }
    }

    public boolean isCargo2Transfer() {
        ImInts spaceAvailable = train.spaceAvailable();

        int total = 0;
        for (int cargoType = 0; cargoType < w.size(SKEY.CARGO_TYPES); cargoType++) {
            int quantity = spaceAvailable.get(cargoType);
            int amount2transfer = Math.min(quantity, stationAfter
                    .getAmount(cargoType));
            total += amount2transfer;
        }
        return total > 0;
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