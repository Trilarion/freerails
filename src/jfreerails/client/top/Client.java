package jfreerails.client.top;

import jfreerails.controller.MoveChainFork;
import jfreerails.world.player.Player;


/**
 * Represents an instance of a jfreerails client. It provides access to common
 * services which implementations make use of. Objects within the client
 * keep a reference to an instance of this object to access per-client objects.
 */
public abstract class Client {
    private MoveChainFork moveChainFork;
    private ConnectionAdapter receiver;

    protected Client(Player p) {
    }

    public ConnectionAdapter getReceiver() {
        return receiver;
    }

    protected void setMoveChainFork(MoveChainFork moveChainFork) {
        this.moveChainFork = moveChainFork;
    }

    /**
     * @return  A MoveChainFork to which classes may subscribe to receive Moves
     */
    protected MoveChainFork getMoveChainFork() {
        return moveChainFork;
    }

    protected void setReceiver(ConnectionAdapter receiver) {
        this.receiver = receiver;
    }
}