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

    /**
     * Player that this client represents
     */
    protected Player player;

    protected Client(Player p) {
        player = p;
    }

    public ConnectionAdapter getReceiver() {
        return receiver;
    }

    /**
     * @return the player initialised by the client with the clients private
     * key
     */
    public Player getPlayer() {
        return player;
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