/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.move.generator;

import freerails.model.ModelConstants;
import freerails.model.cargo.CargoBatchBundle;
import freerails.model.finances.CargoDeliveryMoneyTransaction;
import freerails.model.finances.Money;
import freerails.model.train.schedule.TrainOrder;
import freerails.move.AddTransactionMove;
import freerails.move.ChangeTrainMove;
import freerails.move.Move;
import freerails.move.listmove.ChangeItemInListMove;
import freerails.move.TransferCargoAtStationCompositeMove;

import freerails.model.world.PlayerKey;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.cargo.CargoBatch;
import freerails.model.cargo.ImmutableCargoBatchBundle;
import freerails.model.cargo.MutableCargoBatchBundle;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.station.StationCargoConversion;
import freerails.model.station.StationDemand;
import freerails.model.train.*;
import freerails.model.train.schedule.UnmodifiableSchedule;

import java.util.*;

/**
 * Generates moves that transfer cargo between train and the stations
 * it stops at - it also handles cargo conversions that occur when cargo is
 * dropped off.
 */
public class DropOffAndPickupCargoMoveGenerator {

    private final UnmodifiableWorld world;
    private final TrainAccessor trainAccessor;
    private final int trainId;
    private final int stationId;
    private final Player player;
    private final boolean waitingForFullLoad;
    private final boolean autoConsist;
    private int trainBundleId;
    private int stationBundleId;
    private MutableCargoBatchBundle stationAfter;
    private MutableCargoBatchBundle stationBefore;
    private MutableCargoBatchBundle trainAfter;
    private MutableCargoBatchBundle trainBefore;
    private List<Move> moves;
    private List<Integer> consist;

    /**
     * Constructor.
     *
     * @param trainNo   ID of the train
     * @param stationNo ID of the station
     * @param world     The world object
     */
    public DropOffAndPickupCargoMoveGenerator(int trainNo, int stationNo, UnmodifiableWorld world, Player player, boolean waiting, boolean autoConsist) {
        this.player = player;
        trainId = trainNo;
        stationId = stationNo;
        this.world = world;
        this.autoConsist = autoConsist;
        waitingForFullLoad = waiting;
        trainAccessor = new TrainAccessor(this.world, this.player, trainNo);
        consist = trainAccessor.getTrain().getConsist();
        getBundles();

        processTrainBundle(); // ie. unload train / drop-off cargo

        if (autoConsist) {
            List<WagonLoad> wagonsAvailable = new ArrayList<>();

            assert trainAccessor.equals(world.getTrain(player, trainId));
            UnmodifiableSchedule schedule = trainAccessor.getSchedule();
            TrainOrder order = schedule.getOrder(schedule.getOrderToGoto());

            int nextStationId = order.getStationId();

            Station station = world.getStation(this.player, nextStationId);
            StationDemand demand = station.getDemandForCargo();

            // TODO i is not an id
            for (int i = 0; i < world.getCargos().size(); i++) {
                // If this cargo is demanded at the next scheduled station.
                if (demand.isCargoDemanded(i)) {
                    int amount = stationAfter.getAmountOfType(i);

                    while (amount > 0) {
                        int amount2remove = Math.min(amount, ModelConstants.UNITS_OF_CARGO_PER_WAGON);
                        amount -= amount2remove;

                        // Don't bother with less than half a wagon load.
                        if (amount2remove * 2 > ModelConstants.UNITS_OF_CARGO_PER_WAGON) {
                            wagonsAvailable.add(new WagonLoad(amount2remove, i));
                        }
                    }
                }
            }

            Collections.sort(wagonsAvailable);

            int numWagonsToadd = Math.min(wagonsAvailable.size(), 3);

            Integer[] temp = new Integer[numWagonsToadd];
            for (int i = 0; i < numWagonsToadd; i++) {
                WagonLoad wagonload = wagonsAvailable.get(i);
                temp[i] = wagonload.cargoType;
            }
            consist = new ArrayList<>(Arrays.asList(temp));
        }

        processStationBundle(); // ie. load train / pickup cargo
    }

