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
 * ChangeTrackPieceCompositeMoveTest.java
 * JUnit based test
 *
 */
package freerails.move.mapupdatemove;

import freerails.io.GsonManager;
import freerails.model.terrain.Terrain;
import freerails.model.world.WorldItem;
import freerails.model.world.SharedKey;
import freerails.move.AbstractMoveTestCase;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.move.TrackMoveTransactionsGenerator;
import freerails.savegames.MapCreator;
import freerails.util.Vec2D;
import freerails.model.*;
import freerails.model.game.GameRules;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.*;
import freerails.model.world.World;

import java.io.File;
import java.net.URL;
import java.util.SortedSet;

/**
 *
 */
public class ChangeTrackPieceCompositeMoveTest extends AbstractMoveTestCase {

    private TrackMoveTransactionsGenerator transactionsGenerator;

    /**
     *
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        super.setHasSetupBeenCalled(true);
        // load terrain types
        URL url = MapCreator.class.getResource("/freerails/data/scenario/terrain_types.json");
        File file = new File(url.toURI());
        SortedSet<Terrain> terrainTypes = GsonManager.loadTerrainTypes(file);

        // load track types
        url = MapCreator.class.getResource("/freerails/data/scenario/track_types.json");
        file = new File(url.toURI());
        SortedSet<TrackType> trackTypes = GsonManager.loadTrackTypes(file);

        setWorld(new World.Builder().setMapSize(new Vec2D(10, 10)).setTerrainTypes(terrainTypes).setTrackTypes(trackTypes).build());
        getWorld().set(WorldItem.GameRules, GameRules.DEFAULT_RULES);
        getWorld().addPlayer(MapFixtureFactory.TEST_PLAYER);
        MapFixtureFactory.generateTrackRuleList(getWorld());
        transactionsGenerator = new TrackMoveTransactionsGenerator(getWorld(), MapFixtureFactory.TEST_PRINCIPAL);
    }

    /**
     *
     */
    public void testRemoveTrack() {
        getWorld().set(WorldItem.GameRules, GameRules.NO_RESTRICTIONS);

        TrackRule trackRule = (TrackRule) getWorld().get(SharedKey.TrackRules, 0);
        TrackType trackType = getWorld().getTrackType(0);

        assertBuildTrackSucceeds(new Vec2D(0, 5), TileTransition.EAST, trackRule, trackType);
        assertBuildTrackSucceeds(new Vec2D(0, 6), TileTransition.EAST, trackRule, trackType);
        assertBuildTrackSucceeds(new Vec2D(1, 6), TileTransition.EAST, trackRule, trackType);
        assertBuildTrackSucceeds(new Vec2D(0, 7), TileTransition.EAST, trackRule, trackType);
        assertBuildTrackSucceeds(new Vec2D(1, 7), TileTransition.EAST, trackRule, trackType);
        assertBuildTrackSucceeds(new Vec2D(2, 7), TileTransition.EAST, trackRule, trackType);

        // Remove only track piece built.
        assertRemoveTrackSucceeds(new Vec2D(0, 5), TileTransition.EAST);
        TrackPiece trackPiece = getWorld().getTile(new Vec2D(0, 5)).getTrackPiece();
        assertEquals(null, trackPiece);

        trackPiece = getWorld().getTile(new Vec2D(1, 5)).getTrackPiece();
        assertEquals(null, trackPiece);
    }

    /**
     * All track except the first piece built should be connected to existing
     * track.
     */
    public void testMustConnectToExistingTrack() {
        TrackRule trackRule = (TrackRule) world.get(SharedKey.TrackRules, 0);
        TrackType trackType = world.getTrackType(0);

        int numberOfTransactions = world.getNumberOfTransactions(MapFixtureFactory.TEST_PRINCIPAL);
        assertEquals(0, numberOfTransactions);

        boolean hasTrackBeenBuilt = ChangeTrackPieceCompositeMove.hasAnyTrackBeenBuilt(world, MapFixtureFactory.TEST_PRINCIPAL);
        assertFalse("No track has been built yet.", hasTrackBeenBuilt);
        assertBuildTrackSucceeds(new Vec2D(0, 5), TileTransition.EAST, trackRule, trackType);

        // Building the track should have added a transaction.
        numberOfTransactions = world.getNumberOfTransactions(MapFixtureFactory.TEST_PRINCIPAL);
        assertTrue(0 < numberOfTransactions);

        hasTrackBeenBuilt = ChangeTrackPieceCompositeMove.hasAnyTrackBeenBuilt(
                world, MapFixtureFactory.TEST_PRINCIPAL);
        assertTrue("One track piece has been built.", hasTrackBeenBuilt);

        assertBuildTrackSucceeds(new Vec2D(1, 5), TileTransition.EAST, trackRule, trackType);
        assertBuildTrackFails(new Vec2D(4, 8), TileTransition.EAST, trackRule, trackType);
    }

