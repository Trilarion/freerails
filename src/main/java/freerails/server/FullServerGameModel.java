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

/*
 *
 */
package freerails.server;

import freerails.model.ModelConstants;
import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.model.economy.Money;
import freerails.model.economy.transaction.BondItemTransaction;
import freerails.model.economy.transaction.Transaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.economy.transaction.aggregator.ItemsTransactionAggregator;
import freerails.model.simulation.Clock;
import freerails.model.simulation.Time;
import freerails.model.player.Player;
import freerails.model.operations.station.Station;
import freerails.model.operations.station.StationUtils;
import freerails.model.track.NoTrackException;
import freerails.model.track.OccupiedTracks;
import freerails.model.track.TrackType;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.TrainTemplate;
import freerails.model.building.EngineShop;
import freerails.model.operations.train.TrainUtils;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.schedule.TrainOrder;
import freerails.model.world.*;
import freerails.move.*;
import freerails.move.generator.AddTrainMoveGenerator;
import freerails.move.generator.MoveGenerator;
import freerails.move.generator.MoveTrainMoveGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A ServerGameModel that contains the automations used in the actual game. Saved games store only the world (see
 * {@link SavedGame}), so this model holds no persistent state of its own.
 */
public class FullServerGameModel implements ServerGameModel {

    private World world;
    private long nextModelUpdateDue;

    /**
     * Maintains and advances the authoritative game world on the server.
     */
    public FullServerGameModel() {
        nextModelUpdateDue = System.currentTimeMillis();
    }

    /**
     * Call this method once a month.
     * <p>
     * Loops over the list of stations and sends a move that updates the cargo waiting at each station, as
     * calculated by {@link StationUtils#calculateCargoAtStationAfterMonth}.
     *
     * @param world the world containing the stations and cargo
     * @param moveReceiver the receiver used to apply each generated move
     */
    public static void cargoAtStationsUpdate(UnmodifiableWorld world, MoveReceiver moveReceiver) {
        for (Player player: world.getPlayers()) {
            for (Station station: world.getStations(player)) {
                CargoBatchBundle after = StationUtils.calculateCargoAtStationAfterMonth(world, station);
                Move move = new ChangeCargoAtStationMove(player, station.getId(), after);
                moveReceiver.process(move);
            }
        }
    }

    /**
     * Iterates over the entries in the bank account and counts the number
     * of outstanding bonds, then calculates the interest due.
     * <p>
     * Done on the server side
     * @param world the game world used by this operation
     * @param moveReceiver the receiver used to apply each generated move
     */
    private static void applyBondInterestMoves(UnmodifiableWorld world, MoveReceiver moveReceiver) {
        for (Player player: world.getPlayers()) {
            Money interestDue = Money.ZERO;

            for (Transaction transaction: world.getTransactions(player)) {
                if (transaction instanceof BondItemTransaction bondItemTransaction) {
                    int interestRate = bondItemTransaction.getId();
                    Money interestPerBond = Money.divide(Money.multiply(ModelConstants.BOND_VALUE_ISSUE, interestRate), 100);
                    interestDue = Money.add(interestDue, Money.multiply(interestPerBond, bondItemTransaction.getQuantity()));
                }
            }

            if (interestDue.amount() > 0) {
                Transaction transaction = new Transaction(TransactionCategory.INTEREST_CHARGE, Money.opposite(interestDue), world.getClock().getCurrentTime());
                moveReceiver.process(new AddTransactionMove(player, transaction));
            }
        }
    }

    /**
     *
     * Iterates over the entries in the BankAccount and counts the number
     * of trains, then calculates the cost of maintenance.
     *
     * @param world the game world used by this operation
     */
    private static void applyTrainMaintenanceMoves(UnmodifiableWorld world, MoveReceiver moveReceiver) {
        for (Player player: world.getPlayers()) {
            int numberOfTrains = world.getTrains(player).size();
            Money amount = Money.multiply(ModelConstants.TRAIN_MAINTENANCE_PER_YEAR, numberOfTrains);
            Transaction transaction = new Transaction(TransactionCategory.TRAIN_MAINTENANCE, Money.opposite(amount), world.getClock().getCurrentTime());

            Move move = new AddTransactionMove(player, transaction);
            moveReceiver.process(move);
        }
    }

