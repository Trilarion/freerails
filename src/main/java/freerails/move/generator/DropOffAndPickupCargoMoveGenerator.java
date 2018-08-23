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
import freerails.util.Vec2D;

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
            // if the cargo is demanded and its not from this station originally...
            StationDemand stationDemand = station.getDemandForCargo();

            if (stationDemand.isCargoDemanded(cargoTypeId) && stationId != cargoBatch.getOriginalStationId()) {
                int amount = trainAfter.getAmount(cargoBatch);
                cargoDroppedOff.addCargo(cargoBatch, amount);

                // Now perform any conversions..
                StationCargoConversion converted = station.getCargoConversion();

                if (converted.convertsCargo(cargoTypeId)) {
                    int newCargoType = converted.getConversion(cargoTypeId);
                    CargoBatch newCargoBatch = new CargoBatch(newCargoType, station.getLocation(), 0, stationId);
                    stationAfter.addCargo(newCargoBatch, amount);
                }

                trainAfter.setAmount(cargoBatch, 0);
            }
        }

        // Generates Moves that pay the player for delivering the cargo.
        moves = new ArrayList<>();

        for (CargoBatch cargoBatch : cargoDroppedOff.getCargoBatches()) {
            double distance = Vec2D.subtract(cargoBatch.getSourceP(), station.getLocation()).norm();
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
                CargoUtils.transferCargo(cargoType1, amountToTransfer1, trainAfter, stationAfter);
            }
        }

        if (autoConsist) {
            List<WagonLoad> wagonsAvailable = new ArrayList<>();

            UnmodifiableSchedule schedule = train.getSchedule();
            TrainOrder order = schedule.getOrder(schedule.getCurrentOrderIndex());

            int nextStationId = order.getStationId();

            Station otherStation = world.getStation(player, nextStationId);
            StationDemand demand = otherStation.getDemandForCargo();

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

        // ie. load train / pickup cargo
        /**
         * Transfer cargo from the station to the train subject to the space
         * available on the train.
         */
        List<Integer> spaceAvailable = TrainUtils.spaceAvailable2(world, trainAfter, consist);
        for (int cargoType = 0; cargoType < spaceAvailable.size(); cargoType++) {
            int quantity = spaceAvailable.get(cargoType);
            int amountToTransfer = Math.min(quantity, stationAfter.getAmountOfType(cargoType));
            CargoUtils.transferCargo(cargoType, amountToTransfer, stationAfter, trainAfter);
        }
    }

    /**
     * @return
     */
    public Move generate() {

        // Only generate a move if there is some cargo to add..
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