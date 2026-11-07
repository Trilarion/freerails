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
import freerails.model.economy.TransactionUtils;
import freerails.model.world.World;
import freerails.move.AbstractMoveTestCase;
import freerails.move.Move;
import freerails.move.Status;
import freerails.move.generator.TrackMoveTransactionsGenerator;
import freerails.util.IntPoint2D;
import freerails.model.game.Rules;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.*;
import freerails.util.WorldGenerator;

/**
 * Tests change track piece composite move behavior.
 */
public class ChangeTrackPieceCompositeMoveTest extends AbstractMoveTestCase {

    private TrackMoveTransactionsGenerator transactionsGenerator;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
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
     * Tests remove track behavior.
     */
    public void testRemoveTrack() {
        // TODO need this rules change here really?
        Rules rules = new Rules(false, false);
        world.setRules(rules);

        TrackType trackType = getWorld().getTrackType(0);

        assertBuildTrackSucceeds(new IntPoint2D(0, 5), TileTransition.EAST, trackType);
        assertBuildTrackSucceeds(new IntPoint2D(0, 6), TileTransition.EAST, trackType);
        assertBuildTrackSucceeds(new IntPoint2D(1, 6), TileTransition.EAST, trackType);
        assertBuildTrackSucceeds(new IntPoint2D(0, 7), TileTransition.EAST, trackType);
        assertBuildTrackSucceeds(new IntPoint2D(1, 7), TileTransition.EAST, trackType);
        assertBuildTrackSucceeds(new IntPoint2D(2, 7), TileTransition.EAST, trackType);

        // Remove only track piece built.
        assertRemoveTrackSucceeds(new IntPoint2D(0, 5), TileTransition.EAST);
        TrackPiece trackPiece = getWorld().getTile(new IntPoint2D(0, 5)).trackPiece();
        assertEquals(null, trackPiece);

        trackPiece = getWorld().getTile(new IntPoint2D(1, 5)).trackPiece();
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
        assertBuildTrackSucceeds(new IntPoint2D(0, 5), TileTransition.EAST, trackType);

        // Building the track should have added a transaction.
        numberOfTransactions = world.getTransactions(WorldGenerator.TEST_PLAYER).size();
        assertTrue(0 < numberOfTransactions);

        hasTrackBeenBuilt = TransactionUtils.hasAnyTrackBeenBuilt(world, WorldGenerator.TEST_PLAYER);
        assertTrue("One track piece has been built.", hasTrackBeenBuilt);

        assertBuildTrackSucceeds(new IntPoint2D(1, 5), TileTransition.EAST, trackType);
        assertBuildTrackFails(new IntPoint2D(4, 8), TileTransition.EAST, trackType);
    }

    /**
     * Tests cannot connect2 other r rs track behavior.
     */
    public void testCannotConnect2OtherRRsTrack() {
        Rules rules = world.getRules();
        assertFalse(rules.canConnectToOtherPlayersTracks());
        final int TRACK_RULE_ID = 0;
        TrackType trackType = getWorld().getTrackType(TRACK_RULE_ID);

        assertBuildTrackSucceeds(new IntPoint2D(0, 6), TileTransition.EAST, trackType);
        // Now change the owner of the track piece at (1, 6);
        int anotherPlayer = 999;
        TerrainTile oldTile = world.getTile(new IntPoint2D(1, 6));
        TrackPiece tp = oldTile.trackPiece();
        TrackPiece newTrackPiece = new TrackPiece(tp.trackConfiguration(), tp.trackType(), anotherPlayer);
        TerrainTile newTile = new TerrainTile(oldTile.terrainTypeId(), newTrackPiece);
        world.setTile(new IntPoint2D(1, 6), newTile);
        assertBuildTrackFails(new IntPoint2D(1, 6), TileTransition.EAST, trackType);
        world.setTile(new IntPoint2D(1, 6), oldTile);
        assertBuildTrackSucceeds(new IntPoint2D(1, 6), TileTransition.EAST, trackType);
    }

    /**
     * Tests build track behavior.
     */
    public void testBuildTrack() {
        IntPoint2D pointA = IntPoint2D.ZERO;
        IntPoint2D pointB = new IntPoint2D(1, 1);
        IntPoint2D pointC = new IntPoint2D(1, 0);

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
        assertBuildTrackFails(new IntPoint2D(0, 1), TileTransition.NORTH_EAST, trackType);

        // Illegal config. connecting between two existing track pieces
        assertBuildTrackFails(pointC, TileTransition.SOUTH, trackType);

        // Not allowed on this terrain type, from existing track.
        assertBuildTrackFails(new IntPoint2D(2, 0), TileTransition.NORTH_EAST, getWorld().getTrackType(1));
    }

