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
package freerails.model.world;

import freerails.model.cargo.Cargo;
import freerails.model.cargo.CargoBatchBundle;
import freerails.model.cargo.CargoCategory;
import freerails.model.finance.transaction.Transaction;
import freerails.model.finance.transaction.TransactionCategory;
import freerails.model.game.Speed;
import freerails.model.game.Sentiment;
import freerails.model.game.Time;
import freerails.model.station.Station;
import freerails.model.terrain.TerrainTile;
import freerails.model.track.TrackConfiguration;
import freerails.util.Vec2D;
import freerails.util.Utils;
import freerails.model.finance.*;
import freerails.model.player.Player;
import freerails.model.track.TrackCategory;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackType;
import freerails.model.train.Train;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Collections;

/**
 * Test for World.
 */
public class WorldTest extends TestCase {

    private static final Serializable fs = new TestState(1);

    /**
     *
     */
    public void testConstructor() {
        World world = WorldGenerator.minimalWorld();
        assertEquals(world.getMapSize(), new Vec2D(20, 20));
    }

    /**
     * Tests that changing the object returned by defensiveCopy() does not alter
     * the world object that was copied.
     */
    public void testDefensiveCopy() {
        World original;
        World copy;
        original = WorldGenerator.minimalWorld();
        /**
         * Returns a copy of this world object - making changes to this copy will
         * not change this object.
         */
        copy = (World) Utils.cloneBySerialisation(original);
        assertNotSame("The copies should be different objects.", original, copy);
        assertEquals("The copies should be logically equal.", original, copy);
    }

    public void testSerializationRoundTripCreatesIndependentMutableWorld() {
        World original = createWorldWithMutableState();
        World copy = (World) Utils.cloneBySerialisation(original);
        Player copiedPlayer = copy.getPlayer(copy.getPlayerIndex(0));
        Vec2D location = Vec2D.ZERO;
        TerrainTile originalTile = original.getTile(location);

        copy.setTile(location, new TerrainTile(1));
        copy.addTransaction(copiedPlayer, new Transaction(
                TransactionCategory.MISC_INCOME, new Money(50), copy.getClock().getCurrentTime()));
        copy.addTrain(copiedPlayer, new Train(2, 0));
        copy.setSpeed(Speed.FAST);

        assertEquals(originalTile, original.getTile(location));
        assertEquals(1, original.getTransactions(original.getPlayer(0)).size());
        assertEquals(new Money(100), original.getCurrentBalance(original.getPlayer(0)));
        assertEquals(1, original.getTrains(original.getPlayer(0)).size());
        assertEquals(Speed.MODERATE, original.getSpeed());
    }

    public void testSerializationRoundTripEqualsAndHashCodeTrackWorldState() {
        World original = createWorldWithMutableState();
        World copy = (World) Utils.cloneBySerialisation(original);

        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());

        Vec2D location = Vec2D.ZERO;
        World changed = (World) Utils.cloneBySerialisation(original);
        changed.setTile(location, new TerrainTile(1));
        assertFalse(original.equals(changed));

        changed = (World) Utils.cloneBySerialisation(original);
        Player changedPlayer = changed.getPlayer(changed.getPlayerIndex(0));
        changed.addTransaction(changedPlayer, new Transaction(
                TransactionCategory.MISC_INCOME, new Money(50), changed.getClock().getCurrentTime()));
        assertFalse(original.equals(changed));

        changed = (World) Utils.cloneBySerialisation(original);
        changedPlayer = changed.getPlayer(changed.getPlayerIndex(0));
        changed.addTrain(changedPlayer, new Train(2, 0));
        assertFalse(original.equals(changed));

