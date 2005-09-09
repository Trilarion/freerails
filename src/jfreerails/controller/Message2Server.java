/*
 * Created on Apr 14, 2004
 */
package jfreerails.controller;

import jfreerails.world.common.FreerailsSerializable;

/**
 * Defines a command sent from a client to the server.
 * 
 * @author Luke
 * 
 */
public interface Message2Server extends FreerailsSerializable {
	int getID();

	MessageStatus execute(ServerControlInterface server);
}