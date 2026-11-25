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

package freerails.server;

import freerails.model.ModelConstants;
import freerails.model.economy.Money;
import freerails.model.economy.TransactionUtils;
import freerails.model.economy.Broker;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.MapFixtureFactory2;
import freerails.model.economy.transaction.Transaction;
import freerails.model.terrain.TileTransition;
import freerails.move.generator.AddStationMoveGenerator;
import freerails.move.generator.BuildTrackMoveGenerator;
import freerails.move.generator.RemoveTrackMoveGenerator;
import freerails.model.operations.cargo.CargoBatch;
import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.model.operations.station.Station;
import freerails.model.operations.station.StationSupply;
import freerails.model.player.Player;
import freerails.model.simulation.Time;
import freerails.model.terrain.TerrainTile;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackType;
import freerails.model.world.World;
import freerails.move.Move;
import freerails.move.UpdateStationCargoPolicyMove;
import freerails.util.IntPoint2D;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tests the server model lifecycle and its monthly cargo-at-stations update.
 */
public class FullServerGameModelTest extends TestCase {

    /**
     * Tests that yearly maintenance follows station track: while a station exists
     * it is charged as station track, and after it is removed nothing is charged
     * for it and nothing is paid out.
     */
    public void testMaintenanceFollowsStationTrack() {
        World world = MapFixtureFactory2.getCopy();
        Player player = world.getPlayer(0);
        world.addTransaction(player, new Transaction(TransactionCategory.MISC_INCOME, new Money(1_000_000_000L),
                world.getClock().getCurrentTime()));
        TrackType terminal = world.getTrackTypes().stream()
                .filter(type -> type.getName().equals("terminal")).findFirst().orElseThrow();
        IntPoint2D start = new IntPoint2D(10, 10);
        for (int i = 0; i < 3; i++) {
            applyAll(world, new BuildTrackMoveGenerator(new IntPoint2D(10 + i, 10), TileTransition.EAST, 0, 0, player)
                    .generate(world));
        }
        Money trackOnly = yearlyMaintenance(world, player);

        applyAll(world, new AddStationMoveGenerator(start, terminal.getId(), player).generate(world));
        assertTrue(yearlyMaintenance(world, player).amount() < 0);

        applyAll(world, new RemoveTrackMoveGenerator(start, TileTransition.EAST, player).generate(world));
        Money afterRemoval = yearlyMaintenance(world, player);
        assertTrue("Maintenance must never pay out.", afterRemoval.amount() <= 0);
        assertTrue("The removed track no longer costs maintenance.", afterRemoval.amount() > trackOnly.amount());
    }

    private static Money yearlyMaintenance(World world, Player player) {
        World copy = (World) freerails.util.Utils.cloneBySerialisation(world);
        // Maintenance counts track built before the current time.
        copy.getClock().advanceTime();
        Money before = copy.getCurrentBalance(player);
        List<Move> moves = new ArrayList<>();
        FullServerGameModel.applyTrackMaintenanceMoves(copy, moves::add);
        moves.forEach(move -> move.apply(copy));
        return Money.subtract(copy.getCurrentBalance(player), before);
    }

    private static void applyAll(World world, Move move) {
        assertTrue(move.applicable(world).getMessage(), move.applicable(world).isSuccess());
        move.apply(world);
    }

    /**
     * Tests that the year-end interest charge uses each outstanding bond's own
     * interest rate and leaves out repaid bonds.
     */
    public void testBondInterestUsesEachBondsRate() {
        World world = WorldGenerator.defaultWorld();
        Player player = WorldGenerator.TEST_PLAYER;
        world.addPlayer(player);
        Time now = world.getClock().getCurrentTime();
        for (double rate : Broker.outstandingBondRates(world, player)) {
            world.addTransaction(player, TransactionUtils.repayBond(rate, now));
        }
        world.addTransaction(player, TransactionUtils.issueBond(4, now));
        world.addTransaction(player, TransactionUtils.issueBond(6, now));
        world.addTransaction(player, TransactionUtils.repayBond(6, now));
        world.addTransaction(player, TransactionUtils.issueBond(7, now));

        List<Move> moves = new ArrayList<>();
        FullServerGameModel.applyBondInterestMoves(world, moves::add);

        assertEquals(1, moves.size());
        Money before = world.getCurrentBalance(player);
        moves.getFirst().apply(world);
        // 4% and 7% of the bond value; the repaid 6% bond pays nothing.
        assertEquals(Money.subtract(before, Money.divide(Money.multiply(ModelConstants.BOND_VALUE_ISSUE, 11), 100)),
                world.getCurrentBalance(player));
        assertEquals(TransactionCategory.INTEREST_CHARGE,
                world.getTransaction(player, world.getTransactions(player).size() - 1).getCategory());
    }

    /** Checks that an update before world initialization is harmless. */
    public void testUpdateBeforeWorldIsInitialized() throws InterruptedException {
        FullServerGameModel model = new FullServerGameModel();
        Thread.sleep(2);

        model.update(move -> fail("No moves expected without a world: " + move));
    }

    /**
     * Tests station production records the current simulation time.
     */
    public void testCargoProductionUsesCurrentSimulationTime() {
        World world = WorldGenerator.defaultWorld();
        Player player = WorldGenerator.TEST_PLAYER;
        world.addPlayer(player);
        int cargoTypeId = world.getCargos().iterator().next().getId();
        Station station = new Station(1, new IntPoint2D(4, 5), "Station", CargoBatchBundle.EMPTY);
        station.setSupply(new StationSupply(Map.of(cargoTypeId, 12)));
        world.addStation(player, station);

        Time creationTime = new Time(42);
        world.getClock().advanceTimeTo(creationTime);
        FullServerGameModel.cargoAtStationsUpdate(world, move -> move.apply(world));

        CargoBatch batch = new CargoBatch(cargoTypeId, station.getLocation(), creationTime, station.getId());
        assertTrue(station.getCargoBatchBundle().contains(batch));
        assertTrue(station.getCargoBatchBundle().getAmount(batch) > 0);
    }

    /**
     * Tests that a changed supply and demand policy is sent as a move instead of changing the world directly, and
     * that no move is sent once the policy is up to date.
     */
    public void testSupplyUpdateSendsMoveWithoutChangingWorldDirectly() {
        World world = WorldGenerator.defaultWorld();
        Player player = WorldGenerator.TEST_PLAYER;
        world.addPlayer(player);
        Station station = new Station(1, new IntPoint2D(4, 5), "Station", CargoBatchBundle.EMPTY);
        world.addStation(player, station);
        TrackType stationTrackType = world.getTrackTypes().stream()
                .filter(TrackType::isStation)
                .findFirst()
                .orElseThrow();
        world.setTile(station.getLocation(), new TerrainTile(0,
                new TrackPiece(TrackConfiguration.from8bitTemplate(1), stationTrackType, player.getId())));
        StationSupply supplyBefore = station.getSupply();

        List<Move> moves = new ArrayList<>();
        FullServerGameModel.supplyAtStationsUpdate(world, moves::add);

        assertEquals(1, moves.size());
        assertTrue(moves.get(0) instanceof UpdateStationCargoPolicyMove);
        assertSame("The world must only change through the move.", supplyBefore, station.getSupply());

        moves.get(0).apply(world);
        assertNotSame(supplyBefore, station.getSupply());

        moves.clear();
        FullServerGameModel.supplyAtStationsUpdate(world, moves::add);
        assertTrue("An unchanged policy must not produce a move.", moves.isEmpty());
    }
}