    /**
     * Move the specified quantity of the specified cargo type from one bundle to
     * another.
     */
    private static void transferCargo(int cargoTypeToTransfer, int amountToTransfer, MutableCargoBatchBundle from, MutableCargoBatchBundle to) {
        if (0 == amountToTransfer) {
            return;
        }
        Iterator<CargoBatch> batches = from.toImmutableCargoBundle().cargoBatchIterator();
        int amountTransferedSoFar = 0;

        while (batches.hasNext() && amountTransferedSoFar < amountToTransfer) {
            CargoBatch cb = batches.next();

            if (cb.getCargoTypeId() == cargoTypeToTransfer) {
                int amount = from.getAmount(cb);
                int amountOfThisBatchToTransfer;

                if (amount < amountToTransfer - amountTransferedSoFar) {
                    amountOfThisBatchToTransfer = amount;
                    from.setAmount(cb, 0);
                } else {
                    amountOfThisBatchToTransfer = amountToTransfer - amountTransferedSoFar;
                    from.addCargo(cb, -amountOfThisBatchToTransfer);
                }

                to.addCargo(cb, amountOfThisBatchToTransfer);
                amountTransferedSoFar += amountOfThisBatchToTransfer;
            }
        }
    }

    /**
     * Generates Moves that pay the player for delivering the cargo.
     *
     * @param world
     * @param bundle
     * @param stationID
     * @param player
     * @param trainId
     * @return
     */
    public static List<Move> processCargo(UnmodifiableWorld world, CargoBatchBundle bundle, int stationID, Player player, int trainId) {
        Station thisStation = world.getStation(player, stationID);
        Iterator<CargoBatch> batches = bundle.cargoBatchIterator();

        List<Move> moves = new ArrayList<>();

        while (batches.hasNext()) {
            CargoBatch batch = batches.next();
            // TODO own function for that
            double distanceSquared = (batch.getSourceP().x - thisStation.location.x) * (batch.getSourceP().x - thisStation.location.x) + (batch.getSourceP().y - thisStation.location.y) * (batch.getSourceP().y - thisStation.location.y);
            double dist = Math.sqrt(distanceSquared);
            int quantity = bundle.getAmount(batch);

            double amount = quantity * Math.log(dist) * ModelConstants.CARGO_DELIVERY_EARNINGS_FACTOR;
            Money money = new Money((long) amount);
            CargoDeliveryMoneyTransaction receipt = new CargoDeliveryMoneyTransaction(money, quantity, stationID, batch, trainId);
            moves.add(new AddTransactionMove(player, receipt));
        }

        return moves;
    }

    /**
     * @return
     */
    public Move generate() {
        // The methods that calculate the before and after bundles could be called from here.
        Move changeAtStation = new ChangeItemInListMove(PlayerKey.CargoBundles, stationBundleId, stationBefore.toImmutableCargoBundle(), stationAfter.toImmutableCargoBundle(), player);
        ChangeItemInListMove changeOnTrain = new ChangeItemInListMove(PlayerKey.CargoBundles, trainBundleId, trainBefore.toImmutableCargoBundle(), trainAfter.toImmutableCargoBundle(), player);

        moves.add(TransferCargoAtStationCompositeMove.CHANGE_AT_STATION_INDEX, changeAtStation);
        moves.add(TransferCargoAtStationCompositeMove.CHANGE_ON_TRAIN_INDEX, changeOnTrain);

        if (autoConsist) {
            int engine = trainAccessor.getTrain().getEngineId();
            Train before = trainAccessor.getTrain();
            Train after = new Train(before.getId(), engine, consist, before.getCargoBundleId(), before.getSchedule());
            // TODO we need a dedicated ChangeTrainMove
            // Move move = new ChangeItemInListMove(PlayerKey.Trains, trainId, before, after, player);
            Move move = new ChangeTrainMove(player, after);
            moves.add(move);
        } else if (waitingForFullLoad) {
            // Only generate a move if there is some cargo to add..
            if (changeOnTrain.beforeEqualsAfter()) return null;
        }

        TransferCargoAtStationCompositeMove move = new TransferCargoAtStationCompositeMove(moves);

        assert move.getChangeAtStation() == changeAtStation;
        assert move.getChangeOnTrain() == changeOnTrain;

        return move;
    }

