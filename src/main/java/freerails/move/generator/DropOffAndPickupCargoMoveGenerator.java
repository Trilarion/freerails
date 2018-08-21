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
import freerails.model.cargo.*;
import freerails.model.finance.transaction.CargoDeliveryTransaction;
import freerails.model.finance.Money;
import freerails.model.train.schedule.TrainOrder;
import freerails.move.*;

import freerails.model.world.UnmodifiableWorld;
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
    private final int trainId;
    private final int stationId;
    private final Player player;
    private final boolean waitingForFullLoad;
    private final boolean autoConsist;
    private CargoBatchBundle stationAfter;
    private CargoBatchBundle stationBefore;
    private CargoBatchBundle trainAfter;
    private CargoBatchBundle trainBefore;
    private List<Move> moves;
    private List<Integer> consist;

    /**
     * Constructor.
     *
     * @param trainId   ID of the train
     * @param stationId ID of the station
     * @param world     The world object
     */
    public DropOffAndPickupCargoMoveGenerator(int trainId, int stationId, UnmodifiableWorld world, Player player, boolean waiting, boolean autoConsist) {
        this.player = player;
        this.trainId = trainId;
        this.stationId = stationId;
        this.world = world;
        this.autoConsist = autoConsist;
        waitingForFullLoad = waiting;
        consist = world.getTrain(player, trainId).getConsist();
        setupBundles();

        // TODO should this be done here? I thought this is only a Move generator
        processTrainBundle(); // ie. unload train / drop-off cargo

        if (autoConsist) {
            List<WagonLoad> wagonsAvailable = new ArrayList<>();

            Train train = world.getTrain(player, this.trainId);
            UnmodifiableSchedule schedule = train.getSchedule();
            TrainOrder order = schedule.getOrder(schedule.getCurrentOrderIndex());

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
                temp[i] = wagonload.getCargoType();
            }
            consist = new ArrayList<>(Arrays.asList(temp));
        }

        processStationBundle(); // ie. load train / pickup cargo
    }

    // TODO part of this should go to the model

    /**
     * Generates Moves that pay the player for delivering the cargo.
     *
     * @param world
     * @param cargoBatchBundle
     * @param stationID
     * @param player
     * @param trainId
     * @return
     */
    public static List<Move> processCargo(UnmodifiableWorld world, UnmodifiableCargoBatchBundle cargoBatchBundle, int stationID, Player player, int trainId) {
        Station thisStation = world.getStation(player, stationID);

        List<Move> moves = new ArrayList<>();

        for (CargoBatch cargoBatch : cargoBatchBundle.getCargoBatches()) {
            // TODO own function for that
            double distanceSquared = (cargoBatch.getSourceP().x - thisStation.getLocation().x) * (cargoBatch.getSourceP().x - thisStation.getLocation().x) + (cargoBatch.getSourceP().y - thisStation.getLocation().y) * (cargoBatch.getSourceP().y - thisStation.getLocation().y);
            double dist = Math.sqrt(distanceSquared);
            int quantity = cargoBatchBundle.getAmount(cargoBatch);

            double amount = quantity * Math.log(dist) * ModelConstants.CARGO_DELIVERY_EARNINGS_FACTOR;
            Money money = new Money((long) amount);
            CargoDeliveryTransaction receipt = new CargoDeliveryTransaction(money, world.getClock().getCurrentTime(), quantity, stationID, trainId, cargoBatch);
            moves.add(new AddTransactionMove(player, receipt));
        }

        return moves;
    }

    /**
     * @return
     */
    public Move generate() {
        // The methods that calculate the before and after bundles could be called from here.
        Move changeAtStation = new ChangeCargoAtStationMove(player, stationId, stationAfter);
        Move changeOnTrain = new ChangeTrainCargoMove(player, trainId, trainAfter);

        moves.add(changeAtStation);
        moves.add(changeOnTrain);

        if (autoConsist) {
            Move move = new ChangeTrainConsistMove(player, trainId, consist);
            moves.add(move);
        } else if (waitingForFullLoad) {
            // Only generate a move if there is some cargo to add..
            if (trainAfter.equals(trainBefore)) return null;
        }

        return new CompositeMove(moves);
    }

    private void setupBundles() {
        Train train = world.getTrain(player, trainId);
        UnmodifiableCargoBatchBundle cargoBatchBundle = train.getCargoBatchBundle();
        trainBefore = new CargoBatchBundle(cargoBatchBundle);
        trainAfter = new CargoBatchBundle(cargoBatchBundle);

        Station station = world.getStation(player, stationId);
        cargoBatchBundle = station.getCargoBatchBundle();
        stationAfter = new CargoBatchBundle(cargoBatchBundle);
        stationBefore = new CargoBatchBundle(cargoBatchBundle);
    }

    private void processTrainBundle() {
        Station station = world.getStation(player, stationId);
        CargoBatchBundle cargoDroppedOff = new CargoBatchBundle();

        // Unload the cargo that the station demands
        for (CargoBatch cb : trainAfter.getCargoBatches()) {
            // if the cargo is demanded and its not from this station originally...
            StationDemand demand = station.getDemandForCargo();
            int cargoType = cb.getCargoTypeId();

            if ((demand.isCargoDemanded(cargoType)) && (stationId != cb.getOriginalStationId())) {
                int amount = trainAfter.getAmount(cb);
                cargoDroppedOff.addCargo(cb, amount);

                // Now perform any conversions..
                StationCargoConversion converted = station.getCargoConversion();

                if (converted.convertsCargo(cargoType)) {
                    int newCargoType = converted.getConversion(cargoType);
                    CargoBatch newCargoBatch = new CargoBatch(newCargoType, station.getLocation(), 0, stationId);
                    stationAfter.addCargo(newCargoBatch, amount);
                }

                trainAfter.setAmount(cb, 0);
            }
        }

        moves = processCargo(world, cargoDroppedOff, stationId, player, trainId);

        // Unload the cargo that there isn't space for on the train regardless
        // of whether the station demands it.
        // determine the space available on the train measured in cargo units.
        Train train = world.getTrain(player, trainId);
        List<Integer> spaceAvailable = TrainUtils.spaceAvailable2(world, train.getCargoBatchBundle(), train.getConsist());

        for (int cargoType = 0; cargoType < spaceAvailable.size(); cargoType++) {
            int quantity = spaceAvailable.get(cargoType);
            if (quantity < 0) {
                int amountToTransfer = -quantity;
                CargoUtils.transferCargo(cargoType, amountToTransfer, trainAfter, stationAfter);
            }
        }
    }

    /**
     * Transfer cargo from the station to the train subject to the space
     * available on the train.
     */
    private void processStationBundle() {
        List<Integer> spaceAvailable = TrainUtils.spaceAvailable2(world, trainAfter, consist);
        for (int cargoType = 0; cargoType < spaceAvailable.size(); cargoType++) {
            int quantity = spaceAvailable.get(cargoType);
            int amountToTransfer = Math.min(quantity, stationAfter.getAmountOfType(cargoType));
            CargoUtils.transferCargo(cargoType, amountToTransfer, stationAfter, trainAfter);
        }
    }
}