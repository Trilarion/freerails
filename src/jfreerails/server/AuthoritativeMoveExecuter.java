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

package jfreerails.server;

import java.util.LinkedList;
import jfreerails.controller.MoveReceiver;
import jfreerails.controller.UncommittedMoveReceiver;
import jfreerails.move.RejectedMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.top.World;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;


/**
 * A move executer which has the authority to reject moves
 * outright.
 *
 * During processing of moves, we must obtain a lock on the game world in order
 * to prevent the world changing when other threads are accessing the world (eg
 * when the world is being sent to a client by the network thread).
 */
class AuthoritativeMoveExecuter implements UncommittedMoveReceiver {
    private static final int MAX_UNDOS = 10;
    protected final World world;
    protected final MoveReceiver moveReceiver;
    private final LinkedList moveStack = new LinkedList();

    public AuthoritativeMoveExecuter(World w, MoveReceiver mr) {
        world = w;
        moveReceiver = mr;
    }

    /**
     * forwards move as a RejectedMove if it failed.
     */
    private void forwardMove(Move move, MoveStatus status) {
        if (moveReceiver == null) {
            return;
        }

        if (status != MoveStatus.MOVE_OK) {
	    System.err.println("forwarding rejected move " + move);
            moveReceiver.processMove(new RejectedMove(move, status));
        } else {
            moveReceiver.processMove(move);
        }
    }

    void processMove(Move move, FreerailsPrincipal p) {
        /*
	 * if the server is submitting the move, then act on behalf of whoever
         * move was submitted for
	 */
        if (p.equals(Player.AUTHORITATIVE))
            p = move.getPrincipal();
	
        moveStack.add(move);

        if (moveStack.size() > MAX_UNDOS) {
            moveStack.removeFirst();
        }

        MoveStatus ms;

	synchronized (world) {
	    ms = move.doMove(world, p);
	}

        forwardMove(move, ms);
    }

    /**
     * @see MoveReceiver#processMove(Move)
     */
    public void processMove(Move move) {
        processMove(move, move.getPrincipal());
    }

    /**
     * FIXME clients can undo each others moves.
     * FIXME information about the principal is lost.
     */
    public void undoLastMove() {
        if (moveStack.size() > 0) {
            Move m = (Move)moveStack.removeLast();
            MoveStatus ms;

	    synchronized (world) {
		ms = m.undoMove(world, Player.NOBODY);
	    }

            if (ms != MoveStatus.MOVE_OK) {
                System.err.println("Couldn't undo move!");

                /* push it back on the stack to prevent further
                 * out-of-order undos */
                moveStack.add(m);
            }

            forwardMove(m, ms);
        } else {
            System.err.println("No moves on stack.");
        }
    }
}
