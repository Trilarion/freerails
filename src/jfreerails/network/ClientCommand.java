/*
 * Created on Apr 14, 2004
 */
package jfreerails.network;

import jfreerails.world.common.FreerailsSerializable;


/**
 *  Defines a command sent from the server to the client.
 *  @author Luke
 *
 */
public interface ClientCommand extends FreerailsSerializable {
    /** Executes this command on the specified ClientControlInterface.*/
    CommandStatus execute(ClientControlInterface client);

    /** Returns the id of this command.*/
    int getID();
}