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

import freerails.util.Point2D;
import freerails.world.*;
import freerails.world.game.GameRules;
import freerails.world.player.Player;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.terrain.TileTransition;
import freerails.world.top.MapFixtureFactory;
import freerails.world.track.*;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test.
 */
public class ChangeTrackPieceCompositeMoveTest extends AbstractMoveTestCase {
    private final TileTransition southeast = TileTransition.SOUTH_EAST;

    private final TileTransition east = TileTransition.EAST;

    private final TileTransition northeast = TileTransition.NORTH_EAST;

    private final TileTransition south = TileTransition.SOUTH;

    private TrackMoveTransactionsGenerator transactionsGenerator;

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

        assertBuildTrackSucceeds(new Point2D(0, 5), east, trackRule);

        assertBuildTrackSucceeds(new Point2D(0, 6), east, trackRule);
        assertBuildTrackSucceeds(new Point2D(1, 6), east, trackRule);

        assertBuildTrackSucceeds(new Point2D(0, 7), east, trackRule);
        assertBuildTrackSucceeds(new Point2D(1, 7), east, trackRule);
        assertBuildTrackSucceeds(new Point2D(2, 7), east, trackRule);

        // Remove only track piece built.
        assertRemoveTrackSucceeds(new Point2D(0, 5), east);
        TrackConfiguration trackConfiguration = ((FullTerrainTile) getWorld()
                .getTile(0, 5)).getTrackPiece().getTrackConfiguration();
        TrackConfiguration expected = NullTrackPiece.getInstance()
                .getTrackConfiguration();
        assertEquals(expected, trackConfiguration);
        TrackConfiguration trackConfiguration2 = ((FullTerrainTile) getWorld()
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
        assertBuildTrackSucceeds(new Point2D(0, 5), east, trackRule);

        // Building the track should have added a transaction.
        numberOfTransactions = world
                .getNumberOfTransactions(MapFixtureFactory.TEST_PRINCIPAL);
        assertTrue(0 < numberOfTransactions);

        hasTrackBeenBuilt = ChangeTrackPieceCompositeMove.hasAnyTrackBeenBuilt(
                world, MapFixtureFactory.TEST_PRINCIPAL);
        assertTrue("One track piece has been built.", hasTrackBeenBuilt);

        assertBuildTrackSucceeds(new Point2D(1, 5), east, trackRule);
        assertBuildTrackFails(new Point2D(4, 8), east, trackRule);
    }

    /**
     *
     */
    public void testCannotConnect2OtherRRsTrack() {
        assertFalse(ChangeTrackPieceMove.canConnect2OtherRRsTrack(world));

        final int TRACK_RULE_ID = 0;
        TrackRule trackRule = (TrackRule) getWorld().get(SKEY.TRACK_RULES,
                TRACK_RULE_ID);

        assertBuildTrackSucceeds(new Point2D(0, 6), east, trackRule);

        // Now change the owner of the track piece at (1, 6);
        int anotherPlayer = 999;
        FullTerrainTile oldTile = (FullTerrainTile) world.getTile(1, 6);
        TrackPiece tp = oldTile.getTrackPiece();
        TrackPiece newTrackPiece = new TrackPieceImpl(tp
                .getTrackConfiguration(), tp.getTrackRule(), anotherPlayer,
                TRACK_RULE_ID);
        FullTerrainTile newTile = FullTerrainTile.getInstance(oldTile
                .getTerrainTypeID(), newTrackPiece);
        world.setTile(1, 6, newTile);
        assertBuildTrackFails(new Point2D(1, 6), east, trackRule);
        world.setTile(1, 6, oldTile);
        assertBuildTrackSucceeds(new Point2D(1, 6), east, trackRule);
    }

    /**
     *
     */
    public void testBuildTrack() {
        Point2D pointA = new Point2D(0, 0);
        Point2D pointB = new Point2D(1, 1);
        Point2D pointC = new Point2D(1, 0);

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
        assertBuildTrackFails(new Point2D(0, 1), northeast, trackRule);

        // Illegal config. connecting between two existing track pieces
        assertBuildTrackFails(pointC, south, trackRule);

        // Not allowed on this terrain type, from existing track.
        assertBuildTrackFails(new Point2D(2, 0), northeast,
                (TrackRule) getWorld().get(SKEY.TRACK_RULES, 1));
    }

    private void assertBuildTrackFails(Point2D p, TileTransition v, TrackRule rule) {
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                .generateBuildTrackMove(p, v, rule, rule, getWorld(),
                        MapFixtureFactory.TEST_PRINCIPAL);
        MoveStatus status = move.doMove(getWorld(), Player.AUTHORITATIVE);
        assertEquals(false, status.succeeds());
    }

    private void assertBuildTrackSucceeds(Point2D p, TileTransition v, TrackRule rule) {
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                .generateBuildTrackMove(p, v, rule, rule, getWorld(),
                        MapFixtureFactory.TEST_PRINCIPAL);

        Move moveAndTransaction = transactionsGenerator.addTransactions(move);
        MoveStatus status = moveAndTransaction.doMove(getWorld(),
                Player.AUTHORITATIVE);
        assertEquals(true, status.succeeds());
    }

    private void assertRemoveTrackSucceeds(Point2D p, TileTransition v) {
        try {
            ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                    .generateRemoveTrackMove(p, v, getWorld(),
                            MapFixtureFactory.TEST_PRINCIPAL);
            MoveStatus status = move.doMove(getWorld(), Player.AUTHORITATIVE);
            assertEquals(true, status.succeeds());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     *
     */
    @Override
    public void testMove() {
        Point2D pointA = new Point2D(0, 0);
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