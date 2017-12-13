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

import jfreerails.util.SychronizedQueue;
import jfreerails.controller.SourcedMoveReceiver;
import jfreerails.controller.ConnectionToServer;
import jfreerails.move.Move;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;


/**
 * Implements a queue for moves which can be periodically emptied
 */
class QueuedMoveReceiver implements SourcedMoveReceiver {
    private SychronizedQueue moveQueue = new SychronizedQueue();
    private SychronizedQueue principalQueue = new SychronizedQueue();
    private final AuthoritativeMoveExecuter moveExecuter;
    private final IdentityProvider identityProvider;

    public QueuedMoveReceiver(AuthoritativeMoveExecuter ame, IdentityProvider p) {
        moveExecuter = ame;
        identityProvider = p;
    }

    public void processMove(Move move) {
        moveQueue.write(move);
        principalQueue.write(Player.NOBODY);
    }

    public void processMove(Move move, ConnectionToServer c) {
        moveQueue.write(move);

        FreerailsPrincipal principal = identityProvider.getPrincipal(c);
        principalQueue.write(principal);
    }

    public void executeOutstandingMoves() {
        FreerailsSerializable[] items = moveQueue.read();
        FreerailsSerializable[] principals = principalQueue.read();

        for (int i = 0; i < items.length; i++) {
            Move move = (Move)items[i];
            moveExecuter.processMove(move, (FreerailsPrincipal)principals[i]);
        }
    }

    /**
     * TODO this should pop moves off the queue.
     */
    public void undoLastMove() {
    }
}