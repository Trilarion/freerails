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

import freerails.controller.TrackMoveProducer;
import freerails.model.finance.TransactionUtils;
import freerails.move.AbstractMoveTestCase;
import freerails.move.Move;
import freerails.move.Status;
import freerails.move.generator.TrackMoveTransactionsGenerator;
import freerails.util.Vec2D;
import freerails.model.game.Rules;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.*;
import freerails.util.WorldGenerator;

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

        setWorld(WorldGenerator.testWorld(true));
        getWorld().addPlayer(WorldGenerator.TEST_PLAYER);

        transactionsGenerator = new TrackMoveTransactionsGenerator(getWorld(), WorldGenerator.TEST_PLAYER);
    }

    /**
     *
     */
    public void testRemoveTrack() {
        // TODO need this rules change here really?
        Rules rules = new Rules(false, false);
        world.setRules(rules);

        TrackType trackType = getWorld().getTrackType(0);

        assertBuildTrackSucceeds(new Vec2D(0, 5), TileTransition.EAST, trackType);
        assertBuildTrackSucceeds(new Vec2D(0, 6), TileTransition.EAST, trackType);
        assertBuildTrackSucceeds(new Vec2D(1, 6), TileTransition.EAST, trackType);
        assertBuildTrackSucceeds(new Vec2D(0, 7), TileTransition.EAST, trackType);
        assertBuildTrackSucceeds(new Vec2D(1, 7), TileTransition.EAST, trackType);
        assertBuildTrackSucceeds(new Vec2D(2, 7), TileTransition.EAST, trackType);

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
        TrackType trackType = world.getTrackType(0);
        Rules rules = world.getRules();
        assertTrue(rules.mustStayConnectedToExistingTrack());

        int numberOfTransactions = world.getTransactions(WorldGenerator.TEST_PLAYER).size();
        assertEquals(0, numberOfTransactions);

        boolean hasTrackBeenBuilt = TransactionUtils.hasAnyTrackBeenBuilt(world, WorldGenerator.TEST_PLAYER);
        assertFalse("No track has been built yet.", hasTrackBeenBuilt);
        assertBuildTrackSucceeds(new Vec2D(0, 5), TileTransition.EAST, trackType);

        // Building the track should have added a transaction.
        numberOfTransactions = world.getTransactions(WorldGenerator.TEST_PLAYER).size();
        assertTrue(0 < numberOfTransactions);

        hasTrackBeenBuilt = TransactionUtils.hasAnyTrackBeenBuilt(world, WorldGenerator.TEST_PLAYER);
        assertTrue("One track piece has been built.", hasTrackBeenBuilt);

        assertBuildTrackSucceeds(new Vec2D(1, 5), TileTransition.EAST, trackType);
        assertBuildTrackFails(new Vec2D(4, 8), TileTransition.EAST, trackType);
    }

    /**
     *
     */
    public void testCannotConnect2OtherRRsTrack() {
        Rules rules = world.getRules();
        assertFalse(rules.canConnectToOtherPlayersTracks());
        final int TRACK_RULE_ID = 0;
        TrackType trackType = getWorld().getTrackType(TRACK_RULE_ID);

        assertBuildTrackSucceeds(new Vec2D(0, 6), TileTransition.EAST, trackType);
        // Now change the owner of the track piece at (1, 6);
        int anotherPlayer = 999;
        TerrainTile oldTile = world.getTile(new Vec2D(1, 6));
        TrackPiece tp = oldTile.getTrackPiece();
        TrackPiece newTrackPiece = new TrackPiece(tp.getTrackConfiguration(), tp.getTrackType(), anotherPlayer);
        TerrainTile newTile = new TerrainTile(oldTile.getTerrainTypeId(), newTrackPiece);
        world.setTile(new Vec2D(1, 6), newTile);
        assertBuildTrackFails(new Vec2D(1, 6), TileTransition.EAST, trackType);
        world.setTile(new Vec2D(1, 6), oldTile);
        assertBuildTrackSucceeds(new Vec2D(1, 6), TileTransition.EAST, trackType);
    }

    /**
     *
     */
    public void testBuildTrack() {
        Vec2D pointA = Vec2D.ZERO;
        Vec2D pointB = new Vec2D(1, 1);
        Vec2D pointC = new Vec2D(1, 0);

        TrackType trackType = getWorld().getTrackType(0);

        // First track piece built
        assertBuildTrackSucceeds(pointA, TileTransition.SOUTH_EAST, trackType);

        // Track connected from one existing track piece
        assertBuildTrackSucceeds(pointB, TileTransition.NORTH_EAST, trackType);

        // Track connected to one existing track piece
        // This is not going through for some reason, not sure why.
        // assertBuildTrackSucceeds(pointC, west, trackRule);
        // Track connecting two existing track pieces.
        assertBuildTrackSucceeds(pointA, TileTransition.EAST, trackType);

        // Track off map.. should fail.
        assertBuildTrackFails(pointA, TileTransition.NORTH_EAST, trackType);

        // Track already there.
        assertBuildTrackFails(pointA, TileTransition.SOUTH_EAST, trackType);

        // Illegal config. connecting from one existing track piece
        assertBuildTrackFails(pointA, TileTransition.SOUTH, trackType);

        // Illegal config. connecting to one existing track piece
        assertBuildTrackFails(new Vec2D(0, 1), TileTransition.NORTH_EAST, trackType);

        // Illegal config. connecting between two existing track pieces
        assertBuildTrackFails(pointC, TileTransition.SOUTH, trackType);

        // Not allowed on this terrain type, from existing track.
        assertBuildTrackFails(new Vec2D(2, 0), TileTransition.NORTH_EAST, getWorld().getTrackType(1));
    }

    private void assertBuildTrackFails(Vec2D p, TileTransition v, TrackType type) {
        ChangeTrackPieceCompositeMove move = TrackMoveProducer.generateBuildTrackMove(p, v, type, type, getWorld(), WorldGenerator.TEST_PLAYER);
        Status status = move.applicable(getWorld());
        assertFalse(status.isSuccess());
        move.apply(getWorld());
    }

    private void assertBuildTrackSucceeds(Vec2D p, TileTransition v, TrackType type) {
        ChangeTrackPieceCompositeMove move = TrackMoveProducer.generateBuildTrackMove(p, v, type, type, getWorld(), WorldGenerator.TEST_PLAYER);

        Move move1 = transactionsGenerator.addTransactions(move);
        Status status = move1.applicable(getWorld());
        assertEquals(true, status.isSuccess());
        move1.apply(getWorld());
    }

    private void assertRemoveTrackSucceeds(Vec2D p, TileTransition v) {
        try {
            ChangeTrackPieceCompositeMove move = TrackMoveProducer
                    .generateRemoveTrackMove(p, v, getWorld(), WorldGenerator.TEST_PLAYER);
            Status status = move.applicable(getWorld());
            assertEquals(true, status.isSuccess());
            move.apply(getWorld());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     */
    public void testMove() throws Exception {
        Vec2D pointA = Vec2D.ZERO;
        TrackType trackType = getWorld().getTrackType(0);

        ChangeTrackPieceCompositeMove move = TrackMoveProducer.generateBuildTrackMove(pointA, TileTransition.SOUTH_EAST, trackType, trackType, getWorld(), WorldGenerator.TEST_PLAYER);
        assertSurvivesSerialisation(move);
        assertOkButNotRepeatable(move);
    }
}