    /**
     * Iterates over the entries in the BankAccount and counts the number
     * of units of each track type, then calculates the cost of maintenance.
     *
     * @param world the game world used by this operation
     * @param player the player who owns or performs the operation
     * @param category the financial transaction category to evaluate
     * @return the maintenance move generated for track in this world, or {@code null} if none is needed
     */
    private static Move generateTrackMaintenanceMoves(World world, Player player, TransactionCategory category) {
        if (TransactionCategory.TRACK_MAINTENANCE != category && TransactionCategory.STATION_MAINTENANCE != category) {
            throw new IllegalArgumentException(String.valueOf(category));
        }

        Time[] times = {Time.ZERO, world.getClock().getCurrentTime()};
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, player, times);
        aggregator.setCategory(TransactionCategory.TRACK);

        Money amount = Money.ZERO;

        for (TrackType trackType: world.getTrackTypes()) {
            Money maintenanceCost = trackType.getYearlyMaintenance();

            // Is the track type the category we are interested in?
            boolean rightType = (TransactionCategory.TRACK_MAINTENANCE == category) != trackType.isStation();

            if (rightType) {
                aggregator.setType(trackType.getId());
                Money cost = Money.divide(Money.multiply(maintenanceCost, aggregator.calculateQuantity()), ModelConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE);
                amount = Money.add(amount, cost);
            }
        }

        Transaction transaction = new Transaction(category, Money.opposite(amount), world.getClock().getCurrentTime());

