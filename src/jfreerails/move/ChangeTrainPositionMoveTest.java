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

package jfreerails.move;

import junit.framework.TestCase;

import jfreerails.world.common.*;
import jfreerails.world.player.Player;
import jfreerails.world.top.KEY;
import jfreerails.world.top.*;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPositionOnMap;

/**
 * @author Luke Lindsay 03-Nov-2002
 *
 */
public class ChangeTrainPositionMoveTest extends TestCase {
    public World w;
    private static Player testPlayer = new Player ("test player", (new Player ("test"
		    + " player")).getPublicKey(), 0);

    /**
     * Constructor for ChangeTrainPositionMoveTest.
     * @param arg0
     */
    public ChangeTrainPositionMoveTest(String arg0) {
        super(arg0);
    }

    public void testChangeTrainPositionMove() {
    }

    public void testGenerate() {
    }

    public void testDoMove() {
        setUp();

        TrainModel t = (TrainModel)this.w.get(KEY.TRAINS, 0,
	       	testPlayer.getPrincipal());
        TrainPositionOnMap oldPosition = t.getPosition();
        assertEquals(FIXTURE1_BEFORE_MOVE1, oldPosition);

        MoveStatus status = MOVE1.doMove(this.w, testPlayer.getPrincipal());
        assertTrue(status.ok);

        t = (TrainModel)this.w.get(KEY.TRAINS, 0, testPlayer.getPrincipal());

        TrainPositionOnMap newPosition = t.getPosition();
        assertEquals(FIXTURE1_AFTER_MOVE1, newPosition);
    }

    public void testUndoMove() {
    }

    public void testTryDoMove() {
        setUp();

        MoveStatus status = MOVE1.tryDoMove(this.w, testPlayer.getPrincipal());
        assertTrue(status.ok);
    }

    public void testTryUndoMove() {
    }

    protected void setUp() {
        w = new WorldImpl(1, 1);

	w.add(KEY.PLAYERS, testPlayer, Player.AUTHORITATIVE);

	GameTime now = (GameTime) w.get(ITEM.TIME, testPlayer.getPrincipal());
        TrainModel train1 = new TrainModel(0, new int[] {},
                FIXTURE1_BEFORE_MOVE1, 0, 0, now);
        w.add(KEY.TRAINS, train1, testPlayer.getPrincipal());
    }

    public static final ChangeTrainPositionMove MOVE1 = 
	new ChangeTrainPositionMove
	(TrainPositionOnMap.createInstance(new int[] {0, 10},
					   new int[] {1, 11}),
            TrainPositionOnMap.createInstance(new int[] {37, 40},
                new int[] {38, 44}),
	    0, testPlayer.getPrincipal(), true, false);

    public static final TrainPositionOnMap FIXTURE1_BEFORE_MOVE1 =
       	TrainPositionOnMap.createInstance
	(new int[] {10, 30, 40}, new int[] {11, 33, 44});

    public static final TrainPositionOnMap FIXTURE1_AFTER_MOVE1 =
       	TrainPositionOnMap.createInstance
	(new int[] {0, 30, 37}, new int[] {1, 33, 38});
}
