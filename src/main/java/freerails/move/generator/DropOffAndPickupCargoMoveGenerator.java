/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
import freerails.model.operations.cargo.*;
import freerails.model.economy.transaction.CargoDeliveryTransaction;
import freerails.model.economy.Money;
import freerails.model.operations.train.schedule.TrainOrder;
import freerails.move.*;

import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.operations.station.Station;
import freerails.model.operations.station.StationDemand;
import freerails.model.operations.station.StationUtils;
import freerails.model.operations.train.*;
import freerails.model.operations.train.schedule.UnmodifiableSchedule;
import freerails.util.IntPoint2D;

import java.util.*;

// TODO part of this should go to the model
/**
 * Generates moves that transfer cargo between train and the stations
 * it stops at - it also handles cargo conversions that occur when cargo is
 * dropped off.
 */
public class DropOffAndPickupCargoMoveGenerator {

    private final int trainId;
    private final int stationId;
    private final Player player;
    private final boolean waitingForFullLoad;
    private final boolean autoConsist;
    private CargoBatchBundle stationAfter;
    private CargoBatchBundle trainAfter;
    private CargoBatchBundle trainBefore;
    private List<Move> moves;
    private List<Integer> consist;

    /**
     * Creates a generator for transferring cargo between a train and station.
     *
     * @param trainId   ID of the train
     * @param stationId ID of the station
     * @param world     the world used to inspect the train and station
     * @param player    the owner of the train and station
     * @param waiting   whether the train waits until its requested load is full
     * @param autoConsist whether the train consist may be updated automatically
     */
    public DropOffAndPickupCargoMoveGenerator(int trainId, int stationId, UnmodifiableWorld world, Player player, boolean waiting, boolean autoConsist) {
        this.player = player;
        this.trainId = trainId;
        this.stationId = stationId;
        this.autoConsist = autoConsist;

        waitingForFullLoad = waiting;
        Train train = world.getTrain(player, trainId);
        consist = train.getConsist();
        UnmodifiableCargoBatchBundle cargoBatchBundle = train.getCargoBatchBundle();
        trainBefore = new CargoBatchBundle(cargoBatchBundle);
        trainAfter = new CargoBatchBundle(cargoBatchBundle);

        Station station = world.getStation(player, stationId);
        stationAfter = new CargoBatchBundle(station.getCargoBatchBundle());

        // TODO should all this be done here? I thought this is only a Move generator
        // process cargo ie. unload train / drop-off cargo
        CargoBatchBundle cargoDroppedOff = new CargoBatchBundle();

        // Unload the cargo that the station demands
        for (CargoBatch cargoBatch : trainAfter.getCargoBatches()) {
            int cargoTypeId = cargoBatch.getCargoTypeId();
            // if cargo is demanded and it's not from this station originally...
            StationDemand stationDemand = station.getDemandForCargo();

            if (stationDemand.isCargoDemanded(cargoTypeId) && stationId != cargoBatch.getOriginalStationId()) {
                int amount = trainAfter.getAmount(cargoBatch);
                cargoDroppedOff.addCargo(cargoBatch, amount);

                // Now perform any conversions...
                OptionalInt productCargoId = StationUtils.cargoConversionAtStation(world, station, cargoTypeId);
                if (productCargoId.isPresent()) {
                    int newCargoType = productCargoId.getAsInt();
                    CargoBatch newCargoBatch = new CargoBatch(
                            newCargoType, station.getLocation(), world.getClock().getCurrentTime(), stationId);
                    stationAfter.addCargo(newCargoBatch, amount);
                }

                trainAfter.setAmount(cargoBatch, 0);
            }
        }

        // Generates Moves that pay the player for delivering the cargo.
        moves = new ArrayList<>();

        for (CargoBatch cargoBatch : cargoDroppedOff.getCargoBatches()) {
            double distance = IntPoint2D.subtract(cargoBatch.getSourceP(), station.getLocation()).norm();
            int amount = cargoDroppedOff.getAmount(cargoBatch);

            Money money = new Money((long) (amount * Math.log(distance) * ModelConstants.CARGO_DELIVERY_EARNINGS_FACTOR));
            CargoDeliveryTransaction transaction = new CargoDeliveryTransaction(money, world.getClock().getCurrentTime(), amount, stationId, trainId, cargoBatch);
            moves.add(new AddTransactionMove(player, transaction));
        }

        // Unload the cargo that there isn't space for on the train regardless
        // of whether the station demands it.
        // determine the space available on the train measured in cargo units.
        List<Integer> spaceAvailable1 = TrainUtils.spaceAvailable2(world, train.getCargoBatchBundle(), train.getConsist());

        for (int cargoType1 = 0; cargoType1 < spaceAvailable1.size(); cargoType1++) {
            int quantity1 = spaceAvailable1.get(cargoType1);
            if (quantity1 < 0) {
                int amountToTransfer1 = -quantity1;
                int cargoId = world.getCargoAtIndex(cargoType1).getId();
                CargoUtils.transferCargo(cargoId, amountToTransfer1, trainAfter, stationAfter);
            }
        }

        if (autoConsist) {
            UnmodifiableSchedule schedule = train.getSchedule();
            int currentOrderIndex = schedule.getCurrentOrderIndex();
            if (currentOrderIndex >= 0) {
                List<WagonLoad> wagonsAvailable = new ArrayList<>();
                TrainOrder order = schedule.getOrder(currentOrderIndex);
                Station otherStation = world.getStation(player, order.getStationId());
                StationDemand demand = otherStation.getDemandForCargo();

                for (Cargo cargo : world.getCargos()) {
                    int cargoId = cargo.getId();
                    // If this cargo is demanded at the next scheduled station.
                    if (demand.isCargoDemanded(cargoId)) {
                        int amount = stationAfter.getAmountOfType(cargoId);

                        while (amount > 0) {
                            int amount2remove = Math.min(amount, ModelConstants.UNITS_OF_CARGO_PER_WAGON);
                            amount -= amount2remove;

                            // Don't bother with less than half a wagon load.
                            if (amount2remove * 2 > ModelConstants.UNITS_OF_CARGO_PER_WAGON) {
                                wagonsAvailable.add(new WagonLoad(amount2remove, cargoId));
                            }
                        }
                    }
                }

                Collections.sort(wagonsAvailable);

                int numWagonsToadd = Math.min(wagonsAvailable.size(), 3);

                Integer[] temp = new Integer[numWagonsToadd];
                for (int i = 0; i < numWagonsToadd; i++) {
                    WagonLoad wagonload = wagonsAvailable.get(i);
                    temp[i] = wagonload.cargoType();
                }
                consist = new ArrayList<>(Arrays.asList(temp));
            }
        }

        // i.e. load train / pickup cargo
        // Transfer cargo from the station to the train subject to the space
        // available on the train.
        List<Integer> spaceAvailable = TrainUtils.spaceAvailable2(world, trainAfter, consist);
        for (int cargoType = 0; cargoType < spaceAvailable.size(); cargoType++) {
            int quantity = spaceAvailable.get(cargoType);
            int cargoId = world.getCargoAtIndex(cargoType).getId();
            int amountToTransfer = Math.min(quantity, stationAfter.getAmountOfType(cargoId));
            CargoUtils.transferCargo(cargoId, amountToTransfer, stationAfter, trainAfter);
        }
    }

    /**
     * Generates the moves needed to transfer cargo between the train and station.
     *
     * @return the cargo transfer move, or {@code null} if no transfer should occur
     */
    public Move generate() {

        // Only generate a move if there is some cargo to add...
        if (!autoConsist && waitingForFullLoad && trainAfter.equals(trainBefore)) {
            return null;
        }

        // The methods that calculate the before and after bundles could be called from here.
        moves.add(new ChangeCargoAtStationMove(player, stationId, stationAfter));

        if (autoConsist) {
            moves.add(new UpdateTrainMove(player, trainId, trainAfter, consist, null));
        } else {
            moves.add(new UpdateTrainMove(player, trainId, trainAfter, null, null));
        }

        return new CompostMove(moves);
    }


}