/*
 * ChangeTrackPieceCompositeMoveTest.java
 * JUnit based test
 *
 * Created on 26 January 2002, 00:33
 */
package jfreerails.move;

import java.awt.Point;
import jfreerails.world.accounts.BankAccount;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.top.KEY;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.track.NullTrackPiece;
import jfreerails.world.track.TrackRule;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 * @author lindsal
 */
public class ChangeTrackPieceCompositeMoveTest extends AbstractMoveTestCase {
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
        this.hasSetupBeenCalled = true;
        world = new WorldImpl(10, 10);
        world.add(KEY.BANK_ACCOUNTS, new BankAccount());
        MapFixtureFactory.generateTrackRuleList(world);
    }

    public void testRemoveTrack() {
        OneTileMoveVector east = OneTileMoveVector.EAST;
        OneTileMoveVector west = OneTileMoveVector.WEST;

        TrackRule trackRule = (TrackRule)world.get(KEY.TRACK_RULES, 0);

        assertBuildTrackSuceeds(new Point(0, 5), east, trackRule);

        assertBuildTrackSuceeds(new Point(0, 6), east, trackRule);
        assertBuildTrackSuceeds(new Point(1, 6), east, trackRule);

        assertBuildTrackSuceeds(new Point(0, 7), east, trackRule);
        assertBuildTrackSuceeds(new Point(1, 7), east, trackRule);
        assertBuildTrackSuceeds(new Point(2, 7), east, trackRule);

        //Remove only track piece built.
        assertRemoveTrackSuceeds(new Point(0, 5), east);
        assertEquals(NullTrackPiece.getInstance().getTrackConfiguration(),
            world.getTile(0, 5).getTrackConfiguration());
        assertEquals(NullTrackPiece.getInstance().getTrackConfiguration(),
            world.getTile(1, 5).getTrackConfiguration());
    }

    public void testBuildTrack() {
        Point pointA = new Point(0, 0);
        Point pointB = new Point(1, 1);
        Point pointC = new Point(1, 0);
        OneTileMoveVector southeast = OneTileMoveVector.SOUTH_EAST;
        OneTileMoveVector east = OneTileMoveVector.EAST;
        OneTileMoveVector northeast = OneTileMoveVector.NORTH_EAST;
        OneTileMoveVector south = OneTileMoveVector.SOUTH;
        OneTileMoveVector west = OneTileMoveVector.WEST;

        TrackRule trackRule = (TrackRule)world.get(KEY.TRACK_RULES, 0);

        //First track piece built
        assertBuildTrackSuceeds(pointA, southeast, trackRule);

        //Track connected from one existing track piece
        assertBuildTrackSuceeds(pointB, northeast, trackRule);

        //Track connected to one existing track piece
        assertBuildTrackSuceeds(pointC, east, trackRule);

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
            (TrackRule)world.get(KEY.TRACK_RULES, 1));

        //Implement these tests later.
        //Not allowed on this terrain type, to existing track.
        //assertBuildTrackFails(new Point(3, 0), west, trackRuleList.getTrackRule(1));
        //Not allowed on this terrain type, first track piece built.
        //assertBuildTrackFails(new Point(3, 1), east, trackRuleList.getTrackRule(1));
    }

    private void assertBuildTrackFails(Point p, OneTileMoveVector v,
        TrackRule rule) {
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(p,
                v, rule, world);
        MoveStatus status = move.doMove(world);
        assertEquals(false, status.isOk());
    }

    private void assertBuildTrackSuceeds(Point p, OneTileMoveVector v,
        TrackRule rule) {
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(p,
                v, rule, world);
        MoveStatus status = move.doMove(world);
        assertEquals(true, status.isOk());
    }

    private void assertRemoveTrackFails(Point p, OneTileMoveVector v) {
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateRemoveTrackMove(p,
                v, world);
        MoveStatus status = move.doMove(world);
        assertEquals(false, status.isOk());
    }

    private void assertRemoveTrackSuceeds(Point p, OneTileMoveVector v) {
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateRemoveTrackMove(p,
                v, world);
        MoveStatus status = move.doMove(world);
        assertEquals(true, status.isOk());
    }

    public void testMove() {
        Point pointA = new Point(0, 0);
        Point pointB = new Point(1, 1);
        Point pointC = new Point(1, 0);
        OneTileMoveVector southeast = OneTileMoveVector.SOUTH_EAST;
        OneTileMoveVector east = OneTileMoveVector.EAST;
        OneTileMoveVector northeast = OneTileMoveVector.NORTH_EAST;
        OneTileMoveVector south = OneTileMoveVector.SOUTH;
        OneTileMoveVector west = OneTileMoveVector.WEST;

        TrackRule trackRule = (TrackRule)world.get(KEY.TRACK_RULES, 0);

        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(pointA,
                southeast, trackRule, world);

        assertEqualsSurvivesSerialisation(move);
        assertOkButNotRepeatable(move);

        setUp();
        assertDoThenUndoLeavesWorldUnchanged(move);
    }
}