package freerails.controller;

import freerails.world.FreerailsSerializable;

/**
 * Defines a command sent from a client to the server.
 *
 */
public interface MessageToServer extends FreerailsSerializable {

    /**
     *
     * @return
     */
    int getID();

    /**
     *
     * @param server
     * @return
     */
    MessageStatus execute(ServerControlInterface server);
}