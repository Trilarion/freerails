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

/*
 *
 */
package freerails.server;

import freerails.model.ModelConstants;
import freerails.model.cargo.CargoBatch;
import freerails.model.cargo.CargoBatchBundle;
import freerails.model.cargo.UnmodifiableCargoBatchBundle;
import freerails.model.finance.Money;
import freerails.model.finance.transaction.BondItemTransaction;
import freerails.model.finance.transaction.Transaction;
import freerails.model.finance.transaction.TransactionCategory;
import freerails.model.finance.transaction.aggregator.ItemsTransactionAggregator;
import freerails.model.game.Clock;
import freerails.model.game.Time;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.station.StationSupply;
import freerails.model.station.StationUtils;
import freerails.model.terrain.TerrainTile;
import freerails.model.track.NoTrackException;
import freerails.model.track.OccupiedTracks;
import freerails.model.track.TrackType;
import freerails.model.train.Train;
import freerails.model.train.TrainTemplate;
import freerails.model.train.TrainUtils;
import freerails.model.train.schedule.Schedule;
import freerails.model.train.schedule.TrainOrder;
import freerails.model.world.*;
import freerails.move.*;
import freerails.move.generator.AddTrainMoveGenerator;
import freerails.move.generator.MoveGenerator;
import freerails.move.generator.MoveTrainMoveGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

// TODO why does it have to be saved and not only the world during loading and saveing? Is there some internal state that should be part of the world?
/**
 * A ServerGameModel that contains the automations used in the actual game. This is serialized during loading and
 * saving of games.
 */
public class FullServerGameModel implements ServerGameModel {

    private static final long serialVersionUID = 3978144352788820021L;
    private World world;
    private String[] passwords;

    private transient long nextModelUpdateDue;
    private transient MoveReceiver moveReceiver;

    /**
     *
     */
    public FullServerGameModel() {
        nextModelUpdateDue = System.currentTimeMillis();
    }

    // TODO this should be part of the model, at least the update part
    /**
     * Call this method once a month.
     *
     * Loops over the list of stations and adds cargo depending on what
     * the surrounding tiles supply.
     */
    public static void cargoAtStationsUpdate(World world, MoveReceiver moveReceiver) {
        // for all players
        for (Player player: world.getPlayers()) {
            // for all stations of a player
            for (Station station: world.getStations(player)) {
                StationSupply supply = station.getSupply();
                UnmodifiableCargoBatchBundle cargoBatchBundle = station.getCargoBatchBundle();
                CargoBatchBundle after = new CargoBatchBundle(cargoBatchBundle);
                int stationId = station.getId();

                for (CargoBatch cargoBatch: after.getCargoBatches()) {
                    int amount = after.getAmount(cargoBatch);

                    if (amount > 0) {
                        // (23/24)^12 = 0.60
                        after.setAmount(cargoBatch, amount * 23 / 24);
                    }
                }

                // TODO i is not an CargoTypeId
                for (int i = 0; i < world.getCargos().size(); i++) {
                    int amountSupplied = supply.getSupply(i);

                    if (amountSupplied > 0) {
                        CargoBatch cargoBatch = new CargoBatch(i, station.getLocation(), 0, stationId);
                        int amountAlready = after.getAmount(cargoBatch);

                        // Obtain the month
                        Clock clock = world.getClock();
                        int month = clock.getCurrentMonth();

                        int amountAfter = calculateAmountToAddPerMonth(amountSupplied, month) + amountAlready;
                        after.setAmount(cargoBatch, amountAfter);
                    }
                }

                // TODO change station move instead
                // Move move = new ChangeItemInListMove(PlayerKey.CargoBundles, station.getCargoBatchBundle(), before, after, player);
                Move move = new ChangeCargoAtStationMove(player, stationId, after);
                moveReceiver.process(move);
            }
        }
    }

