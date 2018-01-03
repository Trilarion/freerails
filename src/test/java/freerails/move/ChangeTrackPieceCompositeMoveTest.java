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
package freerails.move;

import freerails.util.ImPoint;
import freerails.world.*;
import freerails.world.player.Player;
import freerails.world.terrain.FreerailsTile;
import freerails.world.top.MapFixtureFactory;
import freerails.world.track.*;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JUnit test.
 */
public class ChangeTrackPieceCompositeMoveTest extends AbstractMoveTestCase {
    final TileTransition southeast = TileTransition.SOUTH_EAST;

    final TileTransition east = TileTransition.EAST;

    final TileTransition northeast = TileTransition.NORTH_EAST;

    final TileTransition south = TileTransition.SOUTH;

    TrackMoveTransactionsGenerator transactionsGenerator;

    /**
     * @param testName
     */
    public ChangeTrackPieceCompositeMoveTest(java.lang.String testName) {
        super(testName);
    }

    /**
     * @param args
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * @return
     */
    public static Test suite() {

        return new TestSuite(
                ChangeTrackPieceCompositeMoveTest.class);
    }

    /**
     *
     */
    @Override
    protected void setUp() {
        super.setHasSetupBeenCalled(true);
        setWorld(new WorldImpl(10, 10));
        getWorld().set(ITEM.GAME_RULES, GameRules.DEFAULT_RULES);
        getWorld().addPlayer(MapFixtureFactory.TEST_PLAYER);
        MapFixtureFactory.generateTrackRuleList(getWorld());
        transactionsGenerator = new TrackMoveTransactionsGenerator(getWorld(),
                MapFixtureFactory.TEST_PRINCIPAL);
    }

    /**
     *
     */
    public void testRemoveTrack() {
        getWorld().set(ITEM.GAME_RULES, GameRules.NO_RESTRICTIONS);

        TrackRule trackRule = (TrackRule) getWorld().get(SKEY.TRACK_RULES, 0);

        assertBuildTrackSucceeds(new ImPoint(0, 5), east, trackRule);

        assertBuildTrackSucceeds(new ImPoint(0, 6), east, trackRule);
        assertBuildTrackSucceeds(new ImPoint(1, 6), east, trackRule);

        assertBuildTrackSucceeds(new ImPoint(0, 7), east, trackRule);
        assertBuildTrackSucceeds(new ImPoint(1, 7), east, trackRule);
        assertBuildTrackSucceeds(new ImPoint(2, 7), east, trackRule);

        // Remove only track piece built.
        assertRemoveTrackSucceeds(new ImPoint(0, 5), east);
        TrackConfiguration trackConfiguration = ((FreerailsTile) getWorld()
                .getTile(0, 5)).getTrackPiece().getTrackConfiguration();
        TrackConfiguration expected = NullTrackPiece.getInstance()
                .getTrackConfiguration();
        assertEquals(expected, trackConfiguration);
        TrackConfiguration trackConfiguration2 = ((FreerailsTile) getWorld()
                .getTile(1, 5)).getTrackPiece().getTrackConfiguration();

        assertEquals(expected, trackConfiguration2);
    }

    /**
     * All track except the first piece built should be connected to existing
     * track.
     */
    public void testMustConnect2ExistingTrack() {
        TrackRule trackRule = (TrackRule) world.get(SKEY.TRACK_RULES, 0);

        int numberOfTransactions = world
                .getNumberOfTransactions(MapFixtureFactory.TEST_PRINCIPAL);
        assertEquals(0, numberOfTransactions);

        boolean hasTrackBeenBuilt = ChangeTrackPieceCompositeMove
                .hasAnyTrackBeenBuilt(world, MapFixtureFactory.TEST_PRINCIPAL);
        assertFalse("No track has been built yet.", hasTrackBeenBuilt);
        assertBuildTrackSucceeds(new ImPoint(0, 5), east, trackRule);

        // Building the track should have added a transaction.
        numberOfTransactions = world
                .getNumberOfTransactions(MapFixtureFactory.TEST_PRINCIPAL);
        assertTrue(0 < numberOfTransactions);

        hasTrackBeenBuilt = ChangeTrackPieceCompositeMove.hasAnyTrackBeenBuilt(
                world, MapFixtureFactory.TEST_PRINCIPAL);
        assertTrue("One track piece has been built.", hasTrackBeenBuilt);

        assertBuildTrackSucceeds(new ImPoint(1, 5), east, trackRule);
        assertBuildTrackFails(new ImPoint(4, 8), east, trackRule);
    }