    /**
     *
     */
    public void testCannotConnect2OtherRRsTrack() {
        assertFalse(ChangeTrackPieceMove.canConnectToOtherRRsTrack(world));
        final int TRACK_RULE_ID = 0;
        TrackRule trackRule = (TrackRule) getWorld().get(SharedKey.TrackRules, TRACK_RULE_ID);
        TrackType trackType = getWorld().getTrackType(TRACK_RULE_ID);

        assertBuildTrackSucceeds(new Vec2D(0, 6), TileTransition.EAST, trackRule, trackType);
        // Now change the owner of the track piece at (1, 6);
        int anotherPlayer = 999;
        TerrainTile oldTile = world.getTile(new Vec2D(1, 6));
        TrackPiece tp = oldTile.getTrackPiece();
        TrackPiece newTrackPiece = new TrackPiece(tp.getTrackConfiguration(), tp.getTrackRule(), tp.getTrackType(), anotherPlayer, TRACK_RULE_ID);
        TerrainTile newTile = new TerrainTile(oldTile.getTerrainTypeId(), newTrackPiece);
        world.setTile(new Vec2D(1, 6), newTile);
        assertBuildTrackFails(new Vec2D(1, 6), TileTransition.EAST, trackRule, trackType);
        world.setTile(new Vec2D(1, 6), oldTile);
        assertBuildTrackSucceeds(new Vec2D(1, 6), TileTransition.EAST, trackRule, trackType);
    }

    /**
     *
     */
    public void testBuildTrack() {
        Vec2D pointA = Vec2D.ZERO;
        Vec2D pointB = new Vec2D(1, 1);
        Vec2D pointC = new Vec2D(1, 0);

        TrackRule trackRule = (TrackRule) getWorld().get(SharedKey.TrackRules, 0);
        TrackType trackType = getWorld().getTrackType(0);

        // First track piece built
        assertBuildTrackSucceeds(pointA, TileTransition.SOUTH_EAST, trackRule, trackType);

        // Track connected from one existing track piece
        assertBuildTrackSucceeds(pointB, TileTransition.NORTH_EAST, trackRule, trackType);

        // Track connected to one existing track piece
        // This is not going through for some reason, not sure why.
        // assertBuildTrackSucceeds(pointC, west, trackRule);
        // Track connecting two existing track pieces.
        assertBuildTrackSucceeds(pointA, TileTransition.EAST, trackRule, trackType);

        // Track off map.. should fail.
        assertBuildTrackFails(pointA, TileTransition.NORTH_EAST, trackRule, trackType);

        // Track already there.
        assertBuildTrackFails(pointA, TileTransition.SOUTH_EAST, trackRule, trackType);

        // Illegal config. connecting from one existing track piece
        assertBuildTrackFails(pointA, TileTransition.SOUTH, trackRule, trackType);

        // Illegal config. connecting to one existing track piece
        assertBuildTrackFails(new Vec2D(0, 1), TileTransition.NORTH_EAST, trackRule, trackType);

        // Illegal config. connecting between two existing track pieces
        assertBuildTrackFails(pointC, TileTransition.SOUTH, trackRule, trackType);

        // Not allowed on this terrain type, from existing track.
        assertBuildTrackFails(new Vec2D(2, 0), TileTransition.NORTH_EAST, (TrackRule) getWorld().get(SharedKey.TrackRules, 1), getWorld().getTrackType(1));
    }

    private void assertBuildTrackFails(Vec2D p, TileTransition v, TrackRule rule, TrackType type) {
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                .generateBuildTrackMove(p, v, rule, type, rule, type, getWorld(), MapFixtureFactory.TEST_PRINCIPAL);
        MoveStatus status = move.doMove(getWorld(), Player.AUTHORITATIVE);
        assertFalse(status.succeeds());
    }

    private void assertBuildTrackSucceeds(Vec2D p, TileTransition v, TrackRule rule, TrackType type) {
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(p, v, rule, type, rule, type, getWorld(), MapFixtureFactory.TEST_PRINCIPAL);

        Move moveAndTransaction = transactionsGenerator.addTransactions(move);
        MoveStatus status = moveAndTransaction.doMove(getWorld(), Player.AUTHORITATIVE);
        assertEquals(true, status.succeeds());
    }

    private void assertRemoveTrackSucceeds(Vec2D p, TileTransition v) {
        try {
            ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                    .generateRemoveTrackMove(p, v, getWorld(), MapFixtureFactory.TEST_PRINCIPAL);
            MoveStatus status = move.doMove(getWorld(), Player.AUTHORITATIVE);
            assertEquals(true, status.succeeds());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     *
     */
    public void testMove() throws Exception {
        Vec2D pointA = Vec2D.ZERO;
        TrackRule trackRule = (TrackRule) getWorld().get(SharedKey.TrackRules, 0);
        TrackType trackType = getWorld().getTrackType(0);

        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(pointA, TileTransition.SOUTH_EAST, trackRule, trackType, trackRule, trackType, getWorld(), MapFixtureFactory.TEST_PRINCIPAL);

        assertSurvivesSerialisation(move);
        assertOkButNotRepeatable(move);

        // TODO do we really need to call setUp again here?
        setUp();
        assertDoThenUndoLeavesWorldUnchanged(move);
    }
}