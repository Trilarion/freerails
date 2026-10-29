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

package freerails.move;

import freerails.model.cargo.CargoBatch;
import freerails.model.cargo.CargoBatchBundle;
import freerails.model.finance.Money;
import freerails.model.finance.transaction.Transaction;
import freerails.model.finance.transaction.TransactionCategory;
import freerails.model.game.Rules;
import freerails.model.game.Time;
import freerails.model.MapFixtureFactory2;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.terrain.TerrainTile;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackPiece;
import freerails.model.train.Train;
import freerails.model.world.World;
import freerails.move.mapupdatemove.ChangeTileMove;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.util.Vec2D;
import junit.framework.TestCase;

public class MoveStateContractTest extends TestCase {

    private static final Vec2D TILE_TO_CHANGE = new Vec2D(10, 10);
    private static final Vec2D UNRELATED_TILE = new Vec2D(11, 10);
    private Player player;
    private Player otherPlayer;
    private World world;

    @Override
    protected void setUp() {
        world = MapFixtureFactory2.getCopy();
        player = world.getPlayer(0);
        otherPlayer = world.getPlayer(1);
        world.addTrain(player, new Train(4, 0));
        world.addTrain(otherPlayer, new Train(8, 0));
        world.addStation(player, station(10, 2));
        world.addStation(player, station(11, 3));
        world.addStation(otherPlayer, station(10, 4));
        world.addTransaction(otherPlayer, transaction(50));
    }

    public void testApplicableIsPureForAcceptedAndRejectedMoves() {
        CargoBatchBundle cargo = cargo(3);
        ChangeTrackPieceMove outsideMap = new ChangeTrackPieceMove(null, null, new Vec2D(-1, 0));
        world.setRules(new Rules(false, false));
        TrackPiece before = new TrackPiece(
                TrackConfiguration.getFlatInstance("000010000"), world.getTrackType(0), player.getId());
        TrackPiece after = new TrackPiece(
                TrackConfiguration.getFlatInstance("000010000"), world.getTrackType(0), otherPlayer.getId());
        TerrainTile tile = world.getTile(TILE_TO_CHANGE);
        world.setTile(TILE_TO_CHANGE, new TerrainTile(tile.terrainTypeId(), before));
        ChangeTrackPieceMove disallowedTrack = new ChangeTrackPieceMove(before, after, TILE_TO_CHANGE);

        assertApplicableWithoutMutation(new AddTransactionMove(player, transaction(20)), true);
        assertApplicableWithoutMutation(new ChangeCargoAtStationMove(player, 10, cargo), true);
        assertApplicableWithoutMutation(new ChangeCargoAtStationMove(player, 999, cargo), false);
        assertApplicableWithoutMutation(new UpdateTrainMove(player, 999, null, java.util.List.of(), null), false);
        assertApplicableWithoutMutation(
                new AddTransactionMove(player, transaction(-1_000_000_000_000L), true), false);
        assertApplicableWithoutMutation(outsideMap, false);
        assertApplicableWithoutMutation(disallowedTrack, false);
    }

    public void testApplyingTransactionChangesOnlyItsPlayersBalance() {
        Money playerBalance = world.getCurrentBalance(player);
        Money otherPlayerBalance = world.getCurrentBalance(otherPlayer);
        TerrainTile unrelatedTile = world.getTile(UNRELATED_TILE);
        Train otherTrain = world.getTrain(otherPlayer, 8);

        new AddTransactionMove(player, transaction(25)).apply(world);

        assertEquals(Money.add(playerBalance, new Money(25)), world.getCurrentBalance(player));
        assertEquals(otherPlayerBalance, world.getCurrentBalance(otherPlayer));
        assertEquals(unrelatedTile, world.getTile(UNRELATED_TILE));
        assertEquals(otherTrain, world.getTrain(otherPlayer, 8));
    }

    public void testApplyingTileMoveLeavesOtherAggregatesUnchanged() {
        TerrainTile tileBefore = world.getTile(TILE_TO_CHANGE);
        TerrainTile unrelatedTile = world.getTile(UNRELATED_TILE);
        Money playerBalance = world.getCurrentBalance(player);
        Money otherPlayerBalance = world.getCurrentBalance(otherPlayer);
        Train playerTrain = world.getTrain(player, 4);
        Train otherTrain = world.getTrain(otherPlayer, 8);
        ChangeTileMove move = new ChangeTileMove(
                new TerrainTile(5, tileBefore.trackPiece()), TILE_TO_CHANGE);

        assertTrue(move.applicable(world).isSuccess());
        move.apply(world);

        assertEquals(5, world.getTile(TILE_TO_CHANGE).terrainTypeId());
        assertEquals(tileBefore.trackPiece(), world.getTile(TILE_TO_CHANGE).trackPiece());
        assertEquals(unrelatedTile, world.getTile(UNRELATED_TILE));
        assertEquals(playerBalance, world.getCurrentBalance(player));
        assertEquals(otherPlayerBalance, world.getCurrentBalance(otherPlayer));
        assertEquals(playerTrain, world.getTrain(player, 4));
        assertEquals(otherTrain, world.getTrain(otherPlayer, 8));
    }