    /**
     * If, say, 14 units get added each year, some month we should add 1 and
     * others we should add 2 such that over the year exactly 14 units get
     * added.
     *
     * Note: January is 0
     */
    public static int calculateAmountToAddPerMonth(int amountSuppliedPerYear, int month) {
        // This calculation actually delivers the requirement of rounding sometimes up and sometimes down.
        return amountSuppliedPerYear * (month + 1) / 12 - amountSuppliedPerYear * month / 12;
    }

    /**
     * Iterates over the entries in the bank account and counts the number
     * of outstanding bonds, then calculates the interest due.
     *
     * Done on the server side
     * @param world
     */
    private static List<Move> generateBondIterestMoves(UnmodifiableWorld world) {
        // TODO generates multiple moves, good idea?
        List<Move> moves = new ArrayList<>();
        for (Player player: world.getPlayers()) {
            // TODO Money arithmetic on interestDue
            long interestDue = 0;

            for (Transaction transaction: world.getTransactions(player)) {
                if (transaction instanceof BondItemTransaction) {
                    BondItemTransaction bondItemTransaction = (BondItemTransaction) transaction;
                    int interestRate = bondItemTransaction.getId();
                    interestDue += (interestRate * ModelConstants.BOND_VALUE_ISSUE.amount / 100) * bondItemTransaction.getQuantity();
                }
            }

            if (interestDue > 0) {
                Transaction transaction = new Transaction(TransactionCategory.INTEREST_CHARGE, new Money(-interestDue), world.getClock().getCurrentTime());
                moves.add(new AddTransactionMove(player, transaction));
            }
        }
        return moves;
    }

    /**
     *
     * Iterates over the entries in the BankAccount and counts the number
     * of trains, then calculates the cost of maintenance.
     *
     * @param world
     */
    private static void applyTrainMaintenanceMoves(UnmodifiableWorld world, MoveReceiver moveReceiver) {
        for (Player player: world.getPlayers()) {
            int numberOfTrains = world.getTrains(player).size();
            // TODO hardcoded constant, move to constants
            long amount = numberOfTrains * 5000;
            Transaction transaction = new Transaction(TransactionCategory.TRAIN_MAINTENANCE, new Money(-amount), world.getClock().getCurrentTime());

            Move move = new AddTransactionMove(player, transaction);
            moveReceiver.process(move);
        }
    }

    /**
     * Iterates over the entries in the BankAccount and counts the number
     * of units of each track type, then calculates the cost of maintenance.
     *
     * @param world
     * @param player
     * @param category
     * @return
     */
    private static Move generateTrackMaintenanceMoves(World world, Player player, TransactionCategory category) {
        if (TransactionCategory.TRACK_MAINTENANCE != category && TransactionCategory.STATION_MAINTENANCE != category) {
            throw new IllegalArgumentException(String.valueOf(category));
        }

        Time[] times = {Time.ZERO, world.getClock().getCurrentTime()};
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, player, times);
        aggregator.setCategory(TransactionCategory.TRACK);

        long amount = 0;

        for (TrackType trackType: world.getTrackTypes()) {
            // TODO Money arithmetic
            long maintenanceCost = trackType.getYearlyMaintenance().amount;

            // Is the track type the category we are interested in?
            boolean rightType = TransactionCategory.TRACK_MAINTENANCE == category ? !trackType.isStation() : trackType.isStation();

            if (rightType) {
                aggregator.setType(trackType.getId());
                amount += maintenanceCost * aggregator.calculateQuantity() / ModelConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE;
            }
        }

        Transaction transaction = new Transaction(category, new Money(-amount), world.getClock().getCurrentTime());

