package jfreerails.client.top;

import jfreerails.controller.MoveChainFork;
import jfreerails.controller.UntriedMoveReceiver;
import jfreerails.world.top.ReadOnlyWorld;


/**
 * Represents an instance of a jfreerails client. It provides access to common
 * services which implementations make use of. Objects within the client
 * keep a reference to an instance of this object to access per-client objects.
 *
 * TODO currently the world held by the client is not updated by incoming moves
 * over the connection, so it only works with a local connection when we have a
 * reference to the servers copy.
 *
 */
public abstract class Client {
    protected MoveChainFork moveChainFork;
    protected ConnectionAdapter receiver;

    /**
     * @return A receiver with which moves may be tried out and submitted
     */
    public UntriedMoveReceiver getReceiver() {
        return receiver;
    }

    /**
     * @return  A MoveChainFork to which classes may subscrive to receive Moves
     */
    public MoveChainFork getMoveChainFork() {
        return moveChainFork;
    }

    /**
     * @return A read-only copy of the world
     */
    public ReadOnlyWorld getWorld() {
        return receiver.world;
    }

    /**
     * Returns a mutex to the clients local DB
     * @deprecated
     */
    public Object getMutex() {
        return receiver.mutex;
    }
}