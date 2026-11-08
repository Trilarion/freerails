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

import freerails.model.operations.cargo.Cargo;
import freerails.model.building.EngineShop;
import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.model.operations.cargo.CargoCategory;
import freerails.model.economy.transaction.Transaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.simulation.Speed;
import freerails.model.game.Sentiment;
import freerails.model.simulation.Time;
import freerails.model.operations.station.Station;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.city.City;
import freerails.model.track.TrackConfiguration;
import freerails.util.IntPoint2D;
import freerails.util.TestUtils;
import freerails.util.Utils;
import freerails.model.economy.*;
import freerails.model.player.Player;
import freerails.model.track.TrackCategory;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackType;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.Engine;
import freerails.model.operations.train.TrainConfiguration;
import freerails.model.operations.train.TrainTemplate;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.io.Serializable;
import java.lang.reflect.Field;
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
     * Tests constructor behavior.
     */
    public void testConstructor() {
        World world = WorldGenerator.minimalWorld();
        assertEquals(world.getMapSize(), new IntPoint2D(20, 20));
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

    /**
     * Tests serialization round trip creates independent mutable world behavior.
     */
    public void testSerializationRoundTripCreatesIndependentMutableWorld() {
        World original = createWorldWithMutableState();
        World copy = (World) Utils.cloneBySerialisation(original);
        Player copiedPlayer = copy.getPlayer(copy.getPlayerIndex(0));
        IntPoint2D location = IntPoint2D.ZERO;
        TerrainTile originalTile = original.getTile(location);

        copy.setTile(location, new TerrainTile(1));
        copy.addTransaction(copiedPlayer, new Transaction(
                TransactionCategory.MISC_INCOME, new Money(50), copy.getClock().getCurrentTime()));
        copy.addTrain(copiedPlayer, trainWithAvailableEngine(copy, 2));
        copy.setSpeed(Speed.FAST);

        assertEquals(originalTile, original.getTile(location));
        assertEquals(1, original.getTransactions(original.getPlayer(0)).size());
        assertEquals(new Money(100), original.getCurrentBalance(original.getPlayer(0)));
        assertEquals(1, original.getTrains(original.getPlayer(0)).size());
        assertEquals(Speed.MODERATE, original.getSpeed());
    }

    /**
     * Tests serialization round trip equals and hash code track world state behavior.
     */
    public void testSerializationRoundTripEqualsAndHashCodeTrackWorldState() {
        World original = createWorldWithMutableState();
        World copy = (World) Utils.cloneBySerialisation(original);

        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());

        IntPoint2D location = IntPoint2D.ZERO;
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
        changed.addTrain(changedPlayer, trainWithAvailableEngine(changed, 2));
        assertFalse(original.equals(changed));

        changed = (World) Utils.cloneBySerialisation(original);
        changed.setSpeed(Speed.FAST);
        assertFalse(original.equals(changed));
    }

    /**
     * Tests engine shops survive world serialization and are player scoped behavior.
     */
    public void testEngineShopsSurviveWorldSerializationAndArePlayerScoped() {
        World world = WorldGenerator.minimalWorld();
        Player player = new Player(0, "One");
        world.addPlayer(player);
        world.addEngineShop(player, new EngineShop(3, new IntPoint2D(4, 5)));
        world.addPlayer(new Player(1, "Two"));

        World copy = (World) Utils.cloneBySerialisation(world);
        Player copiedPlayer = copy.getPlayer(copy.getPlayerIndex(0));
        Player otherCopiedPlayer = copy.getPlayer(copy.getPlayerIndex(1));

        assertEquals(world, copy);
        assertEquals(world.hashCode(), copy.hashCode());
        assertEquals(new IntPoint2D(4, 5), copy.getEngineShop(copiedPlayer, 3).getLocation());
        assertEquals(1, copy.getEngineShops(copiedPlayer).size());
        assertTrue(copy.getEngineShops(otherCopiedPlayer).isEmpty());
        TestUtils.assertThrows(UnsupportedOperationException.class, () -> copy.getEngineShops(copiedPlayer).clear());

        World changed = (World) Utils.cloneBySerialisation(world);
        Player changedPlayer = changed.getPlayer(changed.getPlayerIndex(0));
        changed.getEngineShop(changedPlayer, 3).setProduction(List.of(new TrainTemplate(1, List.of(2))));
        assertFalse(world.equals(changed));
    }

    /**
     * Tests legacy station production migrates to engine shop on load behavior.
     * @throws Exception if the test legacy station production migrates to engine shop on load operation fails.
     */
    public void testLegacyStationProductionMigratesToEngineShopOnLoad() throws Exception {
        World world = WorldGenerator.minimalWorld();
        Player player = new Player(0, "One");
        world.addPlayer(player);
        Station station = new Station(4, new IntPoint2D(4, 5), "Central", CargoBatchBundle.EMPTY);
        world.addStation(player, station);

        Field legacyProduction = Station.class.getDeclaredField("production");
        legacyProduction.setAccessible(true);
        legacyProduction.set(station, List.of(new TrainTemplate(2, List.of(3))));

        World copy = (World) Utils.cloneBySerialisation(world);
        Player copiedPlayer = copy.getPlayer(copy.getPlayerIndex(0));
        assertEquals(1, copy.getEngineShops(copiedPlayer).size());
        EngineShop shop = copy.getEngineShops(copiedPlayer).iterator().next();
        assertEquals(station.getLocation(), shop.getLocation());
        assertEquals(List.of(new TrainTemplate(2, List.of(3))), shop.getProduction());
    }

    /**
     * Tests removing station also removes its engine shop behavior.
     */
    public void testRemovingStationAlsoRemovesItsEngineShop() {
        World world = WorldGenerator.minimalWorld();
        Player player = new Player(0, "One");
        world.addPlayer(player);
        IntPoint2D location = new IntPoint2D(4, 5);
        world.addStation(player, new Station(4, location, "Central", CargoBatchBundle.EMPTY));
        world.addEngineShop(player, new EngineShop(7, location));

        world.removeStation(player, 4);

        assertTrue(world.getEngineShops(player).isEmpty());
    }

    /**
     * Tests equals behavior.
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

        Transaction transaction = new Transaction(TransactionCategory.MISC_INCOME, new Money(100), copy.getClock().getCurrentTime());
        copy.addTransaction(player, transaction);
        assertEquals(new Money(100), copy.getCurrentBalance(player));
        assertFalse(copy.equals(original));
    }

    /**
     * Tests equals2 behavior.
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
     * Tests bounds contain behavior.
     */
    public void testBoundsContain() {
        World world = WorldGenerator.minimalWorld();
        assertTrue(world.boundsContain(new IntPoint2D(1, 1)));
        assertTrue(world.boundsContain(IntPoint2D.ZERO));
        assertFalse(world.boundsContain(new IntPoint2D(-1, -1)));
        assertFalse(world.boundsContain(new IntPoint2D(20, 20)));
    }

    /**
     * Tests non square map bounds agree with tile access and indexing behavior.
     */
    public void testNonSquareMapBoundsAgreeWithTileAccessAndIndexing() {
        World world = WorldGenerator.minimalWorld(new IntPoint2D(3, 5));
        List<IntPoint2D> coordinates = List.of(
                new IntPoint2D(0, 0), new IntPoint2D(0, 4), new IntPoint2D(2, 0), new IntPoint2D(2, 4),
                new IntPoint2D(1, 1), new IntPoint2D(1, 3), new IntPoint2D(2, 2));
        int terrainId = 1;
        for (IntPoint2D coordinate : coordinates) {
            assertTrue(world.boundsContain(coordinate));
            world.setTile(coordinate, new TerrainTile(terrainId++));
        }

        assertEquals(1, world.getTile(new IntPoint2D(0, 0)).terrainTypeId());
        assertEquals(2, world.getTile(new IntPoint2D(0, 4)).terrainTypeId());
        assertEquals(3, world.getTile(new IntPoint2D(2, 0)).terrainTypeId());
        assertEquals(4, world.getTile(new IntPoint2D(2, 4)).terrainTypeId());
        assertEquals(5, world.getTile(new IntPoint2D(1, 1)).terrainTypeId());
        assertEquals(6, world.getTile(new IntPoint2D(1, 3)).terrainTypeId());
        assertEquals(7, world.getTile(new IntPoint2D(2, 2)).terrainTypeId());

        List<IntPoint2D> outsideCoordinates = List.of(
                new IntPoint2D(-1, 0), new IntPoint2D(3, 0), new IntPoint2D(0, -1),
                new IntPoint2D(0, 5), new IntPoint2D(-1, 5), new IntPoint2D(3, 5));
        for (IntPoint2D coordinate : outsideCoordinates) {
            assertFalse(world.boundsContain(coordinate));
            TestUtils.assertThrows(IndexOutOfBoundsException.class, () -> world.getTile(coordinate));
        }
    }

    /**
     * Tests bank account behavior.
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

    /**
     * Tests future transaction timestamps are rejected while historical order
     * remains unrestricted.
     */
    public void testTransactionTimeCannotBeInTheFuture() {
        World world = WorldGenerator.minimalWorld();
        Player player = new Player(0, "Test");
        world.addPlayer(player);

        Transaction future = new Transaction(
                TransactionCategory.MISC_INCOME, new Money(100), Time.ZERO.advance());
        TestUtils.assertThrows(IllegalArgumentException.class,
                () -> world.addTransaction(player, future));
        assertTrue(world.getTransactions(player).isEmpty());
        assertEquals(Money.ZERO, world.getCurrentBalance(player));

        world.getClock().advanceTimeTo(new Time(2));
        Transaction earlier = new Transaction(
                TransactionCategory.MISC_INCOME, new Money(10), Time.ZERO);
        Transaction later = new Transaction(
                TransactionCategory.MISC_INCOME, new Money(20), new Time(1));
        Transaction current = new Transaction(
                TransactionCategory.MISC_INCOME, new Money(30), new Time(2));
        world.addTransaction(player, later);
        world.addTransaction(player, earlier);
        world.addTransaction(player, current);

        assertEquals(List.of(later, earlier, current), List.copyOf(world.getTransactions(player)));
        assertEquals(new Money(60), world.getCurrentBalance(player));
    }

    /**
     * Tests player owned collections are initialized and isolated behavior.
     */
    public void testPlayerOwnedCollectionsAreInitializedAndIsolated() {
        World world = WorldGenerator.defaultWorld();
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

        world.addTrain(first, trainWithAvailableEngine(world, 1));
        world.addStation(first, new Station(
                2, IntPoint2D.ZERO, "First station", CargoBatchBundle.EMPTY));
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

    /**
     * Tests train and station ids are unique per owner behavior.
     */
    public void testTrainAndStationIdsAreUniquePerOwner() {
        World world = WorldGenerator.defaultWorld();
        Player first = new Player(10, "First");
        Player second = new Player(20, "Second");
        world.addPlayer(first);
        world.addPlayer(second);

        Train firstTrain = trainWithAvailableEngine(world, 7);
        Train firstOtherTrain = trainWithAvailableEngine(world, 8);
        Train secondTrain = trainWithAvailableEngine(world, 7);
        world.addTrain(first, firstTrain);
        world.addTrain(first, firstOtherTrain);
        world.addTrain(second, secondTrain);
        assertDuplicateRejected(() -> world.addTrain(first, trainWithAvailableEngine(world, 7)));
        assertEquals(0, world.getTrainIndex(first, 7));
        assertEquals(1, world.getTrainIndex(first, 8));
        assertEquals(List.of(firstTrain, firstOtherTrain), List.copyOf(world.getTrains(first)));
        assertEquals(List.of(secondTrain), List.copyOf(world.getTrains(second)));

        Station firstStation = new Station(
                9, IntPoint2D.ZERO, "First", CargoBatchBundle.EMPTY);
        Station firstOtherStation = new Station(
                10, new IntPoint2D(1, 0), "First other", CargoBatchBundle.EMPTY);
        Station secondStation = new Station(
                9, new IntPoint2D(2, 0), "Second", CargoBatchBundle.EMPTY);
        world.addStation(first, firstStation);
        world.addStation(first, firstOtherStation);
        world.addStation(second, secondStation);
        assertDuplicateRejected(() -> world.addStation(first,
                new Station(9, new IntPoint2D(3, 0), "Duplicate", CargoBatchBundle.EMPTY)));
        assertEquals(2, world.getStations(first).size());
        assertEquals(List.of(secondStation), List.copyOf(world.getStations(second)));

        world.removeTrain(first, 7);
        assertEquals(firstOtherTrain, world.getTrain(first, 8));
        Train lastTrain = trainWithAvailableEngine(world, 11);
        world.addTrain(first, lastTrain);
        assertEquals(1, world.getTrainIndex(first, 11));
        world.removeTrain(first, 8);
        assertEquals(0, world.getTrainIndex(first, 11));
        assertEquals(secondTrain, world.getTrain(second, 7));
        world.removeStation(first, 9);
        assertEquals(firstOtherStation, world.getStation(first, 10));
        assertEquals(secondStation, world.getStation(second, 9));
    }

    /**
     * Tests train IDs are allocated from the lowest unused value rather than the list size.
     */
    public void testAllocateTrainIdUsesLowestAvailableValue() {
        World world = WorldGenerator.defaultWorld();
        Player player = new Player(10, "Player");
        world.addPlayer(player);

        world.addTrain(player, trainWithAvailableEngine(world, 0));
        world.addTrain(player, trainWithAvailableEngine(world, 2));
        world.removeTrain(player, 0);

        assertEquals(0, world.allocateTrainId(player));

        world.addTrain(player, trainWithAvailableEngine(world, 0));
        assertEquals(1, world.allocateTrainId(player));
    }

    /**
     * Tests station IDs are allocated from the lowest unused value rather than the list size.
     */
    public void testAllocateStationIdUsesLowestAvailableValue() {
        World world = WorldGenerator.defaultWorld();
        Player player = new Player(11, "Player");
        world.addPlayer(player);

        world.addStation(player, new Station(0, new IntPoint2D(0, 0), "Station 0", CargoBatchBundle.EMPTY));
        world.addStation(player, new Station(2, new IntPoint2D(1, 0), "Station 2", CargoBatchBundle.EMPTY));
        world.removeStation(player, 0);

        assertEquals(0, world.allocateStationId(player));

        world.addStation(player, new Station(0, new IntPoint2D(0, 1), "Station 0 again", CargoBatchBundle.EMPTY));
        assertEquals(1, world.allocateStationId(player));
    }

    /**
     * Tests trains reject engine and wagon type IDs absent from the world.
     */
    public void testTrainTypeIdsMustExistInWorld() {
        World world = WorldGenerator.defaultWorld();
        Player player = new Player(10, "Player");
        world.addPlayer(player);
        int engineId = world.getEngines().iterator().next().getId();
        int wagonTypeId = world.getCargos().iterator().next().getId();

        Train validTrain = new Train(1, engineId);
        validTrain.setConsist(List.of(wagonTypeId));
        world.addTrain(player, validTrain);

        assertUnknownId(() -> world.addTrain(player, new Train(2, Integer.MAX_VALUE)), "engine");
        Train invalidWagonTrain = new Train(3, engineId);
        invalidWagonTrain.setConsist(List.of(Integer.MAX_VALUE));
        assertUnknownId(() -> world.addTrain(player, invalidWagonTrain), "wagon type");
        TrainConfiguration originalConfiguration = validTrain.getConfiguration();
        assertUnknownId(() -> world.updateTrainConfiguration(
                player, validTrain.getId(), new TrainConfiguration(engineId, List.of(Integer.MAX_VALUE))),
                "wagon type");
        assertEquals(originalConfiguration, validTrain.getConfiguration());
        assertEquals(List.of(validTrain), List.copyOf(world.getTrains(player)));
    }

    /**
     * Tests world entity lookups use stable ids and keep sorted iteration behavior.
     */
    public void testWorldEntityLookupsUseStableIdsAndKeepSortedIteration() {
        World catalog = WorldGenerator.defaultWorld();
        City city = new City(77, "Test city", IntPoint2D.ZERO);
        SortedSet<City> cities = new TreeSet<>();
        cities.add(city);
        SortedSet<Sentiment> sentiments = new TreeSet<>();
        sentiments.add(catalog.getSentiment());

        World world = new World.Builder()
                .setRules(catalog.getRules())
                .setMapSize(catalog.getMapSize())
                .setEngines(new TreeSet<>(catalog.getEngines()))
                .setCities(cities)
                .setCargos(new TreeSet<>(catalog.getCargos()))
                .setTerrainTypes(new TreeSet<>(catalog.getTerrains()))
                .setTrackTypes(new TreeSet<>(catalog.getTrackTypes()))
                .setSentiments(sentiments)
                .build();

        for (var engine : world.getEngines()) {
            assertSame(engine, world.getEngine(engine.getId()));
        }
        assertSame(city, world.getCity(city.getId()));
        for (var cargo : world.getCargos()) {
            assertSame(cargo, world.getCargo(cargo.getId()));
        }
        for (var terrain : world.getTerrains()) {
            assertSame(terrain, world.getTerrain(terrain.getId()));
        }
        for (var trackType : world.getTrackTypes()) {
            assertSame(trackType, world.getTrackType(trackType.getId()));
        }
        assertSame(catalog.getSentiment(), world.getSentiment());
        assertEquals(world.getEngines().stream().map(engine -> engine.getId()).sorted().toList(),
                world.getEngines().stream().map(engine -> engine.getId()).toList());
        assertEquals(world.getCities().stream().map(City::getId).sorted().toList(),
                world.getCities().stream().map(City::getId).toList());

        World copy = (World) Utils.cloneBySerialisation(world);
        for (var engine : copy.getEngines()) {
            assertSame(engine, copy.getEngine(engine.getId()));
        }
        for (var cityCopy : copy.getCities()) {
            assertSame(cityCopy, copy.getCity(cityCopy.getId()));
        }
        for (var cargo : copy.getCargos()) {
            assertSame(cargo, copy.getCargo(cargo.getId()));
        }
        for (var terrain : copy.getTerrains()) {
            assertSame(terrain, copy.getTerrain(terrain.getId()));
        }
        for (var trackType : copy.getTrackTypes()) {
            assertSame(trackType, copy.getTrackType(trackType.getId()));
        }
        assertEquals(world.getSentiment().getId(), copy.getSentiment().getId());
    }

    /**
     * Tests transaction append is ordered and overflow is atomic behavior.
     */
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
        TestUtils.assertThrows(ArithmeticException.class, () -> overflowWorld.addTransaction(overflowPlayer, overflowing));
        assertEquals(List.of(maximum), List.copyOf(overflowWorld.getTransactions(overflowPlayer)));
        assertEquals(new Money(Long.MAX_VALUE), overflowWorld.getCurrentBalance(overflowPlayer));
    }

    /**
     * Tests undoing transactions restores prior account state behavior.
     */
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

        TestUtils.assertThrows(IllegalStateException.class, () -> world.removeLastTransaction(player));
        assertTrue(world.getTransactions(player).isEmpty());
        assertEquals(Money.ZERO, world.getCurrentBalance(player));
    }

    /**
     * Tests sparse player ids map to stable collection indices behavior.
     */
    public void testSparsePlayerIdsMapToStableCollectionIndices() {
        World world = WorldGenerator.defaultWorld();
        Player first = new Player(40, "First");
        Player second = new Player(7, "Second");

        assertEquals(0, world.addPlayer(first));
        assertEquals(1, world.addPlayer(second));
        assertEquals(0, world.getPlayerIndex(40));
        assertEquals(1, world.getPlayerIndex(7));

        world.addTransaction(first,
                new Transaction(TransactionCategory.MISC_INCOME, new Money(25), world.getClock().getCurrentTime()));
        world.addTrain(first, trainWithAvailableEngine(world, 101));
        Station firstStation = new Station(
                202, IntPoint2D.ZERO, "First", CargoBatchBundle.EMPTY);
        world.addStation(first, firstStation);
        world.addTransaction(second,
                new Transaction(TransactionCategory.MISC_INCOME, new Money(70), world.getClock().getCurrentTime()));
        Player firstReference = new Player(40, "Another reference to First");

        assertEquals(new Money(25), world.getCurrentBalance(first));
        assertEquals(new Money(25), world.getCurrentBalance(firstReference));
        assertEquals(List.of(trainWithAvailableEngine(world, 101)), List.copyOf(world.getTrains(firstReference)));
        assertEquals(List.of(firstStation), List.copyOf(world.getStations(firstReference)));
        assertEquals(new Money(70), world.getCurrentBalance(second));
        assertTrue(world.getTrains(second).isEmpty());
        assertTrue(world.getStations(second).isEmpty());
        assertEquals(List.of(first, second), List.copyOf(world.getPlayers()));
        assertEquals(List.of(first.getId(), second.getId()),
                world.getPlayers().stream().map(Player::getId).toList());

        World copy = (World) Utils.cloneBySerialisation(world);
        assertEquals(0, copy.getPlayerIndex(40));
        assertEquals(1, copy.getPlayerIndex(7));
        assertEquals(new Money(25), copy.getCurrentBalance(firstReference));
        assertEquals(new Money(70), copy.getCurrentBalance(second));
        assertEquals(List.of(trainWithAvailableEngine(copy, 101)), List.copyOf(copy.getTrains(firstReference)));
        assertEquals(List.of(firstStation), List.copyOf(copy.getStations(firstReference)));
    }

    /**
     * Tests duplicate player id does not change player or account state behavior.
     */
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

    /**
     * Tests sparse cargo ids map to collection indices and builder copies input behavior.
     */
    public void testSparseCargoIdsMapToCollectionIndicesAndBuilderCopiesInput() {
        World base = WorldGenerator.minimalWorld();
        SortedSet<Cargo> cargos = new TreeSet<>();
        cargos.add(new Cargo(7, "Mail", CargoCategory.MAIL, 1));
        World.Builder builder = new World.Builder()
                .setRules(base.getRules())
                .setMapSize(new IntPoint2D(2, 2))
                .setTerrainTypes(new TreeSet<>(base.getTerrains()))
                .setCargos(cargos);
        cargos.add(new Cargo(90, "Coal", CargoCategory.BULK_FREIGHT, 2));
        World world = builder.build();

        assertEquals(1, world.getCargos().size());
        assertEquals(0, world.getCargoIndex(7));
        assertEquals(7, world.getCargoAtIndex(0).getId());

        TestUtils.assertThrows(UnsupportedOperationException.class, () -> world.getCargos().clear());
    }

    /**
     * Tests sparse track type ids map to collection indices behavior.
     */
    public void testSparseTrackTypeIdsMapToCollectionIndices() {
        World base = WorldGenerator.minimalWorld();
        SortedSet<TrackType> trackTypes = new TreeSet<>();
        trackTypes.add(new TrackType(12, "first", TrackCategory.TRACK, Collections.emptySet(),
                Money.ZERO, Money.ZERO, Collections.emptySet(), Collections.emptySet()));
        trackTypes.add(new TrackType(88, "second", TrackCategory.TRACK, Collections.emptySet(),
                Money.ZERO, Money.ZERO, Collections.emptySet(), Collections.emptySet()));
        World world = new World.Builder()
                .setRules(base.getRules())
                .setMapSize(new IntPoint2D(2, 2))
                .setTerrainTypes(new TreeSet<>(base.getTerrains()))
                .setTrackTypes(trackTypes)
                .build();

        assertEquals(0, world.getTrackTypeIndex(12));
        assertEquals(1, world.getTrackTypeIndex(88));
        assertEquals(88, world.getTrackTypeAtIndex(1).getId());
    }

    /**
     * Tests world builder snapshots catalogues and rejects missing configuration behavior.
     */
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
                .setMapSize(new IntPoint2D(2, 2))
                .setTerrainTypes(new TreeSet<>(base.getTerrains()))
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

        IllegalStateException missingRules = TestUtils.assertThrows(IllegalStateException.class,
                () -> new World.Builder().setMapSize(new IntPoint2D(2, 2)).build());
        assertTrue(missingRules.getMessage().contains("Rules"));
        IllegalStateException missingMapSize = TestUtils.assertThrows(IllegalStateException.class,
                () -> new World.Builder().setRules(base.getRules()).build());
        assertTrue(missingMapSize.getMessage().contains("Map size"));

        SortedSet<Terrain> terrainWithoutZero = new TreeSet<>(base.getTerrains());
        terrainWithoutZero.remove(base.getTerrain(0));
        TestUtils.assertThrows(IllegalArgumentException.class,
                () -> new World.Builder()
                        .setRules(base.getRules())
                        .setMapSize(new IntPoint2D(2, 2))
                        .setTerrainTypes(terrainWithoutZero)
                        .build());
    }

    /**
     * Tests populated world serialization round trip restores state and indexes behavior.
     */
    public void testPopulatedWorldSerializationRoundTripRestoresStateAndIndexes() {
        World base = WorldGenerator.minimalWorld();
        SortedSet<Engine> engines = new TreeSet<>();
        engines.add(new Engine(12, "Test engine", Money.ZERO, Money.ZERO, 10, 10));
        SortedSet<Cargo> cargos = new TreeSet<>();
        cargos.add(new Cargo(5, "Passengers", CargoCategory.PASSENGER, 1));
        cargos.add(new Cargo(6, "Goods", CargoCategory.FAST_FREIGHT, 2));
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
                .setMapSize(new IntPoint2D(3, 3))
                .setEngines(engines)
                .setCargos(cargos)
                .setTerrainTypes(new TreeSet<>(base.getTerrains()))
                .setTrackTypes(trackTypes)
                .setSentiments(sentiments)
                .build();
        Player firstPlayer = new Player(40, "First");
        Player secondPlayer = new Player(7, "Second");
        assertEquals(0, world.addPlayer(firstPlayer));
        assertEquals(1, world.addPlayer(secondPlayer));
        world.getClock().advanceTimeTo(new Time(15));

        IntPoint2D trackLocation = new IntPoint2D(1, 1);
        TrackPiece trackPiece = new TrackPiece(
                TrackConfiguration.getFlatInstance("00000001"), world.getTrackType(88), firstPlayer.getId());
        world.setTile(trackLocation, new TerrainTile(9, trackPiece));
        world.setTile(new IntPoint2D(2, 2), new TerrainTile(4));

        Train train = new Train(101, 12);
        train.setConsist(List.of(5, 6));
        world.addTrain(firstPlayer, train);
        Station station = new Station(303, new IntPoint2D(2, 1), "North", CargoBatchBundle.EMPTY);
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
        assertEquals(3, copy.getCargoIndex(90));
        assertEquals(90, copy.getCargoAtIndex(3).getId());
        assertEquals("Coal", copy.getCargo(90).getName());
        assertEquals(1, copy.getTrackTypeIndex(88));
        assertEquals(88, copy.getTrackTypeAtIndex(1).getId());
        assertEquals("second", copy.getTrackType(88).getName());
        assertEquals(trackPiece, copy.getTile(trackLocation).trackPiece());
        assertEquals(9, copy.getTile(trackLocation).terrainTypeId());
        assertEquals(40, copy.getTile(trackLocation).trackPiece().playerId());
        Train restoredTrain = copy.getTrain(restoredPlayer, 101);
        assertEquals(train, restoredTrain);
        assertEquals(0, copy.getTrainIndex(restoredPlayer, 101));
        assertEquals(12, restoredTrain.getEngine());
        assertEquals(List.of(5, 6), restoredTrain.getConsist());
        Station restoredStation = copy.getStation(restoredPlayer, 303);
        assertEquals(station, restoredStation);
        assertEquals("North", restoredStation.getStationName());
        assertEquals(new IntPoint2D(2, 1), restoredStation.getLocation());
        assertEquals(bond, copy.getTransaction(restoredPlayer, 0));
        assertEquals(income, copy.getTransaction(restoredPlayer, 1));
        assertEquals(new Money(125), copy.getCurrentBalance(restoredPlayer));
        assertEquals(new Time(15), copy.getClock().getCurrentTime());
        assertEquals(2, copy.getSentiment().getId());
        assertEquals("high", copy.getSentiment().getName());
        assertEquals(1.5, copy.getSentiment().getRate(), 0.0);
    }

    /**
     * Tests unknown catalogue ids fail without aliasing other entries behavior.
     */
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
                .setMapSize(new IntPoint2D(2, 2))
                .setTerrainTypes(new TreeSet<>(base.getTerrains()))
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

    /**
     * Tests tile replacement preserves neighbors and rejects out of bounds writes behavior.
     */
    public void testTileReplacementPreservesNeighborsAndRejectsOutOfBoundsWrites() {
        SortedSet<TrackType> trackTypes = new TreeSet<>();
        TrackType trackType = new TrackType(0, "test", TrackCategory.TRACK, Collections.emptySet(),
                Money.ZERO, Money.ZERO, Collections.emptySet(), Collections.emptySet());
        trackTypes.add(trackType);
        World base = WorldGenerator.minimalWorld();
        World world = new World.Builder()
                .setRules(base.getRules())
                .setTrackTypes(trackTypes)
                .setMapSize(new IntPoint2D(3, 2))
                .setTerrainTypes(new TreeSet<>(base.getTerrains()))
                .build();
        IntPoint2D target = new IntPoint2D(1, 1);
        IntPoint2D neighbor = new IntPoint2D(1, 0);
        TerrainTile terrainOnly = new TerrainTile(4);
        TerrainTile neighborTile = new TerrainTile(5);
        TrackPiece trackPiece = new TrackPiece(
                TrackConfiguration.getFlatInstance("00000001"), world.getTrackType(0), 0);
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
        assertOutOfBoundsWriteRejected(world, new IntPoint2D(-1, 0));
        assertOutOfBoundsWriteRejected(world, new IntPoint2D(3, 0));
        assertOutOfBoundsWriteRejected(world, new IntPoint2D(0, 2));
    }

    /**
     * Tests unmodifiable world collections are read only and live behavior.
     */
    public void testUnmodifiableWorldCollectionsAreReadOnlyAndLive() {
        World world = WorldGenerator.defaultWorld();
        Player player = new Player(0, "Player");
        world.addPlayer(player);
        world.addTrain(player, trainWithAvailableEngine(world, 1));
        world.addStation(player, new Station(
                2, IntPoint2D.ZERO, "Station", CargoBatchBundle.EMPTY));
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
        world.addTrain(player, trainWithAvailableEngine(world, 3));
        world.addStation(player, new Station(
                4, new IntPoint2D(1, 0), "Another station", CargoBatchBundle.EMPTY));
        world.addTransaction(player, transaction(world, 5));
        assertEquals(2, playersView.size());
        assertEquals(2, trainsView.size());
        assertEquals(2, stationsView.size());
        assertEquals(2, transactionsView.size());
    }

    /**
     * Tests map dimension and tile count limits behavior.
     */
    public void testMapDimensionAndTileCountLimits() {
        assertInvalidMapSize(new IntPoint2D(0, 1));
        assertInvalidMapSize(new IntPoint2D(1, 0));
        assertInvalidMapSize(new IntPoint2D(-1, 1));
        assertInvalidMapSize(new IntPoint2D(1, -1));
        assertInvalidMapSize(new IntPoint2D(10_001, 1));
        assertInvalidMapSize(new IntPoint2D(1, 10_001));
        assertInvalidMapSize(new IntPoint2D(1_000, 1_001));

        assertEquals(new IntPoint2D(10_000, 1), WorldGenerator.minimalWorld(new IntPoint2D(10_000, 1)).getMapSize());
        assertEquals(new IntPoint2D(1, 10_000), WorldGenerator.minimalWorld(new IntPoint2D(1, 10_000)).getMapSize());
        assertEquals(new IntPoint2D(1_000, 1_000),
                WorldGenerator.minimalWorld(new IntPoint2D(1_000, 1_000)).getMapSize());
    }

    /**
     * Asserts invalid map size.
     * @param size the size to use in the test.
     */
    private static void assertInvalidMapSize(IntPoint2D size) {
        try {
            WorldGenerator.minimalWorld(size);
            fail("Expected invalid map size to be rejected: " + size);
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }

    /**
     * Asserts duplicate rejected.
     * @param action the action to use in the test.
     */
    private static void assertDuplicateRejected(Runnable action) {
        TestUtils.assertThrows(IllegalArgumentException.class, action::run);
    }

    /**
     * Asserts unsupported mutation.
     * @param action the action to use in the test.
     */
    private static void assertUnsupportedMutation(Runnable action) {
        TestUtils.assertThrows(UnsupportedOperationException.class, action::run);
    }

    /**
     * Asserts unknown id.
     * @param action the action to use in the test.
     * @param type the type to use in the test.
     */
    private static void assertUnknownId(Runnable action, String type) {
        IllegalArgumentException exception = TestUtils.assertThrows(IllegalArgumentException.class, action::run);
        assertTrue(exception.getMessage().contains("Unknown " + type + " ID"));
    }

    /**
     * Asserts out of bounds write rejected.
     * @param world the world state used by the test.
     * @param coordinate the coordinate to use in the test.
     */
    private static void assertOutOfBoundsWriteRejected(World world, IntPoint2D coordinate) {
        try {
            world.setTile(coordinate, new TerrainTile(99));
            fail("Out-of-bounds tile write must fail: " + coordinate);
        } catch (IndexOutOfBoundsException expected) {
            // Expected.
        }
    }

    /**
     * Performs transaction.
     * @param world the world state used by the test.
     * @param amount the amount to use in the test.
     * @return the result of transaction.
     */
    private static Transaction transaction(World world, long amount) {
        return new Transaction(TransactionCategory.MISC_INCOME, new Money(amount), world.getClock().getCurrentTime());
    }

    /**
     * Creates world with mutable state.
     * @return the result of create world with mutable state.
     */
    private static World createWorldWithMutableState() {
        World world = WorldGenerator.defaultWorld();
        Player player = new Player(0, "Original");
        world.addPlayer(player);
        world.addTrain(player, trainWithAvailableEngine(world, 1));
        world.addTransaction(player, new Transaction(
                TransactionCategory.MISC_INCOME, new Money(100), world.getClock().getCurrentTime()));
        world.setTile(IntPoint2D.ZERO, new TerrainTile(0));
        return world;
    }

    /**
     * Creates a train using an engine type defined in the supplied world.
     *
     * @param world world that provides the engine type
     * @param trainId identifier for the train
     * @return train with a valid engine type
     */
    private static Train trainWithAvailableEngine(World world, int trainId) {
        int engineId = world.getEngines().iterator().next().getId();
        return new Train(trainId, engineId);
    }

}
