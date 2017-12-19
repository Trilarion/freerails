/*
 * Created on Sep 12, 2004
 *
 */
package freerails.network;

import freerails.controller.MessageToServer;

/**
 * Defines a method that accepts a command to be sent to the server.
 *
 */
public interface ServerCommandReceiver {

    /**
     *
     * @param c
     */
    void sendCommand(MessageToServer c);
}