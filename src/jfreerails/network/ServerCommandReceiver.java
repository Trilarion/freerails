/*
 * Created on Sep 12, 2004
 *
 */
package jfreerails.network;


/**
 * @author Luke
 *
 */
public interface ServerCommandReceiver {
    void sendCommand(ServerCommand c);
}