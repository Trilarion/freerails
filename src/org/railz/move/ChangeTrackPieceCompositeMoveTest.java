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
package org.railz.move;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.awt.Point;
import org.railz.world.accounts.BankAccount;
import org.railz.world.common.*;
import org.railz.world.player.Player;
import org.railz.world.top.KEY;
import org.railz.world.track.TrackRule;

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
        byte east = CompassPoints.EAST;
        byte west = CompassPoints.WEST;

        assertBuildTrackSuceeds(new Point(0, 5), east, 0);

        assertBuildTrackSuceeds(new Point(0, 6), east, 0);
        assertBuildTrackSuceeds(new Point(1, 6), east, 0);

        assertBuildTrackSuceeds(new Point(0, 7), east, 0);
        assertBuildTrackSuceeds(new Point(1, 7), east, 0);
        assertBuildTrackSuceeds(new Point(2, 7), east, 0);

        //Remove only track piece built.
        assertRemoveTrackSuceeds(new Point(0, 5), east);
        assertTrue(null == getWorld().getTile(0, 5).getTrackTile());
        assertTrue(null == getWorld().getTile(1, 5).getTrackTile());
    }

    public void testBuildTrack() {
        Point pointA = new Point(0, 0);
        Point pointB = new Point(1, 1);
        Point pointC = new Point(1, 0);

        int trackRule = 0;

        //First track piece built
        assertBuildTrackSuceeds(pointA, CompassPoints.SOUTHEAST, trackRule);

        //Track connected from one existing track piece
        assertBuildTrackSuceeds(pointB, CompassPoints.NORTHEAST, trackRule);

        //Track connected to one existing track piece
        assertBuildTrackSuceeds(pointC, CompassPoints.EAST, trackRule);

        //Track connecting two existing track pieces.
        assertBuildTrackSuceeds(pointA, CompassPoints.EAST, trackRule);

        //Track already there.
	assertConstructBuildTrackMoveFails(pointA, CompassPoints.SOUTHEAST,
		trackRule);

        //Illegal config. connecting from one existing track piece
        assertBuildTrackFails(pointA, CompassPoints.SOUTH, trackRule);

        /*
	 * XXX This shouldl no longer fail
	 * Illegal config. connecting to one existing track piece
	assertBuildTrackFails(new Point(0, 1), CompassPoints.NORTHEAST,
		trackRule);
	 */

        //Illegal config. connecting between two existing track pieces
        assertBuildTrackFails(pointC, CompassPoints.SOUTH, trackRule);
    }

    private void assertConstructBuildTrackMoveFails(Point p, byte v, int rule)
	{
	    boolean failed = false;
	    try {
		ChangeTrackPieceCompositeMove move =
		    ChangeTrackPieceCompositeMove.generateBuildTrackMove(p,
			    v, rule, getWorld(), testPlayer.getPrincipal());
	    } catch (IllegalArgumentException e) {
		failed = true;
	    }
	    assertTrue(failed);
	}

    private void assertBuildTrackFails(Point p, byte v,
        int rule) {
        ChangeTrackPieceCompositeMove move =
	    ChangeTrackPieceCompositeMove.generateBuildTrackMove(p,
                v, rule, getWorld(), testPlayer.getPrincipal());
        MoveStatus status = move.doMove(getWorld(), testPlayer.getPrincipal());
        assertEquals(false, status.isOk());
    }

    private void assertBuildTrackSuceeds(Point p, byte v,
        int rule) {
	ChangeTrackPieceCompositeMove move =
	    ChangeTrackPieceCompositeMove.generateBuildTrackMove(p,
                v, rule, getWorld(), testPlayer.getPrincipal());
        MoveStatus status = move.doMove(getWorld(), testPlayer.getPrincipal());
        assertTrue(status.toString(), status.isOk());
    }

    private void assertRemoveTrackSuceeds(Point p, byte v) {
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

        int trackRule = 0;

	ChangeTrackPieceCompositeMove move =
	    ChangeTrackPieceCompositeMove.generateBuildTrackMove(pointA,
		CompassPoints.SOUTHEAST, trackRule, getWorld(),
		testPlayer.getPrincipal());

        assertEqualsSurvivesSerialisation(move);
        assertOkButNotRepeatable(move);

        setUp();
        assertDoThenUndoLeavesWorldUnchanged(move);
    }
}