    private void getBundles() {
        Train train = world.getTrain(player, trainId);
        trainBundleId = train.getCargoBundleId();
        trainBefore = getCopyOfBundle(trainBundleId);
        trainAfter = getCopyOfBundle(trainBundleId);

        Station station = world.getStation(player, stationId);
        stationBundleId = station.getCargoBundleID();
        stationAfter = getCopyOfBundle(stationBundleId);
        stationBefore = getCopyOfBundle(stationBundleId);
    }

    private MutableCargoBatchBundle getCopyOfBundle(int id) {
        return new MutableCargoBatchBundle((ImmutableCargoBatchBundle) world.get(player, PlayerKey.CargoBundles, id));
    }

    private void processTrainBundle() {
        Iterator<CargoBatch> batches = trainAfter.toImmutableCargoBundle().cargoBatchIterator();
        Station station = world.getStation(player, stationId);
        MutableCargoBatchBundle cargoDroppedOff = new MutableCargoBatchBundle();

        // Unload the cargo that the station demands
        while (batches.hasNext()) {
            CargoBatch cb = batches.next();

            // if the cargo is demanded and its not from this station
            // originally...
            StationDemand demand = station.getDemandForCargo();
            int cargoType = cb.getCargoTypeId();

            if ((demand.isCargoDemanded(cargoType)) && (stationId != cb.getStationOfOrigin())) {
                int amount = trainAfter.getAmount(cb);
                cargoDroppedOff.addCargo(cb, amount);

                // Now perform any conversions..
                StationCargoConversion converted = station.getCargoConversion();

                if (converted.convertsCargo(cargoType)) {
                    int newCargoType = converted.getConversion(cargoType);
                    CargoBatch newCargoBatch = new CargoBatch(newCargoType, station.location, 0, stationId);
                    stationAfter.addCargo(newCargoBatch, amount);
                }

                trainAfter.setAmount(cb, 0);
            }
        }

        moves = processCargo(world, cargoDroppedOff, stationId, player, trainId);

        // Unload the cargo that there isn't space for on the train regardless
        // of whether the station demands it.
        List<Integer> spaceAvailable = trainAccessor.spaceAvailable();

        for (int cargoType = 0; cargoType < spaceAvailable.size(); cargoType++) {
            int quantity = spaceAvailable.get(cargoType);
            if (quantity < 0) {
                int amountToTransfer = -quantity;
                transferCargo(cargoType, amountToTransfer, trainAfter, stationAfter);
            }
        }
    }

    /**
     * Transfer cargo from the station to the train subject to the space
     * available on the train.
     */
    private void processStationBundle() {
        List<Integer> spaceAvailable = TrainAccessor.spaceAvailable2(world, trainAfter.toImmutableCargoBundle(), consist);
        for (int cargoType = 0; cargoType < spaceAvailable.size(); cargoType++) {
            int quantity = spaceAvailable.get(cargoType);
            int amountToTransfer = Math.min(quantity, stationAfter.getAmountOfType(cargoType));
            transferCargo(cargoType, amountToTransfer, stationAfter, trainAfter);
        }
    }

    // TODO why needed here, maybe more general and should be needed more often?
    /**
     * Stores the type and quantity of cargo in a wagon.
     */
    private static class WagonLoad implements Comparable<WagonLoad> {

        private final int quantity;
        private final int cargoType;

        private WagonLoad(int quantity, int cargoType) {
            this.quantity = quantity;
            this.cargoType = cargoType;
        }

        public int compareTo(WagonLoad o) {
            return quantity - o.quantity;
        }
    }
}