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
 * ChangeTrackPieceCompositeMoveTest.java
 * JUnit based test
 *
 */
package freerails.move.mapupdatemove;

import freerails.controller.TrackMoveProducer;
import freerails.model.finance.TransactionUtils;
import freerails.model.world.World;
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
        TrackPiece trackPiece = getWorld().getTile(new Vec2D(0, 5)).trackPiece();
        assertEquals(null, trackPiece);

        trackPiece = getWorld().getTile(new Vec2D(1, 5)).trackPiece();
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
        TrackPiece tp = oldTile.trackPiece();
        TrackPiece newTrackPiece = new TrackPiece(tp.trackConfiguration(), tp.trackType(), anotherPlayer);
        TerrainTile newTile = new TerrainTile(oldTile.terrainTypeId(), newTrackPiece);
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

    public void testConstructionAndRemovalKeepAdjacentConnectionsSymmetric() throws Exception {
        world.setRules(new Rules(false, false));
        TrackType trackType = world.getTrackType(0);

        assertBuildTrackSucceeds(new Vec2D(0, 5), TileTransition.EAST, trackType);
        assertSymmetricConnections();
        assertBuildTrackSucceeds(new Vec2D(1, 5), TileTransition.EAST, trackType);
        assertSymmetricConnections();
        TrackConfiguration straight = world.getTile(new Vec2D(1, 5)).trackPiece().trackConfiguration();
        assertTrue(straight.contains(TileTransition.WEST));
        assertTrue(straight.contains(TileTransition.EAST));
        assertTrue(straight.contains(TrackConfiguration.getFlatInstance("000010000")));
        assertEquals(2, Integer.bitCount(straight.get8bitTemplate()));

        assertBuildTrackSucceeds(new Vec2D(5, 5), TileTransition.EAST, trackType);
        assertSymmetricConnections();
        assertBuildTrackSucceeds(new Vec2D(6, 5), TileTransition.SOUTH, trackType);
        assertSymmetricConnections();
        TrackConfiguration corner = world.getTile(new Vec2D(6, 5)).trackPiece().trackConfiguration();
        assertTrue(corner.contains(TileTransition.WEST));
        assertTrue(corner.contains(TileTransition.SOUTH));
        assertEquals(2, Integer.bitCount(corner.get8bitTemplate()));

        assertBuildTrackSucceeds(new Vec2D(8, 5), TileTransition.EAST, trackType);
        assertSymmetricConnections();
        TrackConfiguration endpoint = world.getTile(new Vec2D(8, 5)).trackPiece().trackConfiguration();
        assertTrue(endpoint.contains(TileTransition.EAST));
        assertTrue(endpoint.contains(TrackConfiguration.getFlatInstance("000010000")));
        assertEquals(1, Integer.bitCount(endpoint.get8bitTemplate()));

        assertRemoveTrackSucceeds(new Vec2D(0, 5), TileTransition.EAST);
        assertSymmetricConnections();
        assertNull(world.getTile(new Vec2D(0, 5)).trackPiece());
        straight = world.getTile(new Vec2D(1, 5)).trackPiece().trackConfiguration();
        assertTrue(straight.contains(TileTransition.EAST));
        assertEquals(1, Integer.bitCount(straight.get8bitTemplate()));
        assertRemoveTrackSucceeds(new Vec2D(6, 5), TileTransition.SOUTH);
        assertSymmetricConnections();
        assertNull(world.getTile(new Vec2D(6, 6)).trackPiece());
        endpoint = world.getTile(new Vec2D(6, 5)).trackPiece().trackConfiguration();
        assertTrue(endpoint.contains(TileTransition.WEST));
        assertEquals(1, Integer.bitCount(endpoint.get8bitTemplate()));
        assertRemoveTrackSucceeds(new Vec2D(8, 5), TileTransition.EAST);
        assertSymmetricConnections();
        assertNull(world.getTile(new Vec2D(8, 5)).trackPiece());
        assertNull(world.getTile(new Vec2D(9, 5)).trackPiece());
    }

    public void testTrackRulesAcrossOwnershipAndConnectivityCases() {
        for (boolean canConnectToOtherPlayers : new boolean[]{false, true}) {
            for (boolean mustStayConnected : new boolean[]{false, true}) {
                Rules rules = new Rules(canConnectToOtherPlayers, mustStayConnected);
                TrackType trackType = world.getTrackType(0);

                World ownTrackWorld = createWorld(rules);
                assertBuild(ownTrackWorld, new Vec2D(0, 5), TileTransition.EAST, trackType, true);
                assertBuild(ownTrackWorld, new Vec2D(1, 5), TileTransition.EAST, trackType, true);
                assertEquals(WorldGenerator.TEST_PLAYER.getId(),
                        ownTrackWorld.getTile(new Vec2D(2, 5)).trackPiece().playerId());

                World otherTrackWorld = createWorld(rules);
                assertBuild(otherTrackWorld, new Vec2D(0, 5), TileTransition.EAST, trackType, true);
                Vec2D sharedTile = new Vec2D(1, 5);
                TerrainTile oldTile = otherTrackWorld.getTile(sharedTile);
                TrackPiece oldPiece = oldTile.trackPiece();
                otherTrackWorld.setTile(sharedTile, new TerrainTile(oldTile.terrainTypeId(),
                        new TrackPiece(oldPiece.trackConfiguration(), oldPiece.trackType(), 999)));
                boolean canBuildOnOtherTrack = canConnectToOtherPlayers;
                assertBuild(otherTrackWorld, sharedTile, TileTransition.EAST, trackType, canBuildOnOtherTrack);
                assertEquals(canBuildOnOtherTrack,
                        otherTrackWorld.getTile(new Vec2D(2, 5)).trackPiece() != null);

                World disconnectedWorld = createWorld(rules);
                assertBuild(disconnectedWorld, new Vec2D(0, 5), TileTransition.EAST, trackType, true);
                boolean canBuildDisconnected = !mustStayConnected;
                assertBuild(disconnectedWorld, new Vec2D(7, 7), TileTransition.EAST,
                        trackType, canBuildDisconnected);
                assertEquals(canBuildDisconnected,
                        disconnectedWorld.getTile(new Vec2D(7, 7)).trackPiece() != null);
            }
        }
    }

    private static World createWorld(Rules rules) {
        World testWorld = WorldGenerator.testWorld(true);
        testWorld.addPlayer(WorldGenerator.TEST_PLAYER);
        testWorld.setRules(rules);
        return testWorld;
    }

    private static void assertBuild(World testWorld, Vec2D from, TileTransition direction,
                                    TrackType type, boolean expectedSuccess) {
        Vec2D to = direction.createRelocatedPoint(from);
        TerrainTile fromBefore = testWorld.getTile(from);
        TerrainTile toBefore = testWorld.getTile(to);
        ChangeTrackPieceCompositeMove build = TrackMoveProducer.generateBuildTrackMove(
                from, direction, type, type, testWorld, WorldGenerator.TEST_PLAYER);
        Move move = new TrackMoveTransactionsGenerator(testWorld, WorldGenerator.TEST_PLAYER)
                .addTransactions(build);
        Status status = move.applicable(testWorld);
        assertEquals(status.getMessage(), expectedSuccess, status.isSuccess());
        if (expectedSuccess) {
            move.apply(testWorld);
            assertTrue(testWorld.getTile(from).trackPiece().trackConfiguration().contains(direction));
            assertTrue(testWorld.getTile(to).trackPiece().trackConfiguration().contains(direction.getOpposite()));
        } else {
            assertSame(fromBefore, testWorld.getTile(from));
            assertSame(toBefore, testWorld.getTile(to));
        }
    }

    private void assertSymmetricConnections() {
        for (int x = 0; x < world.getMapSize().x(); x++) {
            for (int y = 0; y < world.getMapSize().y(); y++) {
                Vec2D position = new Vec2D(x, y);
                TrackPiece piece = world.getTile(position).trackPiece();
                if (piece == null) {
                    continue;
                }
                for (TileTransition direction : TileTransition.getTransitions()) {
                    Vec2D neighborPosition = direction.createRelocatedPoint(position);
                    boolean hasConnection = piece.trackConfiguration().contains(direction);
                    boolean neighborHasMatchingConnection = world.boundsContain(neighborPosition)
                            && world.getTile(neighborPosition).trackPiece() != null
                            && world.getTile(neighborPosition).trackPiece().trackConfiguration()
                            .contains(direction.getOpposite());
                    assertEquals("Connection mismatch from " + position + " toward " + direction,
                            hasConnection, neighborHasMatchingConnection);
                }
            }
        }
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
        assertEquals(status.getMessage(), true, status.isSuccess());
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