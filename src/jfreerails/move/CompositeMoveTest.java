/*
 * Copyright (C) 2003 Luke Lindsay
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
 * Created on 31-May-2003
 *
 */
package jfreerails.move;

import jfreerails.world.common.GameTime;
import jfreerails.world.player.Player;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.*;


/**
 * @author Luke
 *
 */
public class CompositeMoveTest extends AbstractMoveTestCase {
    StationModel station1;
    StationModel station2;
    StationModel station3;
    StationModel station4;

    public void setUp() {
	super.setUp();
	GameTime now = (GameTime) getWorld().get(ITEM.TIME,
		Player.AUTHORITATIVE);
	station1 = new StationModel(1, 1, "station1", 10, 0, now);
	station2 = new StationModel(2, 3, "station2", 10, 0, now);
	station3 = new StationModel(3, 3, "station3", 10, 0, now);
	station4 = new StationModel(4, 4, "station4", 10, 0, now);
    }

    public void testMove() {
        Move[] moves = new Move[4];
        moves[0] = new AddItemToListMove(KEY.STATIONS, 0, station1,
		testPlayer.getPrincipal());
        moves[1] = new AddItemToListMove(KEY.STATIONS, 1, station2,
		testPlayer.getPrincipal());
        moves[2] = new AddItemToListMove(KEY.STATIONS, 2, station3,
		testPlayer.getPrincipal());
        moves[3] = new AddItemToListMove(KEY.STATIONS, 3, station4,
		testPlayer.getPrincipal());

        Move compositeMove = new CompositeMove(moves);
        assertEqualsSurvivesSerialisation(compositeMove);
        assertTryMoveIsOk(compositeMove);
        assertEquals("The stations should not have been add yet.", 0,
            getWorld().size(KEY.STATIONS, testPlayer.getPrincipal()));
        assertDoMoveIsOk(compositeMove);
        assertEquals("The stations should have been add now.", 4,
            getWorld().size(KEY.STATIONS, testPlayer.getPrincipal()));
        assertTryUndoMoveIsOk(compositeMove);
        assertUndoMoveIsOk(compositeMove);

        assertOkButNotRepeatable(compositeMove);
    }
}
