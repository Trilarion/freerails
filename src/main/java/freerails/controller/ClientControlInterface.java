/*
 * Created on Apr 14, 2004
 */
package freerails.controller;

import freerails.world.common.FreerailsMutableSerializable;

import java.io.Serializable;

/**
 * Defines the methods that the server can call on a client using a
 * Message2Client.
 *
 * @author Luke
 * @see Message2Client
 */
public interface ClientControlInterface {

    enum ClientProperty {
        CONNECTED_CLIENTS, MAPS_AVAILABLE, SAVED_GAMES
    }

    /**
     * Called when a new game is started or a game is loaded.
     */
    void setGameModel(FreerailsMutableSerializable world);

    /**
     * Sets a property, for example, the list of saved games.
     */
    void setProperty(ClientProperty propertyName, Serializable value);
}