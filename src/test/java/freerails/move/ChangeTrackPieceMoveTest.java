/*
 * ChangeTrackPieceMoveTest.java
 * JUnit based test
 *
 * Created on 24 January 2002, 23:57
 */
package freerails.move;

import freerails.world.common.ImPoint;
import freerails.world.player.Player;
import freerails.world.top.*;
import freerails.world.track.*;

/**
 * JUnit test.
 *
 * @author Luke
 */
public class ChangeTrackPieceMoveTest extends AbstractMoveTestCase {
    public ChangeTrackPieceMoveTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        junit.framework.TestSuite testSuite = new junit.framework.TestSuite(
                ChangeTrackPieceMoveTest.class);

        return testSuite;
    }

    @Override
    protected void setUp() {
        setHasSetupBeenCalled(true);
        setWorld(new WorldImpl(20, 20));
        getWorld().set(ITEM.GAME_RULES, GameRules.NO_RESTRICTIONS);
        MapFixtureFactory.generateTrackRuleList(getWorld());
    }

    public void testTryDoMove() {
        setUp();

        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration newConfig;
        TrackMove move;
        MoveStatus moveStatus;

        // Try building the simplest piece of track.
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = ((FreerailsTile) getWorld().getTile(0, 0))
                .getTrackPiece();

        final int trackRuleID = 0;
        final TrackRule r = (TrackRule) getWorld().get(SKEY.TRACK_RULES,
                trackRuleID);

        newTrackPiece = new TrackPieceImpl(newConfig, r, 0, trackRuleID);
        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new ImPoint(0, 0));
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(true, moveStatus.isOk());

        // As above but with newTrackPiece and oldTrackPiece in the wrong order,
        // should fail.
        move = new ChangeTrackPieceMove(newTrackPiece, oldTrackPiece,
                new ImPoint(0, 0));
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(false, moveStatus.isOk());

        // Try a move that does nothing, i.e. oldTrackPiece==newTrackPiece,
        // should fail.
        move = new ChangeTrackPieceMove(oldTrackPiece, oldTrackPiece,
                new ImPoint(0, 0));
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(false, moveStatus.isOk());

        // Try buildingtrack outside the map.
        move = new ChangeTrackPieceMove(newTrackPiece, oldTrackPiece,
                new ImPoint(100, 0));
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(false, moveStatus.isOk());

        // Try building an illegal track configuration.
        newConfig = TrackConfiguration.getFlatInstance("000011111");

        newTrackPiece = new TrackPieceImpl(newConfig, r, 0, trackRuleID);
        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new ImPoint(0, 0));
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertEquals(false, moveStatus.isOk());
    }

    public void testTryUndoMove() {
        setUp();
    }

    public void testDoMove() {
        setUp();

        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration newConfig;

        // Try building the simplest piece of track.
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = ((FreerailsTile) getWorld().getTile(0, 0))
                .getTrackPiece();

        TrackRule r = (TrackRule) getWorld().get(SKEY.TRACK_RULES, 0);
        newTrackPiece = new TrackPieceImpl(newConfig, r, 0, 0);

        assertMoveDoMoveIsOk(oldTrackPiece, newTrackPiece);
    }

    protected void assertMoveDoMoveIsOk(TrackPiece oldTrackPiece,
                                        TrackPiece newTrackPiece) {
        TrackMove move;
        MoveStatus moveStatus;

        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new ImPoint(0, 0));
        moveStatus = move.doMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(true, moveStatus.isOk());
        TrackConfiguration actual = ((FreerailsTile) getWorld().getTile(0, 0))
                .getTrackPiece().getTrackConfiguration();
        assertEquals(newTrackPiece.getTrackConfiguration(), actual);
    }

    @Override
    public void testMove() {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration newConfig;
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = ((FreerailsTile) getWorld().getTile(0, 0))
                .getTrackPiece();

        TrackRule r = (TrackRule) getWorld().get(SKEY.TRACK_RULES, 0);
        newTrackPiece = new TrackPieceImpl(newConfig, r, 0, 0);

        Move move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new ImPoint(0, 0));

        assertSurvivesSerialisation(move);

        assertOkButNotRepeatable(move);
    }
}