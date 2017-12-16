/*
 * Created on Sep 12, 2004
 *
 */
package freerails.network;

import freerails.controller.Message2Server;

/**
 * Defines a method that accepts a command to be sent to the server.
 * 
 * @author Luke
 * 
 */
public interface ServerCommandReceiver {
    void sendCommand(Message2Server c);
}