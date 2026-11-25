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

import freerails.model.MapFixtureFactory2;
import freerails.model.ModelConstants;
import freerails.model.economy.Money;
import freerails.model.economy.transaction.Transaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.economy.transaction.aggregator.ItemsTransactionAggregator;
import freerails.model.game.Rules;
import freerails.model.player.Player;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainCategory;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackCategory;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackProperty;
import freerails.model.track.TrackType;
import freerails.model.world.World;
import freerails.move.Move;
import freerails.move.Status;
import freerails.util.IntPoint2D;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Tests that the recorded track quantities always match the track on the map,
 * booked on the track owner, and the prices of track type changes.
 */
public class TrackBookkeepingTest extends TestCase {

    private static final Money PLENTY = new Money(1_000_000_000L);

    private World world;
    private Player player;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy(new Rules(true, false, true));
        player = world.getPlayer(0);
        setBalance(player, PLENTY);
    }

    /**
     * Tests building a station on track and removing it again: the station track
     * counts as station track while it exists, and nothing remains afterwards.
     */
    public void testStationCycle() {
        IntPoint2D start = new IntPoint2D(10, 10);
        TrackType terminal = type("terminal");
        for (int i = 0; i < 3; i++) {
            apply(new BuildTrackMoveGenerator(new IntPoint2D(10 + i, 10), TileTransition.EAST,
                    0, 0, player).generate(world));
            assertBookkeepingMatchesMap();
        }

        apply(new AddStationMoveGenerator(start, terminal.getId(), player).generate(world));
        assertBookkeepingMatchesMap();
        assertTrue(recorded(player, terminal) > 0);

        apply(new RemoveTrackMoveGenerator(start, TileTransition.EAST, player).generate(world));
        assertBookkeepingMatchesMap();
        assertEquals(0, recorded(player, terminal));
    }

    /**
     * Tests that an upgrade costs the difference in purchasing price, that a
     * downgrade is free, and that the length moves between the types.
     */
    public void testUpgradeCostsThePriceDifference() {
        IntPoint2D from = new IntPoint2D(5, 5);
        TrackType standard = world.getTrackType(0);
        TrackType expensive = world.getTrackTypes().stream()
                .filter(type -> type.getCategory() == TrackCategory.TRACK)
                .filter(type -> type.getPurchasingPrice().amount() > standard.getPurchasingPrice().amount())
                .filter(type -> type.trackPieceIsLegal(TrackPiece.create(TileTransition.EAST, standard, 0).trackConfiguration()))
                .findFirst().orElseThrow();
        apply(new BuildTrackMoveGenerator(from, TileTransition.EAST, standard.getId(), standard.getId(), player)
                .generate(world));
        int length = world.getTile(from).trackPiece().trackConfiguration().getLength();

        Money before = world.getCurrentBalance(player);
        apply(new UpgradeTrackMoveGenerator(from, expensive.getId(), player).generate(world));
        long difference = expensive.getPurchasingPrice().amount() - standard.getPurchasingPrice().amount();
        assertEquals(Money.subtract(before, new Money(difference * length / ModelConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE)),
                world.getCurrentBalance(player));
        assertBookkeepingMatchesMap();

        before = world.getCurrentBalance(player);
        apply(new UpgradeTrackMoveGenerator(from, standard.getId(), player).generate(world));
        assertEquals(before, world.getCurrentBalance(player));
        assertBookkeepingMatchesMap();
    }

    /**
     * Tests that changing another player's track books the length on the owner,
     * while the acting player pays or is refunded.
     */
    public void testOtherPlayersTrackIsBookedOnTheOwner() {
        Player other = world.getPlayer(1);
        setBalance(other, PLENTY);
        IntPoint2D from = new IntPoint2D(5, 5);
        IntPoint2D shared = new IntPoint2D(6, 5);
        apply(new BuildTrackMoveGenerator(from, TileTransition.EAST, 0, 0, player).generate(world));
        Money ownerBalance = world.getCurrentBalance(player);
        Money otherBalance = world.getCurrentBalance(other);

        // The other player connects to the owner's track.
        apply(new BuildTrackMoveGenerator(shared, TileTransition.EAST, 0, 0, other).generate(world));
        assertBookkeepingMatchesMap();
        assertEquals(ownerBalance, world.getCurrentBalance(player));
        assertTrue(world.getCurrentBalance(other).amount() < otherBalance.amount());

        // The other player removes the owner's track and gets the refund.
        otherBalance = world.getCurrentBalance(other);
        apply(new RemoveTrackMoveGenerator(from, TileTransition.EAST, other).generate(world));
        assertBookkeepingMatchesMap();
        assertEquals(ownerBalance, world.getCurrentBalance(player));
        assertTrue(world.getCurrentBalance(other).amount() > otherBalance.amount());
    }

    /**
     * Tests with a station type far more expensive than track: building a station
     * costs the price difference and the fixed charge, and building and removing
     * stations never makes money.
     */
    public void testExpensiveStationsCostThePriceDifference() {
        TrackType track = new TrackType(0, "track", TrackCategory.TRACK, EnumSet.of(TrackProperty.SINGLE),
                new Money(1000), new Money(10_000), EnumSet.of(TerrainCategory.COUNTRY), templates());
        TrackType station = new TrackType(1, "station", TrackCategory.STATION, EnumSet.of(TrackProperty.SINGLE),
                new Money(2000), new Money(1_000_000), EnumSet.of(TerrainCategory.COUNTRY), templates());
        track.prepare();
        station.prepare();
        SortedSet<TrackType> trackTypes = new TreeSet<>(List.of(track, station));
        SortedSet<Terrain> terrains = new TreeSet<>(List.of(new Terrain(0, "Clear", TerrainCategory.COUNTRY,
                Money.ZERO, null, List.of(), List.of(), List.of())));
        world = new World.Builder().setMapSize(new IntPoint2D(4, 1)).setTerrainTypes(terrains)
                .setTrackTypes(trackTypes).setRules(new Rules(false, false)).build();
        player = new Player(0, "player");
        world.addPlayer(player);
        for (int x = 0; x < 4; x++) {
            world.setTile(new IntPoint2D(x, 0), new TerrainTile(0));
        }
        setBalance(player, PLENTY);
        Money start = world.getCurrentBalance(player);
        IntPoint2D first = IntPoint2D.ZERO;

        apply(new BuildTrackMoveGenerator(first, TileTransition.EAST, 0, 0, player).generate(world));
        int length = world.getTile(first).trackPiece().trackConfiguration().getLength();
        Money beforeStation = world.getCurrentBalance(player);
        apply(new AddStationMoveGenerator(first, station.getId(), player).generate(world));
        long difference = (1_000_000 - 10_000) * (long) length / ModelConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE;
        assertEquals(Money.subtract(beforeStation, new Money(difference + 2000)), world.getCurrentBalance(player));
        assertBookkeepingMatchesMap();

        apply(new RemoveTrackMoveGenerator(first, TileTransition.EAST, player).generate(world));
        assertBookkeepingMatchesMap();
        assertTrue("Building and removing a station must not make money.",
                world.getCurrentBalance(player).amount() < start.amount());
    }

    private static HashSet<String> templates() {
        return new HashSet<>(Arrays.asList("00010000", "00010001", "01000100", "00000001"));
    }

    /**
     * Asserts that, for every player and track type, the recorded quantity equals
     * the length of that type on the tiles the player owns.
     */
    private void assertBookkeepingMatchesMap() {
        Map<Integer, Map<Integer, Integer>> onMap = new HashMap<>();
        for (int x = 0; x < world.getMapSize().x(); x++) {
            for (int y = 0; y < world.getMapSize().y(); y++) {
                TrackPiece piece = world.getTile(new IntPoint2D(x, y)).trackPiece();
                if (piece != null) {
                    onMap.computeIfAbsent(piece.playerId(), id -> new HashMap<>())
                            .merge(piece.trackType().getId(), piece.trackConfiguration().getLength(), Integer::sum);
                }
            }
        }
        for (Player owner : world.getPlayers()) {
            for (TrackType trackType : world.getTrackTypes()) {
                int expected = onMap.getOrDefault(owner.getId(), Map.of()).getOrDefault(trackType.getId(), 0);
                assertEquals(owner.getName() + ", " + trackType.getName(), expected, recorded(owner, trackType));
            }
        }
    }

    private int recorded(Player owner, TrackType trackType) {
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, owner);
        aggregator.setCategory(TransactionCategory.TRACK);
        aggregator.setType(trackType.getId());
        return aggregator.calculateQuantity();
    }

    private TrackType type(String name) {
        return world.getTrackTypes().stream().filter(type -> type.getName().equals(name)).findFirst().orElseThrow();
    }

    private void apply(Move move) {
        Status status = move.applicable(world);
        assertTrue(status.getMessage(), status.isSuccess());
        move.apply(world);
    }

    private void setBalance(Player owner, Money balance) {
        Money change = Money.subtract(balance, world.getCurrentBalance(owner));
        world.addTransaction(owner, new Transaction(TransactionCategory.MISC_INCOME, change,
                world.getClock().getCurrentTime()));
    }
}