    /**
     * Tests construction and removal keep adjacent connections symmetric behavior.
     * @throws Exception if the test construction and removal keep adjacent connections symmetric operation fails.
     */
    public void testConstructionAndRemovalKeepAdjacentConnectionsSymmetric() throws Exception {
        world.setRules(new Rules(false, false));
        TrackType trackType = world.getTrackType(0);

        assertBuildTrackSucceeds(new IntPoint2D(0, 5), TileTransition.EAST, trackType);
        assertSymmetricConnections();
        assertBuildTrackSucceeds(new IntPoint2D(1, 5), TileTransition.EAST, trackType);
        assertSymmetricConnections();
        TrackConfiguration straight = world.getTile(new IntPoint2D(1, 5)).trackPiece().trackConfiguration();
        assertTrue(straight.contains(TileTransition.WEST));
        assertTrue(straight.contains(TileTransition.EAST));
        assertEquals(2, Integer.bitCount(straight.get8bitTemplate()));

        assertBuildTrackSucceeds(new IntPoint2D(5, 5), TileTransition.EAST, trackType);
        assertSymmetricConnections();
        assertBuildTrackSucceeds(new IntPoint2D(6, 5), TileTransition.SOUTH, trackType);
        assertSymmetricConnections();
        TrackConfiguration corner = world.getTile(new IntPoint2D(6, 5)).trackPiece().trackConfiguration();
        assertTrue(corner.contains(TileTransition.WEST));
        assertTrue(corner.contains(TileTransition.SOUTH));
        assertEquals(2, Integer.bitCount(corner.get8bitTemplate()));

        assertBuildTrackSucceeds(new IntPoint2D(8, 5), TileTransition.EAST, trackType);
        assertSymmetricConnections();
        TrackConfiguration endpoint = world.getTile(new IntPoint2D(8, 5)).trackPiece().trackConfiguration();
        assertTrue(endpoint.contains(TileTransition.EAST));
        assertEquals(1, Integer.bitCount(endpoint.get8bitTemplate()));

        assertRemoveTrackSucceeds(new IntPoint2D(0, 5), TileTransition.EAST);
        assertSymmetricConnections();
        assertNull(world.getTile(new IntPoint2D(0, 5)).trackPiece());
        straight = world.getTile(new IntPoint2D(1, 5)).trackPiece().trackConfiguration();
        assertTrue(straight.contains(TileTransition.EAST));
        assertEquals(1, Integer.bitCount(straight.get8bitTemplate()));
        assertRemoveTrackSucceeds(new IntPoint2D(6, 5), TileTransition.SOUTH);
        assertSymmetricConnections();
        assertNull(world.getTile(new IntPoint2D(6, 6)).trackPiece());
        endpoint = world.getTile(new IntPoint2D(6, 5)).trackPiece().trackConfiguration();
        assertTrue(endpoint.contains(TileTransition.WEST));
        assertEquals(1, Integer.bitCount(endpoint.get8bitTemplate()));
        assertRemoveTrackSucceeds(new IntPoint2D(8, 5), TileTransition.EAST);
        assertSymmetricConnections();
        assertNull(world.getTile(new IntPoint2D(8, 5)).trackPiece());
        assertNull(world.getTile(new IntPoint2D(9, 5)).trackPiece());
    }

