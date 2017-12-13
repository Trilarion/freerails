/*
 * Copyright (C) 2002 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * ChangeTrackPieceCompositeMoveTest.java
 * JUnit based test
 *
 * Created on 26 January 2002, 00:33
 */
package jfreerails.move;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.awt.Point;
import jfreerails.world.accounts.BankAccount;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.player.Player;
import jfreerails.world.top.KEY;
import jfreerails.world.track.NullTrackPiece;
import jfreerails.world.track.TrackRule;


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
        super.setUp();
    }

    public void testRemoveTrack() {
        OneTileMoveVector east = OneTileMoveVector.EAST;
        OneTileMoveVector west = OneTileMoveVector.WEST;

        TrackRule trackRule = (TrackRule)getWorld().get(KEY.TRACK_RULES, 0);

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

    public void testBuildTrack() {
        Point pointA = new Point(0, 0);
        Point pointB = new Point(1, 1);
        Point pointC = new Point(1, 0);
        OneTileMoveVector southeast = OneTileMoveVector.SOUTH_EAST;
        OneTileMoveVector east = OneTileMoveVector.EAST;
        OneTileMoveVector northeast = OneTileMoveVector.NORTH_EAST;
        OneTileMoveVector south = OneTileMoveVector.SOUTH;
        OneTileMoveVector west = OneTileMoveVector.WEST;

        TrackRule trackRule = (TrackRule)getWorld().get(KEY.TRACK_RULES, 0);

        //First track piece built
        assertBuildTrackSuceeds(pointA, southeast, trackRule);

        //Track connected from one existing track piece
        assertBuildTrackSuceeds(pointB, northeast, trackRule);

        //Track connected to one existing track piece
        assertBuildTrackSuceeds(pointC, east, trackRule);

        //Track connecting two existing track pieces.
        assertBuildTrackSuceeds(pointA, east, trackRule);

        //Track already there.
        assertBuildTrackFails(pointA, southeast, trackRule);

        //Illegal config. connecting from one existing track piece
        assertBuildTrackFails(pointA, south, trackRule);

        //Illegal config. connecting to one existing track piece
        assertBuildTrackFails(new Point(0, 1), northeast, trackRule);

        //Illegal config. connecting between two existing track pieces
        assertBuildTrackFails(pointC, south, trackRule);

        //Not allowed on this terrain type, from existing track.
        //assertBuildTrackFails(new Point(2, 0), northeast,
        //    (TrackRule)getWorld().get(KEY.TRACK_RULES, 1));
    }

    private void assertBuildTrackFails(Point p, OneTileMoveVector v,
        TrackRule rule) {
        ChangeTrackPieceCompositeMove move =
	    ChangeTrackPieceCompositeMove.generateBuildTrackMove(p,
                v, rule, getWorld(), testPlayer.getPrincipal());
        MoveStatus status = move.doMove(getWorld(), testPlayer.getPrincipal());
        assertEquals(false, status.isOk());
    }

    private void assertBuildTrackSuceeds(Point p, OneTileMoveVector v,
        TrackRule rule) {
	ChangeTrackPieceCompositeMove move =
	    ChangeTrackPieceCompositeMove.generateBuildTrackMove(p,
                v, rule, getWorld(), testPlayer.getPrincipal());
        MoveStatus status = move.doMove(getWorld(), testPlayer.getPrincipal());
        assertEquals(true, status.isOk());
    }

    private void assertRemoveTrackSuceeds(Point p, OneTileMoveVector v) {
	ChangeTrackPieceCompositeMove move =
	    ChangeTrackPieceCompositeMove.generateRemoveTrackMove(p, v,
		    getWorld(), testPlayer.getPrincipal());
        MoveStatus status = move.doMove(getWorld(), testPlayer.getPrincipal());
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

        TrackRule trackRule = (TrackRule)getWorld().get(KEY.TRACK_RULES, 0);

	ChangeTrackPieceCompositeMove move =
	    ChangeTrackPieceCompositeMove.generateBuildTrackMove(pointA,
                southeast, trackRule, getWorld(), testPlayer.getPrincipal());

        assertEqualsSurvivesSerialisation(move);
        assertOkButNotRepeatable(move);

        setUp();
        assertDoThenUndoLeavesWorldUnchanged(move);
    }
}
