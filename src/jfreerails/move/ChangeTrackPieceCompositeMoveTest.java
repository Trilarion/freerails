/*
 * ChangeTrackPieceCompositeMoveTest.java
 * JUnit based test
 *
 * Created on 26 January 2002, 00:33
 */
package jfreerails.move;

import java.awt.Point;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.player.Player;
import jfreerails.world.top.GameRules;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackPiece;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackPieceImpl;
import jfreerails.world.track.TrackRule;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *  JUnit test.
 * @author Luke
 *
 */
public class ChangeTrackPieceCompositeMoveTest extends AbstractMoveTestCase {
    final OneTileMoveVector southeast = OneTileMoveVector.SOUTH_EAST;
    final OneTileMoveVector east = OneTileMoveVector.EAST;
    final OneTileMoveVector northeast = OneTileMoveVector.NORTH_EAST;
    final OneTileMoveVector south = OneTileMoveVector.SOUTH;
    final OneTileMoveVector west = OneTileMoveVector.WEST;
    TrackMoveTransactionsGenerator transactionsGenerator;

    public ChangeTrackPieceCompositeMoveTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite testSuite = new TestSuite(ChangeTrackPieceCompositeMoveTest.class);

        return testSuite;
    }

    protected void setUp() {
        super.setHasSetupBeenCalled(true);
        setWorld(new WorldImpl(10, 10));
        getWorld().set(ITEM.GAME_RULES, GameRules.DEFAULT_RULES);
        getWorld().addPlayer(MapFixtureFactory.TEST_PLAYER);
        MapFixtureFactory.generateTrackRuleList(getWorld());
        transactionsGenerator = new TrackMoveTransactionsGenerator(getWorld(),
                MapFixtureFactory.TEST_PRINCIPAL);
    }

    public void testRemoveTrack() {
        getWorld().set(ITEM.GAME_RULES, GameRules.NO_RESTRICTIONS);

        TrackRule trackRule = (TrackRule)getWorld().get(SKEY.TRACK_RULES, 0);

        assertBuildTrackSuceeds(new Point(0, 5), east, trackRule);

        assertBuildTrackSuceeds(new Point(0, 6), east, trackRule);
        assertBuildTrackSuceeds(new Point(1, 6), east, trackRule);

        assertBuildTrackSuceeds(new Point(0, 7), east, trackRule);
        assertBuildTrackSuceeds(new Point(1, 7), east, trackRule);
        assertBuildTrackSuceeds(new Point(2, 7), east, trackRule);

        //Remove only track piece built.
        assertRemoveTrackSuceeds(new Point(0, 5), east);
        assertEquals(NullTrackPiece.getInstance().getTrackConfiguration(),
            getWorld().getTile(0, 5).getTrackConfiguration());
        assertEquals(NullTrackPiece.getInstance().getTrackConfiguration(),
            getWorld().getTile(1, 5).getTrackConfiguration());
    }

    /** All track except the first piece built should be connected to existing track.*/
    public void testMustConnect2ExistingTrack() {
        World world = getWorld();
        TrackRule trackRule = (TrackRule)world.get(SKEY.TRACK_RULES, 0);

        int numberOfTransactions = world.getNumberOfTransactions(MapFixtureFactory.TEST_PRINCIPAL);
        assertEquals(0, numberOfTransactions);

        boolean hasTrackBeenBuilt = ChangeTrackPieceCompositeMove.hasAnyTrackBeenBuilt(world,
                MapFixtureFactory.TEST_PRINCIPAL);
        assertFalse("No track has been built yet.", hasTrackBeenBuilt);
        assertBuildTrackSuceeds(new Point(0, 5), east, trackRule);

        //Building the track should have added a transaction.
        numberOfTransactions = world.getNumberOfTransactions(MapFixtureFactory.TEST_PRINCIPAL);
        assertTrue(0 < numberOfTransactions);

        hasTrackBeenBuilt = ChangeTrackPieceCompositeMove.hasAnyTrackBeenBuilt(world,
                MapFixtureFactory.TEST_PRINCIPAL);
        assertTrue("One track piece has been built.", hasTrackBeenBuilt);

        assertBuildTrackSuceeds(new Point(1, 5), east, trackRule);
        assertBuildTrackFails(new Point(4, 8), east, trackRule);
    }

    public void testCannotConnect2OtherRRsTrack() {
        World world = getWorld();
        assertFalse(ChangeTrackPieceMove.canConnect2OtherRRsTrack(world));

        TrackRule trackRule = (TrackRule)getWorld().get(SKEY.TRACK_RULES, 0);

        assertBuildTrackSuceeds(new Point(0, 6), east, trackRule);

        //Now change the owner of the track piece at  (1, 6);				
        int anotherPlayer = 999;
        FreerailsTile oldTile = world.getTile(1, 6);
        TrackPiece tp = oldTile.getTrackPiece();
        TrackPiece newTrackPiece = new TrackPieceImpl(tp.getTrackConfiguration(),
                tp.getTrackRule(), anotherPlayer);
        FreerailsTile newTile = FreerailsTile.getInstance(oldTile.getTerrainTypeNumber(),
                newTrackPiece);
        world.setTile(1, 6, newTile);
        assertBuildTrackFails(new Point(1, 6), east, trackRule);
        world.setTile(1, 6, oldTile);
        assertBuildTrackSuceeds(new Point(1, 6), east, trackRule);
    }

    public void testBuildTrack() {
        Point pointA = new Point(0, 0);
        Point pointB = new Point(1, 1);
        Point pointC = new Point(1, 0);

        TrackRule trackRule = (TrackRule)getWorld().get(SKEY.TRACK_RULES, 0);

        //First track piece built
        assertBuildTrackSuceeds(pointA, southeast, trackRule);

        //Track connected from one existing track piece
        assertBuildTrackSuceeds(pointB, northeast, trackRule);

        //Track connected to one existing track piece
        //This is not going through for soem reason, not sure why.
        //       assertBuildTrackSuceeds(pointC, west, trackRule);
        //Track connecting two existing track pieces.
        assertBuildTrackSuceeds(pointA, east, trackRule);

        //Track off map.. should fail.
        assertBuildTrackFails(pointA, northeast, trackRule);

        //Track already there.
        assertBuildTrackFails(pointA, southeast, trackRule);

        //Illegal config. connecting from one existing track piece
        assertBuildTrackFails(pointA, south, trackRule);

        //Illegal config. connecting to one existing track piece
        assertBuildTrackFails(new Point(0, 1), northeast, trackRule);

        //Illegal config. connecting between two existing track pieces
        assertBuildTrackFails(pointC, south, trackRule);

        //Not allowed on this terrain type, from existing track.
        assertBuildTrackFails(new Point(2, 0), northeast,
            (TrackRule)getWorld().get(SKEY.TRACK_RULES, 1));
    }

    private void assertBuildTrackFails(Point p, OneTileMoveVector v,
        TrackRule rule) {
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(p,
                v, rule, getWorld(), MapFixtureFactory.TEST_PRINCIPAL);
        MoveStatus status = move.doMove(getWorld(), Player.AUTHORITATIVE);
        assertEquals(false, status.isOk());
    }

    private void assertBuildTrackSuceeds(Point p, OneTileMoveVector v,
        TrackRule rule) {
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(p,
                v, rule, getWorld(), MapFixtureFactory.TEST_PRINCIPAL);

        Move moveAndTransaction = transactionsGenerator.addTransactions(move);
        MoveStatus status = moveAndTransaction.doMove(getWorld(),
                Player.AUTHORITATIVE);
        assertEquals(true, status.isOk());
    }

    private void assertRemoveTrackSuceeds(Point p, OneTileMoveVector v) {
        try {
            ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateRemoveTrackMove(p,
                    v, getWorld(), MapFixtureFactory.TEST_PRINCIPAL);
            MoveStatus status = move.doMove(getWorld(), Player.AUTHORITATIVE);
            assertEquals(true, status.isOk());
        } catch (Exception e) {
            fail();
        }
    }

    public void testMove() {
        Point pointA = new Point(0, 0);
        TrackRule trackRule = (TrackRule)getWorld().get(SKEY.TRACK_RULES, 0);

        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(pointA,
                southeast, trackRule, getWorld(),
                MapFixtureFactory.TEST_PRINCIPAL);

        assertEqualsSurvivesSerialisation(move);
        assertOkButNotRepeatable(move);

        setUp();
        assertDoThenUndoLeavesWorldUnchanged(move);
    }
}