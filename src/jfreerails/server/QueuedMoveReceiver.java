package jfreerails.server;

import jfreerails.controller.MoveReceiver;
import jfreerails.controller.SychronizedQueue;
import jfreerails.move.Move;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;


/**
 * Implements a queue for moves which can be periodically emptied.
 * @author rob?
 */
class QueuedMoveReceiver implements MoveReceiver {
    private final SychronizedQueue moveQueue = new SychronizedQueue();
    private final SychronizedQueue principalQueue = new SychronizedQueue();
    private final AuthoritativeMoveExecuter moveExecuter;

    public QueuedMoveReceiver(AuthoritativeMoveExecuter ame, IdentityProvider p) {
        moveExecuter = ame;
    }

    public void processMove(Move move) {
        moveQueue.write(move);
        principalQueue.write(Player.NOBODY);
    }

    public void executeOutstandingMoves() {
        FreerailsSerializable[] items = moveQueue.read();
        FreerailsSerializable[] principals = principalQueue.read();

        for (int i = 0; i < items.length; i++) {
            Move move = (Move)items[i];
            moveExecuter.processMove(move, (FreerailsPrincipal)principals[i]);
        }
    }
}