        return new AddTransactionMove(player, transaction);
    }

    /**
     * @param world the game world used by this operation
     */
    private static void applyTrackMaintenanceMoves(World world, MoveReceiver moveReceiver) {
        for (Player player: world.getPlayers()) {
            Move move = generateTrackMaintenanceMoves(world, player, TransactionCategory.TRACK_MAINTENANCE);
            moveReceiver.process(move);

            move = generateTrackMaintenanceMoves(world, player, TransactionCategory.STATION_MAINTENANCE);
            moveReceiver.process(move);
        }
    }

    /**
     * Builds trains queued at each engine shop.
     */
    private static void buildTrains(UnmodifiableWorld world, MoveReceiver moveReceiver) {
        // for all player
        for (Player player: world.getPlayers()) {
            for (EngineShop engineShop : world.getEngineShops(player)) {
                List<TrainTemplate> production = engineShop.getProduction();
                if (!production.isEmpty()) {

                    for (TrainTemplate trainTemplate : production) {
                        // build train
                        int engineId = trainTemplate.engineId();
                        List<Integer> wagonTypes = trainTemplate.wagonTypes();

                        // If there are no wagons, set up an automatic schedule.
                        boolean autoSchedule = wagonTypes.isEmpty();

                        // generate initial schedule
                        ScheduleState schedule = new ScheduleState();

                        // Add up to 4 stations to the schedule.
                        Iterator<Station> wi = world.getStations(player).iterator();
                        while (wi.hasNext() && schedule.getNumberOfOrders() < 5) {
                            int stationId = wi.next().getId();
                            TrainOrder orders = autoSchedule
                                    ? TrainOrder.autoConsist(stationId)
                                    : TrainOrder.keepConsist(stationId);
                            schedule = schedule.addOrder(orders);
                        }

                        schedule = schedule.setOrderToGoto(0);

                        MoveGenerator addTrain = new AddTrainMoveGenerator(engineId, wagonTypes, engineShop.getLocation(), player, schedule);

                        Move move = addTrain.generate(world);
                        moveReceiver.process(move);
                    }
                    // removes from production list
                    Move move = new ChangeProductionAtEngineShopMove(new ArrayList<>(), engineShop.getId(), player);
                    moveReceiver.process(move);
                }
            }
        }
    }

    /**
     * Is used by the server to generate moves that add trains, move trains, and handle stops at stations.
     */
    private static void moveTrains(UnmodifiableWorld world, MoveReceiver moveReceiver) {
        Time currentTime = world.getClock().getCurrentTime();

        for (Player player: world.getPlayers()) {
            OccupiedTracks occupiedTracks = new OccupiedTracks(player, world);
            // If a train is moving, we want it to keep moving rather than stop
            // to allow an already stationary train to start moving. To achieve
            // this we process moving trains first.
            Collection<MoveTrainMoveGenerator> movingTrains = new ArrayList<>();
            Collection<MoveTrainMoveGenerator> stoppedTrains = new ArrayList<>();
            for (Train train: world.getTrains(player)) {
                MoveTrainMoveGenerator moveTrain = new MoveTrainMoveGenerator(train.getId(), player, occupiedTracks);
                if (TrainUtils.isUpdateDue(world, player, train.getId())) {
                    if (train.isMoving(currentTime)) {
                        movingTrains.add(moveTrain);
                    } else {
                        stoppedTrains.add(moveTrain);
                    }
                }
            }
            for (MoveTrainMoveGenerator generator : movingTrains) {
                Move move;
                try {
                    move = generator.generate(world);
                } catch (NoTrackException e) {
                    continue; // user deleted track, continue and ignore train!
                }
                moveReceiver.process(move);
            }
            for (MoveTrainMoveGenerator generator : stoppedTrains) {
                Move move = generator.generate(world);
                moveReceiver.process(move);
            }
        }
    }

    /**
     * Runs all ticks that are due: builds queued trains and, unless paused, advances the clock, moves trains and
     * handles month and year ends.
     *
     * @param moveReceiver the receiver to which generated moves are submitted
     */
    @Override
    public synchronized void update(MoveReceiver moveReceiver) {
        if (world == null) {
            return;
        }

        long frameStartTime = System.currentTimeMillis();

        while (nextModelUpdateDue <= frameStartTime) {
            // First do the things that need doing whether or not the game is paused.

            buildTrains(world, moveReceiver);

            int gameSpeed = world.getSpeed().getTicksPerSecond();

            if (gameSpeed > 0) {
                // update the time first, since other updates might need to know the current time.
                moveReceiver.process(new AdvanceClockMove(Player.AUTHORITATIVE));

                // now do the other updates like moving the trains
                moveTrains(world, moveReceiver);

                // Check whether we are about to start a new year...
                Clock clock = world.getClock();

                if (clock.isLastTickOfYear()) {
                    yearEnd(moveReceiver);
                }

                // And a new month...
                if (clock.isLastTickOfMonth()) {
                    monthEnd(moveReceiver);
                }

                // calculate "ideal world" time for next tick
                nextModelUpdateDue = nextModelUpdateDue + 1000 / gameSpeed;

            } else {
                nextModelUpdateDue = System.currentTimeMillis();
            }
        }
    }

    /**
     * @return the authoritative world maintained by this server model
     */
    @Override
    public World getWorld() {
        return world;
    }

    /**
     * @param world the game world used by this operation
     */
    @Override
    public synchronized void setWorld(World world) {
        this.world = world;
        nextModelUpdateDue = System.currentTimeMillis();
    }

    /**
     * This is called on the last tick of each year.
     */
    private void yearEnd(MoveReceiver moveReceiver) {

        applyTrackMaintenanceMoves(world, moveReceiver);
        applyTrainMaintenanceMoves(world, moveReceiver);

        applyBondInterestMoves(world, moveReceiver);

        // Grow cities
        Move move = new GrowCitiesMove();
        moveReceiver.process(move);
    }

    /**
     * This is called at the start of each new month.
     */
    private void monthEnd(MoveReceiver moveReceiver) {
        supplyAtStationsUpdate(world, moveReceiver);
        cargoAtStationsUpdate(world, moveReceiver);
    }

    /**
     * Loops through all the known stations and recalculates the
     * cargoes that they supply, demand, and convert.
     *
     * @param world the world containing stations and track
     * @param moveReceiver the receiver used to apply each generated move
     */
    public static void supplyAtStationsUpdate(UnmodifiableWorld world, MoveReceiver moveReceiver) {
        for (Player player: world.getPlayers()) {
            for (Station station: world.getStations(player)) {
                Station stationAfter = StationUtils.calculateCargoSupplyRateAtStation(world, station);

                // Station.equals compares only ids, so compare the cargo policy itself.
                boolean policyChanged = !stationAfter.getSupply().equals(station.getSupply())
                        || !stationAfter.getDemandForCargo().equals(station.getDemandForCargo());

                if (policyChanged) {
                    Move move = new UpdateStationCargoPolicyMove(player, stationAfter);
                    moveReceiver.process(move);
                }
            }
        }
    }

}