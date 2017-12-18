/*
 * Created on Apr 13, 2004
 */
package freerails.network;

import freerails.util.GameModel;

/**
 * Defines a server that can accept connections to clients.
 *
 * @author Luke
 */
public interface GameServer extends GameModel {

    /**
     *
     * @param connection
     */
    void addConnection(Connection2Client connection);

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