        changed = (World) Utils.cloneBySerialisation(original);
        changed.setSpeed(Speed.FAST);
        assertFalse(original.equals(changed));
    }

    /**
     *
     */
    public void testEquals() {
        World original;
        World copy;
        original = WorldGenerator.minimalWorld();
        /**
         * Returns a copy of this world object - making changes to this copy will
         * not change this object.
         */
        copy = (World) Utils.cloneBySerialisation(original);

        Player player = new Player(0, "Name");
        int index = copy.addPlayer(player);
        assertEquals(index, 0);
        assertFalse(copy.equals(original));
        original.addPlayer(player);
        assertEquals("The copies should be logically equal.", original, copy);

        assertTrue(Utils.equalsBySerialization(original, copy));

        Transaction transaction = new Transaction(TransactionCategory.MISC_INCOME, new Money(100), copy.getClock().getCurrentTime());
        copy.addTransaction(player, transaction);
        assertEquals(new Money(100), copy.getCurrentBalance(player));
        assertFalse(copy.equals(original));
    }

    /**
     *
     */
    public void testEquals2() {
        World original;
        World copy, copy2;
        original = WorldGenerator.minimalWorld();
        /**
         * Returns a copy of this world object - making changes to this copy will
         * not change this object.
         */
        copy = (World) Utils.cloneBySerialisation(original);
        /**
         * Returns a copy of this world object - making changes to this copy will
         * not change this object.
         */
        copy2 = (World) Utils.cloneBySerialisation(original);
        // Test adding players.
        Player a = new Player(0, "Fred");
        Player b = new Player(1, "John");
        original.addPlayer(a);
        copy.addPlayer(b);
        assertFalse(copy.equals(original));
    }

    /**
     *
     */
    public void testBoundsContain() {
        World world = WorldGenerator.minimalWorld();
        assertTrue(world.boundsContain(new Vec2D(1, 1)));
        assertTrue(world.boundsContain(Vec2D.ZERO));
        assertFalse(world.boundsContain(new Vec2D(-1, -1)));
        assertFalse(world.boundsContain(new Vec2D(20, 20)));
    }

    public void testNonSquareMapBoundsAgreeWithTileAccessAndIndexing() {
        World world = WorldGenerator.minimalWorld(new Vec2D(3, 5));
        List<Vec2D> coordinates = List.of(
                new Vec2D(0, 0), new Vec2D(0, 4), new Vec2D(2, 0), new Vec2D(2, 4),
                new Vec2D(1, 1), new Vec2D(1, 3), new Vec2D(2, 2));
        int terrainId = 1;
        for (Vec2D coordinate : coordinates) {
            assertTrue(world.boundsContain(coordinate));
            world.setTile(coordinate, new TerrainTile(terrainId++));
        }

        assertEquals(1, world.getTile(new Vec2D(0, 0)).terrainTypeId());
        assertEquals(2, world.getTile(new Vec2D(0, 4)).terrainTypeId());
        assertEquals(3, world.getTile(new Vec2D(2, 0)).terrainTypeId());
        assertEquals(4, world.getTile(new Vec2D(2, 4)).terrainTypeId());
        assertEquals(5, world.getTile(new Vec2D(1, 1)).terrainTypeId());
        assertEquals(6, world.getTile(new Vec2D(1, 3)).terrainTypeId());
        assertEquals(7, world.getTile(new Vec2D(2, 2)).terrainTypeId());

        List<Vec2D> outsideCoordinates = List.of(
                new Vec2D(-1, 0), new Vec2D(3, 0), new Vec2D(0, -1),
                new Vec2D(0, 5), new Vec2D(-1, 5), new Vec2D(3, 5));
        for (Vec2D coordinate : outsideCoordinates) {
            assertFalse(world.boundsContain(coordinate));
            try {
                world.getTile(coordinate);
                fail("Tile access must reject out-of-bounds coordinate " + coordinate);
            } catch (IndexOutOfBoundsException expected) {
                // Expected.
            }
        }
    }

    /**
     *
     */
    public void testBankAccount() {
        World world = WorldGenerator.minimalWorld();
        Player player = new Player(0, "Test");
        int playerID = world.addPlayer(player);
        assertEquals(0, playerID);
        player = world.getPlayer(playerID);
        Transaction transaction = new Transaction(TransactionCategory.BOND, new Money(100), world.getClock().getCurrentTime());
        assertEquals(new Money(0), world.getCurrentBalance(player));
        world.addTransaction(player, transaction);
        assertEquals(1, world.getTransactions(player).size());
        assertEquals(new Money(100), world.getCurrentBalance(player));
        Transaction t2 = world.getTransaction(player, 0);
        assertEquals(transaction, t2);
        Transaction t3 = world.removeLastTransaction(player);
        assertEquals(transaction, t3);
        assertEquals(new Money(0), world.getCurrentBalance(player));
    }

    public void testPlayerOwnedCollectionsAreInitializedAndIsolated() {
        World world = WorldGenerator.minimalWorld();
        Player first = new Player(10, "First");
        Player second = new Player(20, "Second");
        world.addPlayer(first);
        world.addPlayer(second);

        assertTrue(world.getTrains(first).isEmpty());
        assertTrue(world.getStations(first).isEmpty());
        assertTrue(world.getTransactions(first).isEmpty());
        assertEquals(Money.ZERO, world.getCurrentBalance(first));
        assertTrue(world.getTrains(second).isEmpty());
        assertTrue(world.getStations(second).isEmpty());
        assertTrue(world.getTransactions(second).isEmpty());
        assertEquals(Money.ZERO, world.getCurrentBalance(second));

        world.addTrain(first, new Train(1, 0));
        world.addStation(first, new Station(
                2, Vec2D.ZERO, "First station", world.getCargos().size(), CargoBatchBundle.EMPTY));
        world.addTransaction(first, transaction(world, 50));

        assertEquals(1, world.getTrains(first).size());
        assertEquals(1, world.getStations(first).size());
        assertEquals(1, world.getTransactions(first).size());
        assertEquals(new Money(50), world.getCurrentBalance(first));
        assertTrue(world.getTrains(second).isEmpty());
        assertTrue(world.getStations(second).isEmpty());
        assertTrue(world.getTransactions(second).isEmpty());
        assertEquals(Money.ZERO, world.getCurrentBalance(second));
    }

    public void testTrainAndStationIdsAreUniquePerOwner() {
        World world = WorldGenerator.minimalWorld();
        Player first = new Player(10, "First");
        Player second = new Player(20, "Second");
        world.addPlayer(first);
        world.addPlayer(second);

        Train firstTrain = new Train(7, 0);
        Train firstOtherTrain = new Train(8, 0);
        Train secondTrain = new Train(7, 0);
        world.addTrain(first, firstTrain);
        world.addTrain(first, firstOtherTrain);
        world.addTrain(second, secondTrain);
        assertDuplicateRejected(() -> world.addTrain(first, new Train(7, 1)));
        assertEquals(List.of(firstTrain, firstOtherTrain), List.copyOf(world.getTrains(first)));
        assertEquals(List.of(secondTrain), List.copyOf(world.getTrains(second)));

        Station firstStation = new Station(
                9, Vec2D.ZERO, "First", world.getCargos().size(), CargoBatchBundle.EMPTY);
        Station firstOtherStation = new Station(
                10, new Vec2D(1, 0), "First other", world.getCargos().size(), CargoBatchBundle.EMPTY);
        Station secondStation = new Station(
                9, new Vec2D(2, 0), "Second", world.getCargos().size(), CargoBatchBundle.EMPTY);
        world.addStation(first, firstStation);
        world.addStation(first, firstOtherStation);
        world.addStation(second, secondStation);
        assertDuplicateRejected(() -> world.addStation(first,
                new Station(9, new Vec2D(3, 0), "Duplicate", world.getCargos().size(), CargoBatchBundle.EMPTY)));
        assertEquals(2, world.getStations(first).size());
        assertEquals(List.of(secondStation), List.copyOf(world.getStations(second)));

        world.removeTrain(first, 7);
        assertEquals(firstOtherTrain, world.getTrain(first, 8));
        assertEquals(secondTrain, world.getTrain(second, 7));
        world.removeStation(first, 9);
        assertEquals(firstOtherStation, world.getStation(first, 10));
        assertEquals(secondStation, world.getStation(second, 9));
    }

    public void testTransactionAppendIsOrderedAndOverflowIsAtomic() {
        World world = WorldGenerator.minimalWorld();
        Player player = new Player(0, "Account");
        world.addPlayer(player);
        Transaction positive = transaction(world, 100);
        Transaction zero = transaction(world, 0);
        Transaction negative = transaction(world, -30);

        world.addTransaction(player, positive);
        world.addTransaction(player, zero);
        world.addTransaction(player, negative);
        assertEquals(List.of(positive, zero, negative), List.copyOf(world.getTransactions(player)));
        assertEquals(new Money(70), world.getCurrentBalance(player));

        World overflowWorld = WorldGenerator.minimalWorld();
        Player overflowPlayer = new Player(1, "Overflow");
        overflowWorld.addPlayer(overflowPlayer);
        Transaction maximum = transaction(overflowWorld, Long.MAX_VALUE);
        overflowWorld.addTransaction(overflowPlayer, maximum);
        Transaction overflowing = transaction(overflowWorld, 1);
        try {
            overflowWorld.addTransaction(overflowPlayer, overflowing);
            fail("Expected transaction balance overflow to be rejected.");
        } catch (ArithmeticException expected) {
            // Expected.
        }
        assertEquals(List.of(maximum), List.copyOf(overflowWorld.getTransactions(overflowPlayer)));
        assertEquals(new Money(Long.MAX_VALUE), overflowWorld.getCurrentBalance(overflowPlayer));
    }

    public void testUndoingTransactionsRestoresPriorAccountState() {
        World world = WorldGenerator.minimalWorld();
        Player player = new Player(0, "Account");
        world.addPlayer(player);
        Transaction first = transaction(world, 100);
        Transaction second = transaction(world, -35);
        Transaction third = transaction(world, 0);
        world.addTransaction(player, first);
        world.addTransaction(player, second);
        world.addTransaction(player, third);

        assertEquals(third, world.removeLastTransaction(player));
        assertEquals(new Money(65), world.getCurrentBalance(player));
        assertEquals(second, world.removeLastTransaction(player));
        assertEquals(new Money(100), world.getCurrentBalance(player));
        assertEquals(first, world.removeLastTransaction(player));
        assertEquals(Money.ZERO, world.getCurrentBalance(player));

        try {
            world.removeLastTransaction(player);
            fail("Removing a transaction from empty history must fail.");
        } catch (IllegalStateException expected) {
            // Expected.
        }
        assertTrue(world.getTransactions(player).isEmpty());
        assertEquals(Money.ZERO, world.getCurrentBalance(player));
    }

    public void testSparsePlayerIdsMapToStableCollectionIndices() {
        World world = WorldGenerator.minimalWorld();
        Player first = new Player(40, "First");
        Player second = new Player(7, "Second");

        assertEquals(0, world.addPlayer(first));
        assertEquals(1, world.addPlayer(second));
        assertEquals(0, world.getPlayerIndex(40));
        assertEquals(1, world.getPlayerIndex(7));

        world.addTransaction(first,
                new Transaction(TransactionCategory.MISC_INCOME, new Money(25), world.getClock().getCurrentTime()));
        assertEquals(new Money(25), world.getCurrentBalance(first));

        World copy = (World) Utils.cloneBySerialisation(world);
        assertEquals(0, copy.getPlayerIndex(40));
        assertEquals(new Money(25), copy.getCurrentBalance(first));
    }

    public void testDuplicatePlayerIdDoesNotChangePlayerOrAccountState() {
        World world = WorldGenerator.minimalWorld();
        Player first = new Player(40, "First");
        Player second = new Player(7, "Second");
        world.addPlayer(first);
        world.addPlayer(second);
        Transaction transaction = transaction(world, 25);
        world.addTransaction(first, transaction);

        assertDuplicateRejected(() -> world.addPlayer(new Player(40, "Duplicate")));

        assertEquals(List.of(first, second), List.copyOf(world.getPlayers()));
        assertEquals(0, world.getPlayerIndex(40));
        assertEquals(1, world.getPlayerIndex(7));
        assertEquals(2, world.getPlayers().size());
        assertEquals(List.of(transaction), List.copyOf(world.getTransactions(first)));
        assertTrue(world.getTransactions(second).isEmpty());
        assertEquals(new Money(25), world.getCurrentBalance(first));
        assertEquals(Money.ZERO, world.getCurrentBalance(second));

        World copy = (World) Utils.cloneBySerialisation(world);
        assertEquals(List.of(first, second), List.copyOf(copy.getPlayers()));
        assertEquals(0, copy.getPlayerIndex(40));
        assertEquals(1, copy.getPlayerIndex(7));
        assertEquals(new Money(25), copy.getCurrentBalance(copy.getPlayer(0)));
    }

    public void testSparseCargoIdsMapToCollectionIndicesAndBuilderCopiesInput() {
        World base = WorldGenerator.minimalWorld();
        SortedSet<Cargo> cargos = new TreeSet<>();
        cargos.add(new Cargo(7, "Mail", CargoCategory.MAIL, 1));
        World.Builder builder = new World.Builder()
                .setRules(base.getRules())
                .setMapSize(new Vec2D(2, 2))
                .setCargos(cargos);
        World world = builder.build();

        cargos.add(new Cargo(90, "Coal", CargoCategory.BULK_FREIGHT, 2));
        assertEquals(1, world.getCargos().size());
        assertEquals(0, world.getCargoIndex(7));
        assertEquals(7, world.getCargoAtIndex(0).getId());

        try {
            world.getCargos().clear();
            fail("World cargo collection must be read-only.");
        } catch (UnsupportedOperationException expected) {
            // Expected.
        }
    }

    public void testSparseTrackTypeIdsMapToCollectionIndices() {
        World base = WorldGenerator.minimalWorld();
        SortedSet<TrackType> trackTypes = new TreeSet<>();
        trackTypes.add(new TrackType(12, "first", TrackCategory.TRACK, Collections.emptySet(),
                Money.ZERO, Money.ZERO, Collections.emptySet(), Collections.emptySet()));
        trackTypes.add(new TrackType(88, "second", TrackCategory.TRACK, Collections.emptySet(),
                Money.ZERO, Money.ZERO, Collections.emptySet(), Collections.emptySet()));
        World world = new World.Builder()
                .setRules(base.getRules())
                .setMapSize(new Vec2D(2, 2))
                .setTrackTypes(trackTypes)
                .build();

        assertEquals(0, world.getTrackTypeIndex(12));
        assertEquals(1, world.getTrackTypeIndex(88));
        assertEquals(88, world.getTrackTypeAtIndex(1).getId());
    }

    public void testWorldBuilderSnapshotsCataloguesAndRejectsMissingConfiguration() {
        World base = WorldGenerator.minimalWorld();
        SortedSet<Cargo> cargos = new TreeSet<>();
        Cargo cargo = new Cargo(7, "Mail", CargoCategory.MAIL, 1);
        cargos.add(cargo);
        SortedSet<TrackType> trackTypes = new TreeSet<>();
        TrackType trackType = new TrackType(12, "first", TrackCategory.TRACK, Collections.emptySet(),
                Money.ZERO, Money.ZERO, Collections.emptySet(), Collections.emptySet());
        trackTypes.add(trackType);
        World world = new World.Builder()
                .setRules(base.getRules())
                .setMapSize(new Vec2D(2, 2))
                .setCargos(cargos)
                .setTrackTypes(trackTypes)
                .build();

        cargos.add(new Cargo(90, "Coal", CargoCategory.BULK_FREIGHT, 2));
        trackTypes.add(new TrackType(88, "second", TrackCategory.TRACK, Collections.emptySet(),
                Money.ZERO, Money.ZERO, Collections.emptySet(), Collections.emptySet()));
        assertEquals(List.of(cargo), List.copyOf(world.getCargos()));
        assertEquals(List.of(trackType), List.copyOf(world.getTrackTypes()));
        assertUnsupportedMutation(() -> world.getCargos().clear());
        assertUnsupportedMutation(() -> world.getTrackTypes().clear());

        try {
            new World.Builder().setMapSize(new Vec2D(2, 2)).build();
            fail("Builder without rules must fail.");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("Rules"));
        }
        try {
            new World.Builder().setRules(base.getRules()).build();
            fail("Builder without map size must fail.");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("Map size"));
        }
    }

    public void testPopulatedWorldSerializationRoundTripRestoresStateAndIndexes() {
        World base = WorldGenerator.minimalWorld();
        SortedSet<Cargo> cargos = new TreeSet<>();
        cargos.add(new Cargo(7, "Mail", CargoCategory.MAIL, 1));
        cargos.add(new Cargo(90, "Coal", CargoCategory.BULK_FREIGHT, 2));
        SortedSet<TrackType> trackTypes = new TreeSet<>();
        trackTypes.add(new TrackType(12, "first", TrackCategory.TRACK, Collections.emptySet(),
                Money.ZERO, Money.ZERO, Collections.emptySet(), Collections.emptySet()));
        trackTypes.add(new TrackType(88, "second", TrackCategory.TRACK, Collections.emptySet(),
                Money.ZERO, Money.ZERO, Collections.emptySet(), Collections.emptySet()));
        SortedSet<Sentiment> sentiments = new TreeSet<>();
        sentiments.add(new Sentiment(0, "low", 0.5));
        sentiments.add(new Sentiment(1, "steady", 1.0));
        sentiments.add(new Sentiment(2, "high", 1.5));

        World world = new World.Builder()
                .setRules(base.getRules())
                .setMapSize(new Vec2D(3, 3))
                .setCargos(cargos)
                .setTrackTypes(trackTypes)
                .setSentiments(sentiments)
                .build();
        Player firstPlayer = new Player(40, "First");
        Player secondPlayer = new Player(7, "Second");
        assertEquals(0, world.addPlayer(firstPlayer));
        assertEquals(1, world.addPlayer(secondPlayer));
        world.getClock().advanceTimeTo(new Time(15));

        Vec2D trackLocation = new Vec2D(1, 1);
        TrackPiece trackPiece = new TrackPiece(
                TrackConfiguration.getFlatInstance("000010000"), world.getTrackType(88), firstPlayer.getId());
        world.setTile(trackLocation, new TerrainTile(9, trackPiece));
        world.setTile(new Vec2D(2, 2), new TerrainTile(4));

        Train train = new Train(101, 12);
        train.setConsist(List.of(5, 6));
        world.addTrain(firstPlayer, train);
        Station station = new Station(303, new Vec2D(2, 1), "North", world.getCargos().size(), CargoBatchBundle.EMPTY);
        world.addStation(firstPlayer, station);

        Transaction bond = new Transaction(TransactionCategory.BOND, new Money(100), world.getClock().getCurrentTime());
        Transaction income = new Transaction(TransactionCategory.MISC_INCOME, new Money(25), world.getClock().getCurrentTime());
        world.addTransaction(firstPlayer, bond);
        world.addTransaction(firstPlayer, income);

        World copy = (World) Utils.cloneBySerialisation(world);

        assertNotSame(world, copy);
        assertEquals(world, copy);
        Player restoredPlayer = copy.getPlayer(copy.getPlayerIndex(40));
        assertEquals(0, copy.getPlayerIndex(40));
        assertEquals(1, copy.getPlayerIndex(7));
        assertEquals(1, copy.getCargoIndex(90));
        assertEquals(90, copy.getCargoAtIndex(1).getId());
        assertEquals("Coal", copy.getCargo(90).getName());
        assertEquals(1, copy.getTrackTypeIndex(88));
        assertEquals(88, copy.getTrackTypeAtIndex(1).getId());
        assertEquals("second", copy.getTrackType(88).getName());
        assertEquals(trackPiece, copy.getTile(trackLocation).trackPiece());
        assertEquals(9, copy.getTile(trackLocation).terrainTypeId());
        assertEquals(40, copy.getTile(trackLocation).trackPiece().playerId());
        Train restoredTrain = copy.getTrain(restoredPlayer, 101);
        assertEquals(train, restoredTrain);
        assertEquals(12, restoredTrain.getEngine());
        assertEquals(List.of(5, 6), restoredTrain.getConsist());
        Station restoredStation = copy.getStation(restoredPlayer, 303);
        assertEquals(station, restoredStation);
        assertEquals("North", restoredStation.getStationName());
        assertEquals(new Vec2D(2, 1), restoredStation.getLocation());
        assertEquals(bond, copy.getTransaction(restoredPlayer, 0));
        assertEquals(income, copy.getTransaction(restoredPlayer, 1));
        assertEquals(new Money(125), copy.getCurrentBalance(restoredPlayer));
        assertEquals(new Time(15), copy.getClock().getCurrentTime());
        assertEquals(2, copy.getSentiment().getId());
        assertEquals("high", copy.getSentiment().getName());
        assertEquals(1.5, copy.getSentiment().getRate(), 0.0);
    }

    public void testUnknownCatalogueIdsFailWithoutAliasingOtherEntries() {
        World base = WorldGenerator.minimalWorld();
        SortedSet<Cargo> cargos = new TreeSet<>();
        cargos.add(new Cargo(7, "Mail", CargoCategory.MAIL, 1));
        cargos.add(new Cargo(90, "Coal", CargoCategory.BULK_FREIGHT, 2));
        SortedSet<TrackType> trackTypes = new TreeSet<>();
        trackTypes.add(new TrackType(12, "first", TrackCategory.TRACK, Collections.emptySet(),
                Money.ZERO, Money.ZERO, Collections.emptySet(), Collections.emptySet()));
        trackTypes.add(new TrackType(88, "second", TrackCategory.TRACK, Collections.emptySet(),
                Money.ZERO, Money.ZERO, Collections.emptySet(), Collections.emptySet()));
        World world = new World.Builder()
                .setRules(base.getRules())
                .setMapSize(new Vec2D(2, 2))
                .setCargos(cargos)
                .setTrackTypes(trackTypes)
                .build();

        assertEquals(cargos.stream().toList(), List.copyOf(world.getCargos()));
        assertEquals(trackTypes.stream().toList(), List.copyOf(world.getTrackTypes()));
        assertEquals(0, world.getCargoIndex(7));
        assertEquals(1, world.getCargoIndex(90));
        assertEquals(7, world.getCargoAtIndex(0).getId());
        assertEquals(90, world.getCargoAtIndex(1).getId());
        assertEquals(0, world.getTrackTypeIndex(12));
        assertEquals(1, world.getTrackTypeIndex(88));
        assertEquals(12, world.getTrackTypeAtIndex(0).getId());
        assertEquals(88, world.getTrackTypeAtIndex(1).getId());

        World copy = (World) Utils.cloneBySerialisation(world);
        assertEquals(0, copy.getCargoIndex(7));
        assertEquals(1, copy.getCargoIndex(90));
        assertEquals(0, copy.getTrackTypeIndex(12));
        assertEquals(1, copy.getTrackTypeIndex(88));
        assertEquals(List.copyOf(world.getCargos()), List.copyOf(copy.getCargos()));
        assertEquals(List.copyOf(world.getTrackTypes()), List.copyOf(copy.getTrackTypes()));
        assertUnknownId(() -> copy.getCargoIndex(8), "cargo");
        assertUnknownId(() -> copy.getTrackTypeIndex(13), "track type");
    }

    public void testTileReplacementPreservesNeighborsAndRejectsOutOfBoundsWrites() {
        SortedSet<TrackType> trackTypes = new TreeSet<>();
        TrackType trackType = new TrackType(0, "test", TrackCategory.TRACK, Collections.emptySet(),
                Money.ZERO, Money.ZERO, Collections.emptySet(), Collections.emptySet());
        trackTypes.add(trackType);
        World base = WorldGenerator.minimalWorld();
        World world = new World.Builder()
                .setRules(base.getRules())
                .setTrackTypes(trackTypes)
                .setMapSize(new Vec2D(3, 2))
                .build();
        Vec2D target = new Vec2D(1, 1);
        Vec2D neighbor = new Vec2D(1, 0);
        TerrainTile terrainOnly = new TerrainTile(4);
        TerrainTile neighborTile = new TerrainTile(5);
        TrackPiece trackPiece = new TrackPiece(
                TrackConfiguration.getFlatInstance("000010000"), world.getTrackType(0), 0);
        TerrainTile withTrack = new TerrainTile(6, trackPiece);

        world.setTile(target, terrainOnly);
        world.setTile(neighbor, neighborTile);
        assertEquals(terrainOnly, world.getTile(target));
        assertFalse(world.getTile(target).hasTrack());
        assertEquals(neighborTile, world.getTile(neighbor));

        world.setTile(target, withTrack);
        assertEquals(withTrack, world.getTile(target));
        assertEquals(trackPiece, world.getTile(target).trackPiece());
        assertEquals(neighborTile, world.getTile(neighbor));
        assertOutOfBoundsWriteRejected(world, new Vec2D(-1, 0));
        assertOutOfBoundsWriteRejected(world, new Vec2D(3, 0));
        assertOutOfBoundsWriteRejected(world, new Vec2D(0, 2));
    }

    public void testUnmodifiableWorldCollectionsAreReadOnlyAndLive() {
        World world = WorldGenerator.minimalWorld();
        Player player = new Player(0, "Player");
        world.addPlayer(player);
        world.addTrain(player, new Train(1, 0));
        world.addStation(player, new Station(
                2, Vec2D.ZERO, "Station", world.getCargos().size(), CargoBatchBundle.EMPTY));
        world.addTransaction(player, transaction(world, 10));
        UnmodifiableWorld readOnly = world;

        assertUnsupportedMutation(() -> readOnly.getPlayers().clear());
        assertUnsupportedMutation(() -> readOnly.getEngines().clear());
        assertUnsupportedMutation(() -> readOnly.getCities().clear());
        assertUnsupportedMutation(() -> readOnly.getCargos().clear());
        assertUnsupportedMutation(() -> readOnly.getTerrains().clear());
        assertUnsupportedMutation(() -> readOnly.getTrackTypes().clear());
        assertUnsupportedMutation(() -> readOnly.getTrains(player).clear());
        assertUnsupportedMutation(() -> readOnly.getStations(player).clear());
        assertUnsupportedMutation(() -> readOnly.getTransactions(player).clear());

        Collection<Player> playersView = readOnly.getPlayers();
        Collection<Train> trainsView = readOnly.getTrains(player);
        Collection<Station> stationsView = readOnly.getStations(player);
        Collection<Transaction> transactionsView = readOnly.getTransactions(player);
        world.addPlayer(new Player(1, "Second"));
        world.addTrain(player, new Train(3, 0));
        world.addStation(player, new Station(
                4, new Vec2D(1, 0), "Another station", world.getCargos().size(), CargoBatchBundle.EMPTY));
        world.addTransaction(player, transaction(world, 5));
        assertEquals(2, playersView.size());
        assertEquals(2, trainsView.size());
        assertEquals(2, stationsView.size());
        assertEquals(2, transactionsView.size());
    }

    public void testMapDimensionAndTileCountLimits() {
        assertInvalidMapSize(new Vec2D(0, 1));
        assertInvalidMapSize(new Vec2D(1, 0));
        assertInvalidMapSize(new Vec2D(-1, 1));
        assertInvalidMapSize(new Vec2D(1, -1));
        assertInvalidMapSize(new Vec2D(10_001, 1));
        assertInvalidMapSize(new Vec2D(1, 10_001));
        assertInvalidMapSize(new Vec2D(1_000, 1_001));

        assertEquals(new Vec2D(10_000, 1), WorldGenerator.minimalWorld(new Vec2D(10_000, 1)).getMapSize());
        assertEquals(new Vec2D(1, 10_000), WorldGenerator.minimalWorld(new Vec2D(1, 10_000)).getMapSize());
        assertEquals(new Vec2D(1_000, 1_000),
                WorldGenerator.minimalWorld(new Vec2D(1_000, 1_000)).getMapSize());
    }

    private static void assertInvalidMapSize(Vec2D size) {
        try {
            WorldGenerator.minimalWorld(size);
            fail("Expected invalid map size to be rejected: " + size);
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }

    private static void assertDuplicateRejected(Runnable action) {
        try {
            action.run();
            fail("Expected duplicate identifier to be rejected.");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }

    private static void assertUnsupportedMutation(Runnable action) {
        try {
            action.run();
            fail("Expected returned collection to be unmodifiable.");
        } catch (UnsupportedOperationException expected) {
            // Expected.
        }
    }

    private static void assertUnknownId(Runnable action, String type) {
        try {
            action.run();
            fail("Unknown " + type + " ID must fail.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Unknown " + type + " ID"));
        }
    }

    private static void assertOutOfBoundsWriteRejected(World world, Vec2D coordinate) {
        try {
            world.setTile(coordinate, new TerrainTile(99));
            fail("Out-of-bounds tile write must fail: " + coordinate);
        } catch (IndexOutOfBoundsException expected) {
            // Expected.
        }
    }

    private static Transaction transaction(World world, long amount) {
        return new Transaction(TransactionCategory.MISC_INCOME, new Money(amount), world.getClock().getCurrentTime());
    }

    private static World createWorldWithMutableState() {
        World world = WorldGenerator.minimalWorld();
        Player player = new Player(0, "Original");
        world.addPlayer(player);
        world.addTrain(player, new Train(1, 0));
        world.addTransaction(player, new Transaction(
                TransactionCategory.MISC_INCOME, new Money(100), world.getClock().getCurrentTime()));
        world.setTile(Vec2D.ZERO, new TerrainTile(0));
        return world;
    }

}
