/*
 * Created on Apr 14, 2004
 */
package jfreerails.controller;

import jfreerails.world.common.FreerailsSerializable;

/**
 * Defines a command sent from the server to the client.
 * 
 * @author Luke
 * 
 */
public interface Message2Client extends FreerailsSerializable {
    /** Executes this command on the specified ClientControlInterface. */
    MessageStatus execute(ClientControlInterface client);

    /** Returns the id of this command. */
    int getID();
}