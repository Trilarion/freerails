/*
 * ChangeTrackPieceMoveTest.java
 * JUnit based test
 *
 * Created on 24 January 2002, 23:57
 */
package jfreerails.move;

import java.awt.Point;
import jfreerails.world.player.Player;
import jfreerails.world.top.GameRules;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;


/**
 *  JUnit test.
 * @author Luke
 *
 */
public class ChangeTrackPieceMoveTest extends AbstractMoveTestCase {
    public ChangeTrackPieceMoveTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        junit.framework.TestSuite testSuite = new junit.framework.TestSuite(ChangeTrackPieceMoveTest.class);

        return testSuite;
    }

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

        //Try building the simplest piece of track.
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = getWorld().getTile(0, 0);

        TrackRule r = (TrackRule)getWorld().get(SKEY.TRACK_RULES, 0);
        newTrackPiece = r.getTrackPiece(newConfig, 0);
        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new Point(0, 0));
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(true, moveStatus.isOk());

        //As above but with newTrackPiece and oldTrackPiece in the wrong order, should fail.
        move = new ChangeTrackPieceMove(newTrackPiece, oldTrackPiece,
                new Point(0, 0));
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(false, moveStatus.isOk());

        //Try a move that does nothing, i.e. oldTrackPiece==newTrackPiece, should fail.
        move = new ChangeTrackPieceMove(oldTrackPiece, oldTrackPiece,
                new Point(0, 0));
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(false, moveStatus.isOk());

        //Try buildingtrack outside the map.
        move = new ChangeTrackPieceMove(newTrackPiece, oldTrackPiece,
                new Point(100, 0));
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(false, moveStatus.isOk());

        //Try building an illegal track configuration.
        newConfig = TrackConfiguration.getFlatInstance("000011111");

        r = (TrackRule)getWorld().get(SKEY.TRACK_RULES, 0);
        newTrackPiece = r.getTrackPiece(newConfig, 0);
        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new Point(0, 0));
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

        //Try building the simplest piece of track.
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = getWorld().getTile(0, 0);

        TrackRule r = (TrackRule)getWorld().get(SKEY.TRACK_RULES, 0);
        newTrackPiece = r.getTrackPiece(newConfig, 0);

        assertMoveDoMoveIsOk(oldTrackPiece, newTrackPiece);
    }

    protected void assertMoveDoMoveIsOk(TrackPiece oldTrackPiece,
        TrackPiece newTrackPiece) {
        TrackMove move;
        MoveStatus moveStatus;

        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new Point(0, 0));
        moveStatus = move.doMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(true, moveStatus.isOk());
        assertEquals(newTrackPiece.getTrackConfiguration(),
            getWorld().getTile(0, 0).getTrackConfiguration());
    }

    public void testMove() {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration newConfig;
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = getWorld().getTile(0, 0);

        TrackRule r = (TrackRule)getWorld().get(SKEY.TRACK_RULES, 0);
        newTrackPiece = r.getTrackPiece(newConfig, 0);

        Move move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new Point(0, 0));

        assertEqualsSurvivesSerialisation(move);

        assertOkButNotRepeatable(move);
    }
}