    public void testApplyingCargoMoveChangesOnlyTheTargetStation() {
        CargoBatchBundle replacement = cargo(7);
        CargoBatchBundle otherStationCargo = cargo(9);
        world.getStation(player, 11).setCargoBatchBundle(otherStationCargo);
        world.getStation(otherPlayer, 10).setCargoBatchBundle(cargo(11));
        Money playerBalance = world.getCurrentBalance(player);
        Money otherPlayerBalance = world.getCurrentBalance(otherPlayer);
        TerrainTile unrelatedTile = world.getTile(UNRELATED_TILE);
        Train otherTrain = world.getTrain(otherPlayer, 8);

        ChangeCargoAtStationMove move = new ChangeCargoAtStationMove(player, 10, replacement);
        assertTrue(move.applicable(world).isSuccess());
        move.apply(world);

        assertEquals(replacement, world.getStation(player, 10).getCargoBatchBundle());
        assertEquals(otherStationCargo, world.getStation(player, 11).getCargoBatchBundle());
        assertEquals(cargo(11), world.getStation(otherPlayer, 10).getCargoBatchBundle());
        assertEquals(playerBalance, world.getCurrentBalance(player));
        assertEquals(otherPlayerBalance, world.getCurrentBalance(otherPlayer));
        assertEquals(unrelatedTile, world.getTile(UNRELATED_TILE));
        assertEquals(otherTrain, world.getTrain(otherPlayer, 8));
    }

    public void testRejectedMovesLeaveTheWorldUnchanged() {
        world.setRules(new Rules(false, false));
        TrackPiece before = new TrackPiece(
                TrackConfiguration.getFlatInstance("000010000"), world.getTrackType(0), player.getId());
        TrackPiece otherOwner = new TrackPiece(
                TrackConfiguration.getFlatInstance("000010000"), world.getTrackType(0), otherPlayer.getId());
        TerrainTile tile = world.getTile(TILE_TO_CHANGE);
        world.setTile(TILE_TO_CHANGE, new TerrainTile(tile.terrainTypeId(), before));

        assertRejectedWithoutMutation(new ChangeCargoAtStationMove(player, 999, cargo(1)));
        assertRejectedWithoutMutation(new UpdateTrainMove(player, 999, null, java.util.List.of(), null));
        assertRejectedWithoutMutation(new AddTransactionMove(player, transaction(-1_000_000_000_000L), true));
        assertRejectedWithoutMutation(new ChangeTrackPieceMove(null, null, new Vec2D(-1, 0)));
        assertRejectedWithoutMutation(new ChangeTrackPieceMove(before, otherOwner, TILE_TO_CHANGE));
    }

    private void assertApplicableWithoutMutation(Move move, boolean expectedSuccess) {
        World before = copyWorld();
        int beforeHashCode = world.hashCode();

        for (int attempt = 0; attempt < 3; attempt++) {
            assertEquals("Unexpected applicability on attempt " + attempt,
                    expectedSuccess, move.applicable(world).isSuccess());
            assertEquals("applicable changed the world", before, world);
            assertEquals("applicable changed the world hash code", beforeHashCode, world.hashCode());
        }
    }

    private void assertRejectedWithoutMutation(Move move) {
        assertApplicableWithoutMutation(move, false);
    }

    private World copyWorld() {
        return (World) freerails.util.Utils.cloneBySerialisation(world);
    }

    private static Transaction transaction(long amount) {
        return new Transaction(TransactionCategory.MISC_INCOME, new Money(amount), new Time(42));
    }

    private static CargoBatchBundle cargo(int amount) {
        CargoBatchBundle cargo = new CargoBatchBundle();
        cargo.addCargo(new CargoBatch(1, Vec2D.ZERO, 42, 9), amount);
        return cargo;
    }

    private static Station station(int id, int x) {
        return new Station(id, new Vec2D(x, 4), "Station " + id, 1, CargoBatchBundle.EMPTY);
    }
}
