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

import jfreerails.world.top.World;
import jfreerails.world.player.FreerailsPrincipal;

/**
 * Specifies a move that has been rejected (ie not executed) by the
 * MoveExecuter. This move has already been attempted and thus all attempts to
 * try/perform the move will fail.
 */
public class RejectedMove implements Move {
    private Move attemptedMove;
    private MoveStatus moveStatus;

    /**
     * @param attemptedMove the move that failed to complete successfully
     * @param result the result of attempting to process the move
     */
    public RejectedMove(Move attemptedMove, MoveStatus result) {
        this.attemptedMove = attemptedMove;
        moveStatus = result;
    }

    public FreerailsPrincipal getPrincipal() {
	return attemptedMove.getPrincipal();
    }

    /**
     * @return the result that was obtained when the server attempted the move
     */
    public MoveStatus getMoveStatus() {
        return moveStatus;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        return MoveStatus.MOVE_FAILED;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        return MoveStatus.MOVE_FAILED;
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        return MoveStatus.MOVE_FAILED;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        return MoveStatus.MOVE_FAILED;
    }

    /**
     * @return the move that was attempted by the server
     */
    public Move getAttemptedMove() {
        return attemptedMove;
    }
}
