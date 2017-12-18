/*
 * Created on Apr 14, 2004
 */
package freerails.controller;

import freerails.world.common.FreerailsSerializable;

/**
 * Defines a command sent from a client to the server.
 *
 * @author Luke
 */
public interface Message2Server extends FreerailsSerializable {

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