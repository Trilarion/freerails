/*
 * Created on Apr 13, 2004
 */
package jfreerails.controller.net;

import jfreerails.util.GameModel;


/**
 *
 *  @author Luke
 *
 */
public interface NewGameServer extends GameModel {
    void addConnection(Connection2Client connection);

    int countOpenConnections();

    void stop();
}