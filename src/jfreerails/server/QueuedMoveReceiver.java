package jfreerails.server;

import jfreerails.controller.ConnectionToServer;
import jfreerails.controller.SourcedMoveReceiver;
import jfreerails.controller.SychronizedQueue;
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