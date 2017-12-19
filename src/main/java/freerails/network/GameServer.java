package freerails.network;

import freerails.util.GameModel;

/**
 * Defines a server that can accept connections to clients.
 *
 */
public interface GameServer extends GameModel {

    /**
     *
     * @param connection
     */
    void addConnection(ConnectionToClient connection);

    /**
     *
     * @return
     */
    int countOpenConnections();

    /**
     *
     */
    void stop();
}