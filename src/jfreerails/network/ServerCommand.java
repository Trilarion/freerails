/*
 * Created on Apr 14, 2004
 */
package jfreerails.network;

import jfreerails.world.common.FreerailsSerializable;


/**
 * Defines a command sent from a client to the server.
 *  @author Luke
 *
 */
public interface ServerCommand extends FreerailsSerializable {
    int getID();

    CommandStatus execute(ServerControlInterface server);
}