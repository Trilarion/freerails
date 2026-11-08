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
import freerails.model.economy.Money;
import freerails.model.economy.transaction.CargoDeliveryTransaction;
import freerails.model.operations.cargo.CargoBatch;
import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.model.operations.cargo.CargoUtils;
import freerails.model.operations.cargo.ImmutableCargoBatchBundle;
import freerails.model.operations.cargo.UnmodifiableCargoBatchBundle;
import freerails.model.operations.station.Station;
import freerails.model.operations.train.TrainConfiguration;
import freerails.model.operations.train.schedule.UnmodifiableSchedule;
import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;
import freerails.move.AddTransactionMove;
import freerails.move.ChangeCargoAtStationMove;
import freerails.move.CompostMove;
import freerails.move.Move;
import freerails.move.UpdateTrainMove;
import freerails.util.IntPoint2D;

import java.util.ArrayList;
import java.util.List;
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
        this(trainId, stationId, world, player, waiting, autoConsist,
                world.getTrain(player, trainId).getSchedule(),
                world.getTrain(player, trainId).getCargoBatchBundle(),
                world.getTrain(player, trainId).getConfiguration());
    }

    /**
     * Creates a cargo transfer generator using a prospective train state.
     *
     * @param trainId ID of the train
     * @param stationId ID of the station
     * @param world the read-only world used to inspect definitions and station state
     * @param player the owner of the train and station
     * @param waiting whether the train waits until its requested load is full
     * @param autoConsist whether the train consist may be updated automatically
     * @param schedule schedule to use when selecting automatic consist
     * @param trainCargo cargo currently carried by the train
     * @param configuration prospective engine and consist
     */
    public DropOffAndPickupCargoMoveGenerator(int trainId, int stationId, UnmodifiableWorld world, Player player,
                                             boolean waiting, boolean autoConsist, UnmodifiableSchedule schedule,
                                             UnmodifiableCargoBatchBundle trainCargo,
                                             TrainConfiguration configuration) {
        this.player = player;
        this.trainId = trainId;
        this.stationId = stationId;
        this.autoConsist = autoConsist;

        waitingForFullLoad = waiting;
        trainBefore = new CargoBatchBundle(trainCargo);
        trainAfter = new CargoBatchBundle(trainCargo);

        Station station = world.getStation(player, stationId);
        stationAfter = new CargoBatchBundle(station.getCargoBatchBundle());

        CargoUtils.CargoTransferPlan cargoTransferPlan = CargoUtils.prepareCargoTransfer(world, player, schedule,
                trainCargo, configuration, station, autoConsist);
        stationAfter = cargoTransferPlan.stationAfter();
        trainAfter = cargoTransferPlan.trainAfter();
        consist = cargoTransferPlan.consist();

        // Generates Moves that pay the player for delivering the cargo.
        moves = new ArrayList<>();

        for (CargoBatch cargoBatch : cargoTransferPlan.cargoDroppedOff().getCargoBatches()) {
            double distance = IntPoint2D.subtract(cargoBatch.sourceP(), station.getLocation()).norm();
            int amount = cargoTransferPlan.cargoDroppedOff().getAmount(cargoBatch);

            Money money = new Money((long) (amount * Math.log(distance) * ModelConstants.CARGO_DELIVERY_EARNINGS_FACTOR));
            CargoDeliveryTransaction transaction = new CargoDeliveryTransaction(money, world.getClock().getCurrentTime(), amount, stationId, trainId, cargoBatch);
            moves.add(new AddTransactionMove(player, transaction));
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

    /**
     * Returns the consist resulting from the planned transfer.
     *
     * @return an immutable snapshot of the resulting wagon type identifiers
     */
    public List<Integer> getResultingConsist() {
        return List.copyOf(consist);
    }

    /**
     * Returns the cargo resulting from the planned transfer.
     *
     * @return an immutable copy of the resulting train cargo
     */
    public UnmodifiableCargoBatchBundle getResultingTrainCargo() {
        return new ImmutableCargoBatchBundle(trainAfter);
    }

}