        return new AddTransactionMove(player, transaction);
    }

    /**
     * @param world
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
     * Iterator over the stations and build trains at any that have their
     * production field set.
     */
    private static void buildTrains(UnmodifiableWorld world, MoveReceiver moveReceiver) {
        // for all player
        for (Player player: world.getPlayers()) {
            // for all stations of that player
            for (Station station: world.getStations(player)) {
                List<TrainTemplate> production = station.getProduction();
                if (production.size() > 0) {

                    for (TrainTemplate trainTemplate : production) {
                        // build train
                        int engineId = trainTemplate.getEngineId();
                        List<Integer> wagonTypes = trainTemplate.getWagonTypes();

                        // If there are no wagons, setup an automatic schedule.
                        boolean autoSchedule = 0 == wagonTypes.size();

                        // generate initial schedule
                        Schedule schedule = new Schedule();

                        // Add up to 4 stations to the schedule.
                        Iterator<Station> wi = world.getStations(player).iterator();
                        while (wi.hasNext() && schedule.getNumberOfOrders() < 5) {
                            TrainOrder orders = new TrainOrder(wi.next().getId(), null, false, autoSchedule);
                            schedule.addOrder(orders);
                        }

                        schedule.setOrderToGoto(0);

                        MoveGenerator addTrain = new AddTrainMoveGenerator(engineId, wagonTypes, station.getLocation(), player, schedule);

                        Move move = addTrain.generate(world);
                        moveReceiver.process(move);
                    }
                    // removes from production list
                    Move move = new ChangeProductionAtEngineShopMove(new ArrayList<>(), station.getId(), player);
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
     * Update of some kind.
     */
    @Override
    public synchronized void update() {
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

                // Check whether we are about to start a new year..
                Clock clock = world.getClock();

                if (clock.isLastTickOfYear()) {
                    yearEnd();
                }

                // And a new month..
                if (clock.isLastTickOfMonth()) {
                    monthEnd();
                }

                // calculate "ideal world" time for next tick
                nextModelUpdateDue = nextModelUpdateDue + 1000 / gameSpeed;

            } else {
                nextModelUpdateDue = System.currentTimeMillis();
            }
        }
    }

    // TODO make this part of the constructor
    /**
     * @param moveReceiver
     */
    @Override
    public void initialize(MoveReceiver moveReceiver) {
        this.moveReceiver = moveReceiver;
        nextModelUpdateDue = System.currentTimeMillis();
    }

    /**
     * @return
     */
    @Override
    public World getWorld() {
        return world;
    }

    /**
     * @param world
     * @param passwords
     */
    @Override
    public void setWorld(World world, String[] passwords) {
        this.world = world;
        this.passwords = passwords.clone();
    }

    /**
     * @return
     */
    @Override
    public String[] getPasswords() {
        return passwords.clone();
    }

    /**
     * This is called on the last tick of each year.
     */
    private void yearEnd() {

        applyTrackMaintenanceMoves(world, moveReceiver);
        applyTrainMaintenanceMoves(world, moveReceiver);

        for (Move move: generateBondIterestMoves(world)) {
            moveReceiver.process(move);
        }

        // Grow cities
        Move move = new GrowCitiesMove();
        moveReceiver.process(move);
    }

    /**
     * This is called at the start of each new month.
     */
    private void monthEnd() {
        supplyAtStationsUpdate(world, moveReceiver);
        cargoAtStationsUpdate(world, moveReceiver);
    }

    // TODO move static code to model? what about a generator
    /**
     * Loops through all of the known stations and recalculates the
     * cargoes that they supply, demand, and convert.
     */
    public static void supplyAtStationsUpdate(World world, MoveReceiver moveReceiver) {
        for (Player player: world.getPlayers()) {
            for (Station station: world.getStations(player)) {
                TerrainTile tile = world.getTile(station.getLocation());
                int trackRuleId = tile.getTrackPiece().getTrackType().getId();
                Station stationAfter = StationUtils.calculateCargoSupplyRateAtStation(world, trackRuleId, station);

                if (!stationAfter.equals(station)) {
                    Move move = new UpdateStationCargoDemandSupplyConversionMove(player, stationAfter);
                    moveReceiver.process(move);
                }
            }
        }
    }

}