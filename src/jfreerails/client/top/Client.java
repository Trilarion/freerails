package jfreerails.client.top;

import jfreerails.controller.MoveChainFork;


/**
 * Represents an instance of a jfreerails client. It provides access to common
 * services which implementations make use of. Objects within the client
 * keep a reference to an instance of this object to access per-client objects.
 * @author rob
 */
public abstract class Client {
    private MoveChainFork moveChainFork;
    private ConnectionAdapter receiver;

    Client() {
    }

    ConnectionAdapter getReceiver() {
        return receiver;
    }

    void setMoveChainFork(MoveChainFork moveChainFork) {
        this.moveChainFork = moveChainFork;
    }

    /**
     * @return  A MoveChainFork to which classes may subscribe to receive Moves
     */
    MoveChainFork getMoveChainFork() {
        return moveChainFork;
    }

    void setReceiver(ConnectionAdapter receiver) {
        this.receiver = receiver;
    }
}