    /**
     *
     */
    public void testCannotConnect2OtherRRsTrack() {
        assertFalse(ChangeTrackPieceMove.canConnect2OtherRRsTrack(world));

        final int TRACK_RULE_ID = 0;
        TrackRule trackRule = (TrackRule) getWorld().get(SKEY.TRACK_RULES,
                TRACK_RULE_ID);

        assertBuildTrackSucceeds(new ImPoint(0, 6), east, trackRule);

        // Now change the owner of the track piece at (1, 6);
        int anotherPlayer = 999;
        FreerailsTile oldTile = (FreerailsTile) world.getTile(1, 6);
        TrackPiece tp = oldTile.getTrackPiece();
        TrackPiece newTrackPiece = new TrackPieceImpl(tp
                .getTrackConfiguration(), tp.getTrackRule(), anotherPlayer,
                TRACK_RULE_ID);
        FreerailsTile newTile = FreerailsTile.getInstance(oldTile
                .getTerrainTypeID(), newTrackPiece);
        world.setTile(1, 6, newTile);
        assertBuildTrackFails(new ImPoint(1, 6), east, trackRule);
        world.setTile(1, 6, oldTile);
        assertBuildTrackSucceeds(new ImPoint(1, 6), east, trackRule);
    }

    /**
     *
     */
    public void testBuildTrack() {
        ImPoint pointA = new ImPoint(0, 0);
        ImPoint pointB = new ImPoint(1, 1);
        ImPoint pointC = new ImPoint(1, 0);

        TrackRule trackRule = (TrackRule) getWorld().get(SKEY.TRACK_RULES, 0);

        // First track piece built
        assertBuildTrackSucceeds(pointA, southeast, trackRule);

        // Track connected from one existing track piece
        assertBuildTrackSucceeds(pointB, northeast, trackRule);

        // Track connected to one existing track piece
        // This is not going through for some reason, not sure why.
        // assertBuildTrackSucceeds(pointC, west, trackRule);
        // Track connecting two existing track pieces.
        assertBuildTrackSucceeds(pointA, east, trackRule);

        // Track off map.. should fail.
        assertBuildTrackFails(pointA, northeast, trackRule);

        // Track already there.
        assertBuildTrackFails(pointA, southeast, trackRule);

        // Illegal config. connecting from one existing track piece
        assertBuildTrackFails(pointA, south, trackRule);

        // Illegal config. connecting to one existing track piece
        assertBuildTrackFails(new ImPoint(0, 1), northeast, trackRule);

        // Illegal config. connecting between two existing track pieces
        assertBuildTrackFails(pointC, south, trackRule);

        // Not allowed on this terrain type, from existing track.
        assertBuildTrackFails(new ImPoint(2, 0), northeast,
                (TrackRule) getWorld().get(SKEY.TRACK_RULES, 1));
    }

    private void assertBuildTrackFails(ImPoint p, TileTransition v, TrackRule rule) {
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                .generateBuildTrackMove(p, v, rule, rule, getWorld(),
                        MapFixtureFactory.TEST_PRINCIPAL);
        MoveStatus status = move.doMove(getWorld(), Player.AUTHORITATIVE);
        assertEquals(false, status.isOk());
    }

    private void assertBuildTrackSucceeds(ImPoint p, TileTransition v, TrackRule rule) {
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                .generateBuildTrackMove(p, v, rule, rule, getWorld(),
                        MapFixtureFactory.TEST_PRINCIPAL);

        Move moveAndTransaction = transactionsGenerator.addTransactions(move);
        MoveStatus status = moveAndTransaction.doMove(getWorld(),
                Player.AUTHORITATIVE);
        assertEquals(true, status.isOk());
    }

    private void assertRemoveTrackSucceeds(ImPoint p, TileTransition v) {
        try {
            ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                    .generateRemoveTrackMove(p, v, getWorld(),
                            MapFixtureFactory.TEST_PRINCIPAL);
            MoveStatus status = move.doMove(getWorld(), Player.AUTHORITATIVE);
            assertEquals(true, status.isOk());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     *
     */
    @Override
    public void testMove() {
        ImPoint pointA = new ImPoint(0, 0);
        TrackRule trackRule = (TrackRule) getWorld().get(SKEY.TRACK_RULES, 0);

        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                .generateBuildTrackMove(pointA, southeast, trackRule,
                        trackRule, getWorld(), MapFixtureFactory.TEST_PRINCIPAL);

        assertSurvivesSerialisation(move);
        assertOkButNotRepeatable(move);

        setUp();
        assertDoThenUndoLeavesWorldUnchanged(move);
    }
}