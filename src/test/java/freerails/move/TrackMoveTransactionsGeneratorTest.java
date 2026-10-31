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
package freerails.move;

import freerails.controller.TrackMoveProducer;
import freerails.io.GsonManager;
import freerails.model.finance.Money;
import freerails.model.game.Rules;
import freerails.model.finance.transaction.Transaction;
import freerails.model.finance.transaction.TransactionCategory;
import freerails.model.terrain.TileTransition;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainCategory;
import freerails.model.terrain.TerrainTile;
import freerails.model.track.TrackType;
import freerails.model.world.*;
import freerails.move.generator.TrackMoveTransactionsGenerator;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.move.mapupdatemove.TrackMove;
import freerails.util.Vec2D;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackPiece;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Test for TrackMoveTransactionsGenerator.
 */
public class TrackMoveTransactionsGeneratorTest extends TestCase {

    private World world;
    private TrackMoveTransactionsGenerator transactionGenerator;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = WorldGenerator.testWorld(true);
        world.addPlayer(WorldGenerator.TEST_PLAYER);
        transactionGenerator = new TrackMoveTransactionsGenerator(world, WorldGenerator.TEST_PLAYER);
    }

    /**
     *
     */
    public void testAddTrackMove() {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration newConfig;
        TrackMove trackMove;

        // Try building the simplest piece of track.
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = world.getTile(Vec2D.ZERO).trackPiece();

        TrackType trackType = world.getTrackType(0);
        int owner = WorldGenerator.TEST_PLAYER.getId();
        newTrackPiece = new TrackPiece(newConfig, trackType, owner);
        trackMove = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, Vec2D.ZERO);

        Move move = transactionGenerator.addTransactions(trackMove);
        assertNotNull(move);
    }

    public void testTerrainTrackCostIsAddedToTrackPriceAndNotRefunded() throws Exception {
        URL url = getClass().getResource("/freerails/data/scenario/track_types.json");
        SortedSet<TrackType> trackTypes = GsonManager.loadTrackTypes(new File(url.toURI()));
        SortedSet<Terrain> terrainTypes = new TreeSet<>();
        terrainTypes.add(new Terrain(0, "Costly", TerrainCategory.COUNTRY, new Money(1000), null,
                List.of(), List.of(), List.of()));
        terrainTypes.add(new Terrain(1, "Free", TerrainCategory.COUNTRY, Money.ZERO, null,
                List.of(), List.of(), List.of()));
        World testWorld = new World.Builder()
                .setMapSize(new Vec2D(2, 1))
                .setTerrainTypes(terrainTypes)
                .setTrackTypes(trackTypes)
                .setRules(new Rules(false, false))
                .build();
        testWorld.addPlayer(WorldGenerator.TEST_PLAYER);
        testWorld.addTransaction(WorldGenerator.TEST_PLAYER, new Transaction(
                TransactionCategory.MISC_INCOME, new Money(100_000), testWorld.getClock().getCurrentTime()));
        testWorld.setTile(Vec2D.ZERO, new TerrainTile(0));
        Vec2D end = new Vec2D(1, 0);
        testWorld.setTile(end, new TerrainTile(1));

        TrackMoveTransactionsGenerator generator =
                new TrackMoveTransactionsGenerator(testWorld, WorldGenerator.TEST_PLAYER);
        Money startingBalance = testWorld.getCurrentBalance(WorldGenerator.TEST_PLAYER);
        TrackType standardTrack = testWorld.getTrackType(0);
        Move buildMove = TrackMoveProducer.generateBuildTrackMove(
                Vec2D.ZERO, TileTransition.EAST, standardTrack, standardTrack,
                testWorld, WorldGenerator.TEST_PLAYER);

        generator.addTransactions(buildMove).apply(testWorld);

        assertEquals(new Money(startingBalance.amount() - 10_500),
                testWorld.getCurrentBalance(WorldGenerator.TEST_PLAYER));

        Move removeMove = TrackMoveProducer.generateRemoveTrackMove(
                Vec2D.ZERO, TileTransition.EAST, testWorld, WorldGenerator.TEST_PLAYER);
        generator.addTransactions(removeMove).apply(testWorld);

        assertEquals(new Money(startingBalance.amount() - 5_500),
                testWorld.getCurrentBalance(WorldGenerator.TEST_PLAYER));
    }
}