    /**
     * Tests track rules across ownership and connectivity cases behavior.
     */
    public void testTrackRulesAcrossOwnershipAndConnectivityCases() {
        for (boolean canConnectToOtherPlayers : new boolean[]{false, true}) {
            for (boolean mustStayConnected : new boolean[]{false, true}) {
                Rules rules = new Rules(canConnectToOtherPlayers, mustStayConnected);
                TrackType trackType = world.getTrackType(0);

                World ownTrackWorld = createWorld(rules);
                assertBuild(ownTrackWorld, new IntPoint2D(0, 5), TileTransition.EAST, trackType, true);
                assertBuild(ownTrackWorld, new IntPoint2D(1, 5), TileTransition.EAST, trackType, true);
                assertEquals(WorldGenerator.TEST_PLAYER.getId(),
                        ownTrackWorld.getTile(new IntPoint2D(2, 5)).trackPiece().playerId());

                World otherTrackWorld = createWorld(rules);
                assertBuild(otherTrackWorld, new IntPoint2D(0, 5), TileTransition.EAST, trackType, true);
                IntPoint2D sharedTile = new IntPoint2D(1, 5);
                TerrainTile oldTile = otherTrackWorld.getTile(sharedTile);
                TrackPiece oldPiece = oldTile.trackPiece();
                otherTrackWorld.setTile(sharedTile, new TerrainTile(oldTile.terrainTypeId(),
                        new TrackPiece(oldPiece.trackConfiguration(), oldPiece.trackType(), 999)));
                boolean canBuildOnOtherTrack = canConnectToOtherPlayers;
                assertBuild(otherTrackWorld, sharedTile, TileTransition.EAST, trackType, canBuildOnOtherTrack);
                assertEquals(canBuildOnOtherTrack,
                        otherTrackWorld.getTile(new IntPoint2D(2, 5)).trackPiece() != null);

                World disconnectedWorld = createWorld(rules);
                assertBuild(disconnectedWorld, new IntPoint2D(0, 5), TileTransition.EAST, trackType, true);
                boolean canBuildDisconnected = !mustStayConnected;
                assertBuild(disconnectedWorld, new IntPoint2D(7, 7), TileTransition.EAST,
                        trackType, canBuildDisconnected);
                assertEquals(canBuildDisconnected,
                        disconnectedWorld.getTile(new IntPoint2D(7, 7)).trackPiece() != null);
            }
        }
    }

    /**
     * Creates world.
     * @param rules the rules used by this test.
     * @return the result of create world.
     */
    private static World createWorld(Rules rules) {
        World testWorld = WorldGenerator.testWorld(true);
        testWorld.addPlayer(WorldGenerator.TEST_PLAYER);
        testWorld.setRules(rules);
        return testWorld;
    }

    /**
     * Asserts build.
     * @param testWorld the test world to use in the test.
     * @param from the from to use in the test.
     * @param direction the direction to use in the test.
     * @param type the type to use in the test.
     * @param expectedSuccess the expected success to use in the test.
     */
    private static void assertBuild(World testWorld, IntPoint2D from, TileTransition direction,
                                    TrackType type, boolean expectedSuccess) {
        IntPoint2D to = direction.createRelocatedPoint(from);
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

    /**
     * Asserts symmetric connections.
     */
    private void assertSymmetricConnections() {
        for (int x = 0; x < world.getMapSize().x(); x++) {
            for (int y = 0; y < world.getMapSize().y(); y++) {
                IntPoint2D position = new IntPoint2D(x, y);
                TrackPiece piece = world.getTile(position).trackPiece();
                if (piece == null) {
                    continue;
                }
                for (TileTransition direction : TileTransition.getTransitions()) {
                    IntPoint2D neighborPosition = direction.createRelocatedPoint(position);
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

    /**
     * Asserts build track fails.
     * @param p the p to use in the test.
     * @param v the v to use in the test.
     * @param type the type to use in the test.
     */
    private void assertBuildTrackFails(IntPoint2D p, TileTransition v, TrackType type) {
        ChangeTrackPieceCompositeMove move = TrackMoveProducer.generateBuildTrackMove(p, v, type, type, getWorld(), WorldGenerator.TEST_PLAYER);
        Status status = move.applicable(getWorld());
        assertFalse(status.isSuccess());
        move.apply(getWorld());
    }

    /**
     * Asserts build track succeeds.
     * @param p the p to use in the test.
     * @param v the v to use in the test.
     * @param type the type to use in the test.
     */
    private void assertBuildTrackSucceeds(IntPoint2D p, TileTransition v, TrackType type) {
        ChangeTrackPieceCompositeMove move = TrackMoveProducer.generateBuildTrackMove(p, v, type, type, getWorld(), WorldGenerator.TEST_PLAYER);

        Move move1 = transactionsGenerator.addTransactions(move);
        Status status = move1.applicable(getWorld());
        assertEquals(status.getMessage(), true, status.isSuccess());
        move1.apply(getWorld());
    }

    /**
     * Asserts remove track succeeds.
     * @param p the p to use in the test.
     * @param v the v to use in the test.
     */
    private void assertRemoveTrackSucceeds(IntPoint2D p, TileTransition v) {
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
     * Tests move behavior.
     * @throws Exception if the test move operation fails.
     */
    public void testMove() throws Exception {
        IntPoint2D pointA = IntPoint2D.ZERO;
        TrackType trackType = getWorld().getTrackType(0);

        ChangeTrackPieceCompositeMove move = TrackMoveProducer.generateBuildTrackMove(pointA, TileTransition.SOUTH_EAST, trackType, trackType, getWorld(), WorldGenerator.TEST_PLAYER);
        assertSurvivesSerialisation(move);
        assertOkButNotRepeatable(move);
    }
}