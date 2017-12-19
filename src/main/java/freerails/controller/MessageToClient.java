package freerails.controller;

import freerails.world.FreerailsSerializable;

/**
 * Defines a command sent from the server to the client.
 *
 */
public interface MessageToClient extends FreerailsSerializable {
    /**
     * Executes this command on the specified ClientControlInterface.
     * @param client
     * @return 
     */
    MessageStatus execute(ClientControlInterface client);

    /**
     * Returns the id of this command.
     * @return 
     */
    int getID();
}