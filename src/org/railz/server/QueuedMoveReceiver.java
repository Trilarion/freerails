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

package org.railz.server;

import java.util.LinkedList;

import org.railz.controller.SourcedMoveReceiver;
import org.railz.controller.ConnectionToServer;
import org.railz.move.Move;
import org.railz.world.common.FreerailsSerializable;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;


/**
 * Implements a queue for moves which can be periodically emptied
 */
class QueuedMoveReceiver implements SourcedMoveReceiver {
    private LinkedList moveQueue = new LinkedList();
    private LinkedList principalQueue = new LinkedList();
    private final AuthoritativeMoveExecuter moveExecuter;
    private final IdentityProvider identityProvider;

    public QueuedMoveReceiver(AuthoritativeMoveExecuter ame, IdentityProvider p) {
        moveExecuter = ame;
        identityProvider = p;
    }

    public void processMove(Move move) {
	synchronized (moveQueue) {
	    moveQueue.add(move);
	    principalQueue.add(Player.NOBODY);
	}
    }

    public void processMove(Move move, ConnectionToServer c) {
	synchronized (moveQueue) {
	    moveQueue.add(move);

	    FreerailsPrincipal principal = identityProvider.getPrincipal(c);
	    principalQueue.add(principal);
	}
    }

    public void executeOutstandingMoves() {
	final LinkedList tmpMoves = new LinkedList();
	final LinkedList tmpPrincipals = new LinkedList();
	synchronized (moveQueue) {
	    while (!moveQueue.isEmpty()) {
		tmpMoves.add(moveQueue.removeFirst());
		tmpPrincipals.add(principalQueue.removeFirst());
	    }
	}
	
	while (!tmpMoves.isEmpty()) {
            Move move = (Move) tmpMoves.removeFirst();
            moveExecuter.processMove(move,
		    (FreerailsPrincipal)tmpPrincipals.removeFirst());
        }
    }

    /**
     * TODO this should pop moves off the queue.
     */
    public void undoLastMove() {
    }
}
