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

import freerails.util.Vector2D;
import freerails.world.*;
import freerails.world.game.GameRules;
import freerails.world.player.Player;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.terrain.TileTransition;
import freerails.world.track.*;

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
        setWorld(new FullWorld(10, 10));
        getWorld().set(ITEM.GAME_RULES, GameRules.DEFAULT_RULES);
        getWorld().addPlayer(MapFixtureFactory.TEST_PLAYER);
        MapFixtureFactory.generateTrackRuleList(getWorld());
        transactionsGenerator = new TrackMoveTransactionsGenerator(getWorld(), MapFixtureFactory.TEST_PRINCIPAL);
    }

    /**
     *
     */
    public void testRemoveTrack() {
        getWorld().set(ITEM.GAME_RULES, GameRules.NO_RESTRICTIONS);

        TrackRule trackRule = (TrackRule) getWorld().get(SKEY.TRACK_RULES, 0);

        assertBuildTrackSucceeds(new Vector2D(0, 5), TileTransition.EAST, trackRule);

        assertBuildTrackSucceeds(new Vector2D(0, 6), TileTransition.EAST, trackRule);
        assertBuildTrackSucceeds(new Vector2D(1, 6), TileTransition.EAST, trackRule);

        assertBuildTrackSucceeds(new Vector2D(0, 7), TileTransition.EAST, trackRule);
        assertBuildTrackSucceeds(new Vector2D(1, 7), TileTransition.EAST, trackRule);
        assertBuildTrackSucceeds(new Vector2D(2, 7), TileTransition.EAST, trackRule);

        // Remove only track piece built.
        assertRemoveTrackSucceeds(new Vector2D(0, 5), TileTransition.EAST);
        TrackConfiguration trackConfiguration = ((FullTerrainTile) getWorld()
                .getTile(new Vector2D(0, 5))).getTrackPiece().getTrackConfiguration();
        TrackConfiguration expected = NullTrackPiece.getInstance().getTrackConfiguration();
        assertEquals(expected, trackConfiguration);
        TrackConfiguration trackConfiguration2 = ((FullTerrainTile) getWorld()
                .getTile(new Vector2D(1, 5))).getTrackPiece().getTrackConfiguration();

        assertEquals(expected, trackConfiguration2);
    }

    /**
     * All track except the first piece built should be connected to existing
     * track.
     */
    public void testMustConnect2ExistingTrack() {
        TrackRule trackRule = (TrackRule) world.get(SKEY.TRACK_RULES, 0);

        int numberOfTransactions = world.getNumberOfTransactions(MapFixtureFactory.TEST_PRINCIPAL);
        assertEquals(0, numberOfTransactions);

        boolean hasTrackBeenBuilt = ChangeTrackPieceCompositeMove
                .hasAnyTrackBeenBuilt(world, MapFixtureFactory.TEST_PRINCIPAL);
        assertFalse("No track has been built yet.", hasTrackBeenBuilt);
        assertBuildTrackSucceeds(new Vector2D(0, 5), TileTransition.EAST, trackRule);

        // Building the track should have added a transaction.
        numberOfTransactions = world.getNumberOfTransactions(MapFixtureFactory.TEST_PRINCIPAL);
        assertTrue(0 < numberOfTransactions);

        hasTrackBeenBuilt = ChangeTrackPieceCompositeMove.hasAnyTrackBeenBuilt(
                world, MapFixtureFactory.TEST_PRINCIPAL);
        assertTrue("One track piece has been built.", hasTrackBeenBuilt);

        assertBuildTrackSucceeds(new Vector2D(1, 5), TileTransition.EAST, trackRule);
        assertBuildTrackFails(new Vector2D(4, 8), TileTransition.EAST, trackRule);
    }

    /**
     *
     */
    public void testCannotConnect2OtherRRsTrack() {
        assertFalse(ChangeTrackPieceMove.canConnect2OtherRRsTrack(world));
        final int TRACK_RULE_ID = 0;
        TrackRule trackRule = (TrackRule) getWorld().get(SKEY.TRACK_RULES, TRACK_RULE_ID);

        assertBuildTrackSucceeds(new Vector2D(0, 6), TileTransition.EAST, trackRule);
        // Now change the owner of the track piece at (1, 6);
        int anotherPlayer = 999;
        FullTerrainTile oldTile = (FullTerrainTile) world.getTile(new Vector2D(1, 6));
        TrackPiece tp = oldTile.getTrackPiece();
        TrackPiece newTrackPiece = new TrackPieceImpl(tp.getTrackConfiguration(), tp.getTrackRule(), anotherPlayer,
                TRACK_RULE_ID);
        FullTerrainTile newTile = FullTerrainTile.getInstance(oldTile.getTerrainTypeID(), newTrackPiece);
        world.setTile(new Vector2D(1, 6), newTile);
        assertBuildTrackFails(new Vector2D(1, 6), TileTransition.EAST, trackRule);
        world.setTile(new Vector2D(1, 6), oldTile);
        assertBuildTrackSucceeds(new Vector2D(1, 6), TileTransition.EAST, trackRule);
    }

    /**
     *
     */
    public void testBuildTrack() {
        Vector2D pointA = Vector2D.ZERO;
        Vector2D pointB = new Vector2D(1, 1);
        Vector2D pointC = new Vector2D(1, 0);

        TrackRule trackRule = (TrackRule) getWorld().get(SKEY.TRACK_RULES, 0);

        // First track piece built
        assertBuildTrackSucceeds(pointA, TileTransition.SOUTH_EAST, trackRule);

        // Track connected from one existing track piece
        assertBuildTrackSucceeds(pointB, TileTransition.NORTH_EAST, trackRule);

        // Track connected to one existing track piece
        // This is not going through for some reason, not sure why.
        // assertBuildTrackSucceeds(pointC, west, trackRule);
        // Track connecting two existing track pieces.
        assertBuildTrackSucceeds(pointA, TileTransition.EAST, trackRule);

        // Track off map.. should fail.
        assertBuildTrackFails(pointA, TileTransition.NORTH_EAST, trackRule);

        // Track already there.
        assertBuildTrackFails(pointA, TileTransition.SOUTH_EAST, trackRule);

        // Illegal config. connecting from one existing track piece
        assertBuildTrackFails(pointA, TileTransition.SOUTH, trackRule);

        // Illegal config. connecting to one existing track piece
        assertBuildTrackFails(new Vector2D(0, 1), TileTransition.NORTH_EAST, trackRule);

        // Illegal config. connecting between two existing track pieces
        assertBuildTrackFails(pointC, TileTransition.SOUTH, trackRule);

        // Not allowed on this terrain type, from existing track.
        assertBuildTrackFails(new Vector2D(2, 0), TileTransition.NORTH_EAST,
                (TrackRule) getWorld().get(SKEY.TRACK_RULES, 1));
    }

    private void assertBuildTrackFails(Vector2D p, TileTransition v, TrackRule rule) {
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                .generateBuildTrackMove(p, v, rule, rule, getWorld(),
                        MapFixtureFactory.TEST_PRINCIPAL);
        MoveStatus status = move.doMove(getWorld(), Player.AUTHORITATIVE);
        assertFalse(status.succeeds());
    }

    private void assertBuildTrackSucceeds(Vector2D p, TileTransition v, TrackRule rule) {
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                .generateBuildTrackMove(p, v, rule, rule, getWorld(),
                        MapFixtureFactory.TEST_PRINCIPAL);

        Move moveAndTransaction = transactionsGenerator.addTransactions(move);
        MoveStatus status = moveAndTransaction.doMove(getWorld(),
                Player.AUTHORITATIVE);
        assertEquals(true, status.succeeds());
    }

    private void assertRemoveTrackSucceeds(Vector2D p, TileTransition v) {
        try {
            ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                    .generateRemoveTrackMove(p, v, getWorld(), MapFixtureFactory.TEST_PRINCIPAL);
            MoveStatus status = move.doMove(getWorld(), Player.AUTHORITATIVE);
            assertEquals(true, status.succeeds());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     *
     */
    public void testMove() throws Exception {
        Vector2D pointA = Vector2D.ZERO;
        TrackRule trackRule = (TrackRule) getWorld().get(SKEY.TRACK_RULES, 0);

        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                .generateBuildTrackMove(pointA, TileTransition.SOUTH_EAST, trackRule, trackRule, getWorld(), MapFixtureFactory.TEST_PRINCIPAL);

        assertSurvivesSerialisation(move);
        assertOkButNotRepeatable(move);

        // TODO do we really need to call setUp again here?
        setUp();
        assertDoThenUndoLeavesWorldUnchanged(move);
    }
}