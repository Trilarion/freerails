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
 * ChangeTrackPieceMoveTest.java
 * JUnit based test
 *
 * Created on 24 January 2002, 23:57
 */
package org.railz.move;

import java.awt.Point;

import org.railz.world.common.*;
import org.railz.world.top.KEY;
import org.railz.world.top.MapFixtureFactory;
import org.railz.world.top.WorldImpl;
import org.railz.world.track.*;

/**
 *
 * @author lindsal
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
	MapFixtureFactory mff = new MapFixtureFactory(20,20);
        setWorld(mff.world);
    }

    public void testTryDoMove() {
        setUp();

        TrackTile oldTrackPiece;
        TrackTile newTrackPiece;
        byte oldConfig;
        byte newConfig;
        TrackMove move;
        MoveStatus moveStatus;

        //Try building the simplest piece of track.
        newConfig = CompassPoints.NORTH;
	FreerailsTile ft = getWorld().getTile(0, 0);
	ft.setOwner(testPlayer.getPrincipal());
        oldTrackPiece = ft.getTrackTile();

        newTrackPiece = TrackTile.createTrackTile(getWorld(), newConfig, 0);
        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new Point(0, 0), testPlayer.getPrincipal());
        moveStatus = move.tryDoMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(moveStatus);
        assertEquals(true, moveStatus.isOk());

        //As above but with newTrackPiece and oldTrackPiece in the wrong order, should fail.
        move = new ChangeTrackPieceMove(newTrackPiece, oldTrackPiece,
                new Point(0, 0), testPlayer.getPrincipal());
        moveStatus = move.tryDoMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(moveStatus);
        assertEquals(false, moveStatus.isOk());

        //Try a move that does nothing, i.e. oldTrackPiece==newTrackPiece, should fail.
	boolean failed = false;
	try {
	    move = new ChangeTrackPieceMove(oldTrackPiece, oldTrackPiece,
		    new Point(0, 0), testPlayer.getPrincipal());
	} catch (IllegalArgumentException e) {
	    failed = true;
	}
	assertTrue(failed);

        //Try buildingtrack outside the map.
        move = new ChangeTrackPieceMove(newTrackPiece, oldTrackPiece,
                new Point(100, 0), testPlayer.getPrincipal());
        moveStatus = move.tryDoMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(moveStatus);
        assertEquals(false, moveStatus.isOk());

        //Try building an illegal track configuration.
	newConfig = CompassPoints.SOUTHEAST | CompassPoints.SOUTH |
	    CompassPoints.SOUTHWEST | CompassPoints.WEST;

        newTrackPiece = TrackTile.createTrackTile(getWorld(), newConfig, 0);
        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new Point(0, 0), testPlayer.getPrincipal());
        moveStatus = move.tryDoMove(getWorld(), testPlayer.getPrincipal());
        assertEquals(false, moveStatus.isOk());
    }

    public void testDoMove() {
        setUp();

        TrackTile oldTrackPiece;
        TrackTile newTrackPiece;
        byte oldConfig;
        byte newConfig;
        TrackMove move;
        MoveStatus moveStatus;

        //Try building the simplest piece of track.
        newConfig = CompassPoints.NORTH;
	getWorld().getTile(0, 0).setOwner(testPlayer.getPrincipal());
        oldTrackPiece = getWorld().getTile(0, 0).getTrackTile();

        newTrackPiece = TrackTile.createTrackTile(getWorld(), newConfig, 0);

        assertMoveDoMoveIsOk(oldTrackPiece, newTrackPiece);
    }

    protected void assertMoveDoMoveIsOk(TrackTile oldTrackPiece,
        TrackTile newTrackPiece) {
        TrackMove move;
        MoveStatus moveStatus;

        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new Point(0, 0), testPlayer.getPrincipal());
        moveStatus = move.doMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(moveStatus);
        assertTrue(moveStatus.toString(), moveStatus.isOk());
        assertEquals(newTrackPiece.getTrackConfiguration(),
            getWorld().getTile(0, 0).getTrackConfiguration());
    }

    public void testUndoMove() {
        //Exactly like testDoMove() v
        setUp();

        TrackTile oldTrackPiece;
        TrackTile newTrackPiece;
        byte oldConfig;
        byte newConfig;
        TrackMove move;
        MoveStatus moveStatus;

	getWorld().getTile(0, 0).setOwner(testPlayer.getPrincipal());
        //Try building the simplest piece of track.
        newConfig = CompassPoints.NORTH;
        oldTrackPiece = getWorld().getTile(0, 0).getTrackTile();

        //  assertMoveDoMoveIsOk(oldTrackPiece, newConfig);
    }

    public void testMove() {
        TrackTile oldTrackPiece;
        TrackTile newTrackPiece;
        byte oldConfig;
        byte newConfig;
        newConfig = CompassPoints.NORTH;
        oldTrackPiece = getWorld().getTile(0, 0).getTrackTile();

        newTrackPiece = TrackTile.createTrackTile(getWorld(), newConfig, 0);

	getWorld().getTile(0, 0).setOwner(testPlayer.getPrincipal());
        Move move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new Point(0, 0), testPlayer.getPrincipal());

        assertEqualsSurvivesSerialisation(move);

        assertOkButNotRepeatable(move);
    }
}
