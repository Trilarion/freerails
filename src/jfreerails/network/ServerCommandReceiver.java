/*
 * Created on Sep 12, 2004
 *
 */
package jfreerails.network;


/**
 * Defines a method that accepts a command to be sent to the server.
 * 
 * @author Luke
 *
 */
public interface ServerCommandReceiver {
    void sendCommand(ServerCommand c);
}