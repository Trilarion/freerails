/*
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

import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.World;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.common.GameTime;
import jfreerails.world.top.ITEM;


public class TimeTickMove implements Move {
    private GameTime oldTime = null;
    private GameTime newTime = null;

    public static TimeTickMove getMove(ReadOnlyWorld w) {
        TimeTickMove timeTickMove = new TimeTickMove();
        timeTickMove.oldTime = (GameTime)w.get(ITEM.TIME);
        timeTickMove.newTime = new GameTime(timeTickMove.oldTime.getTime() + 1);

        return timeTickMove;
    }

    public FreerailsPrincipal getPrincipal() {
	return Player.NOBODY;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        if (((GameTime)w.get(ITEM.TIME)).equals(oldTime)) {
            return MoveStatus.MOVE_OK;
        } else {
            System.err.println("oldTime = " + oldTime.getTime() + " <=> " +
                "currentTime " + ((GameTime)w.get(ITEM.TIME)).getTime());

            return MoveStatus.MOVE_FAILED;
        }
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        if (((GameTime)w.get(ITEM.TIME)).equals(newTime)) {
            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.MOVE_FAILED;
        }
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        if (tryDoMove(w, p).equals(MoveStatus.MOVE_OK)) {
            w.set(ITEM.TIME, newTime);

            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.MOVE_FAILED;
        }
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        if (tryUndoMove(w, p).equals(MoveStatus.MOVE_OK)) {
            w.set(ITEM.TIME, oldTime);

            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.MOVE_FAILED;
        }
    }

    public String toString() {
        return "TimeTickMove: " + oldTime + "=>" + newTime;
    }
}
