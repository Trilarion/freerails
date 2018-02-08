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
 * ChangeTrackPieceMoveTest.java
 * JUnit based test
 *
 */
package freerails.move;

import freerails.util.Point2D;
import freerails.world.FullWorld;
import freerails.world.game.GameRules;
import freerails.world.ITEM;
import freerails.world.SKEY;
import freerails.world.player.Player;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.MapFixtureFactory;
import freerails.world.track.TrackConfiguration;
import freerails.world.track.TrackPiece;
import freerails.world.track.TrackPieceImpl;
import freerails.world.track.TrackRule;

/**
 *
 */
public class ChangeTrackPieceMoveTest extends AbstractMoveTestCase {

    /**
     * @param testName
     */
    public ChangeTrackPieceMoveTest(String testName) {
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
    public static junit.framework.Test suite() {

        return new junit.framework.TestSuite(
                ChangeTrackPieceMoveTest.class);
    }

    /**
     *
     */
    @Override
    protected void setUp() {
        setHasSetupBeenCalled(true);
        setWorld(new FullWorld(20, 20));
        getWorld().set(ITEM.GAME_RULES, GameRules.NO_RESTRICTIONS);
        MapFixtureFactory.generateTrackRuleList(getWorld());
    }

    /**
     *
     */
    public void testTryDoMove() {
        setUp();

        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration newConfig;
        TrackMove move;
        MoveStatus moveStatus;

        // Try building the simplest piece of track.
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = ((FullTerrainTile) getWorld().getTile(Point2D.ZERO))
                .getTrackPiece();

        final int trackRuleID = 0;
        final TrackRule r = (TrackRule) getWorld().get(SKEY.TRACK_RULES,
                trackRuleID);

        newTrackPiece = new TrackPieceImpl(newConfig, r, 0, trackRuleID);
        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                Point2D.ZERO);
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(true, moveStatus.succeeds());

        // As above but with newTrackPiece and oldTrackPiece in the wrong order,
        // should fail.
        move = new ChangeTrackPieceMove(newTrackPiece, oldTrackPiece,
                Point2D.ZERO);
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(false, moveStatus.succeeds());

        // Try a move that does nothing, i.e. oldTrackPiece==newTrackPiece,
        // should fail.
        move = new ChangeTrackPieceMove(oldTrackPiece, oldTrackPiece,
                Point2D.ZERO);
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(false, moveStatus.succeeds());

        // Try to build track outside the map.
        move = new ChangeTrackPieceMove(newTrackPiece, oldTrackPiece,
                new Point2D(100, 0));
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(false, moveStatus.succeeds());

        // Try building an illegal track configuration.
        newConfig = TrackConfiguration.getFlatInstance("000011111");

        newTrackPiece = new TrackPieceImpl(newConfig, r, 0, trackRuleID);
        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                Point2D.ZERO);
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertEquals(false, moveStatus.succeeds());
    }

    /**
     *
     */
    public void testTryUndoMove() {
        setUp();
    }

    /**
     *
     */
    public void testDoMove() {
        setUp();

        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration newConfig;

        // Try building the simplest piece of track.
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = ((FullTerrainTile) getWorld().getTile(Point2D.ZERO))
                .getTrackPiece();

        TrackRule r = (TrackRule) getWorld().get(SKEY.TRACK_RULES, 0);
        newTrackPiece = new TrackPieceImpl(newConfig, r, 0, 0);

        assertMoveDoMoveIsOk(oldTrackPiece, newTrackPiece);
    }

    /**
     * @param oldTrackPiece
     * @param newTrackPiece
     */
    private void assertMoveDoMoveIsOk(TrackPiece oldTrackPiece,
                                      TrackPiece newTrackPiece) {
        TrackMove move;
        MoveStatus moveStatus;

        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                Point2D.ZERO);
        moveStatus = move.doMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(true, moveStatus.succeeds());
        TrackConfiguration actual = ((FullTerrainTile) getWorld().getTile(Point2D.ZERO))
                .getTrackPiece().getTrackConfiguration();
        assertEquals(newTrackPiece.getTrackConfiguration(), actual);
    }

    /**
     *
     */
    @Override
    public void testMove() {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration newConfig;
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = ((FullTerrainTile) getWorld().getTile(Point2D.ZERO))
                .getTrackPiece();

        TrackRule r = (TrackRule) getWorld().get(SKEY.TRACK_RULES, 0);
        newTrackPiece = new TrackPieceImpl(newConfig, r, 0, 0);

        Move move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                Point2D.ZERO);

        assertSurvivesSerialisation(move);

        assertOkButNotRepeatable(move);
    }
}