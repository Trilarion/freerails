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
package jfreerails.move;

import java.awt.Point;

import jfreerails.world.top.KEY;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;


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
        setWorld(new WorldImpl(20, 20));
        MapFixtureFactory.generateTrackRuleList(getWorld());
    }

    public void testTryDoMove() {
        setUp();

        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration oldConfig;
        TrackConfiguration newConfig;
        TrackMove move;
        MoveStatus moveStatus;

        //Try building the simplest piece of track.
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = (TrackPiece)getWorld().getTile(0, 0);
	((FreerailsTile) oldTrackPiece).setOwner(testPlayer.getPrincipal());

        TrackRule r = (TrackRule)getWorld().get(KEY.TRACK_RULES, 0);
        newTrackPiece = r.getTrackPiece(newConfig);
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
        move = new ChangeTrackPieceMove(oldTrackPiece, oldTrackPiece,
                new Point(0, 0), testPlayer.getPrincipal());
        moveStatus = move.tryDoMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(moveStatus);
        assertEquals(false, moveStatus.isOk());

        //Try buildingtrack outside the map.
        move = new ChangeTrackPieceMove(newTrackPiece, oldTrackPiece,
                new Point(100, 0), testPlayer.getPrincipal());
        moveStatus = move.tryDoMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(moveStatus);
        assertEquals(false, moveStatus.isOk());

        //Try building an illegal track configuration.
        newConfig = TrackConfiguration.getFlatInstance("000011111");

        r = (TrackRule)getWorld().get(KEY.TRACK_RULES, 0);
        newTrackPiece = r.getTrackPiece(newConfig);
        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new Point(0, 0), testPlayer.getPrincipal());
        moveStatus = move.tryDoMove(getWorld(), testPlayer.getPrincipal());
        assertEquals(false, moveStatus.isOk());
    }

    public void testTryUndoMove() {
        setUp();

        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration oldConfig;
        TrackConfiguration newConfig;
        TrackMove move;
        MoveStatus moveStatus;
    }

    public void testDoMove() {
        setUp();

        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration oldConfig;
        TrackConfiguration newConfig;
        TrackMove move;
        MoveStatus moveStatus;

        //Try building the simplest piece of track.
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = (TrackPiece)getWorld().getTile(0, 0);

        TrackRule r = (TrackRule)getWorld().get(KEY.TRACK_RULES, 0);
        newTrackPiece = r.getTrackPiece(newConfig);

        assertMoveDoMoveIsOk(oldTrackPiece, newTrackPiece);
    }

    protected void assertMoveDoMoveIsOk(TrackPiece oldTrackPiece,
        TrackPiece newTrackPiece) {
        TrackMove move;
        MoveStatus moveStatus;

        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new Point(0, 0), testPlayer.getPrincipal());
        moveStatus = move.doMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(moveStatus);
        assertEquals(true, moveStatus.isOk());
        assertEquals(newTrackPiece.getTrackConfiguration(),
            getWorld().getTile(0, 0).getTrackConfiguration());
    }

    public void testUndoMove() {
        //Exactly like testDoMove() v
        setUp();

        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration oldConfig;
        TrackConfiguration newConfig;
        TrackMove move;
        MoveStatus moveStatus;

        //Try building the simplest piece of track.
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = (TrackPiece)getWorld().getTile(0, 0);

        //  assertMoveDoMoveIsOk(oldTrackPiece, newConfig);
    }

    public void testMove() {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration oldConfig;
        TrackConfiguration newConfig;
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = (TrackPiece)getWorld().getTile(0, 0);

        TrackRule r = (TrackRule)getWorld().get(KEY.TRACK_RULES, 0);
        newTrackPiece = r.getTrackPiece(newConfig);

        Move move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new Point(0, 0), testPlayer.getPrincipal());

        assertEqualsSurvivesSerialisation(move);

        assertOkButNotRepeatable(move);
    }
}
