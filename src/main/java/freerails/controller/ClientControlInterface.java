package freerails.controller;

import freerails.world.common.FreerailsMutableSerializable;

import java.io.Serializable;

/**
 * Defines the methods that the server can call on a client using a
 * MessageToClient.
 *
 * @see MessageToClient
 */
public interface ClientControlInterface {

    /**
     * Called when a new game is started or a game is loaded.
     * @param world
     */
    void setGameModel(FreerailsMutableSerializable world);

    /**
     * Sets a property, for example, the list of saved games.
     * @param propertyName
     * @param value
     */
    void setProperty(ClientProperty propertyName, Serializable value);

    /**
     *
     */
    enum ClientProperty {

        /**
         *
         */
        CONNECTED_CLIENTS,

        /**
         *
         */
        MAPS_AVAILABLE,

        /**
         *
         */
        SAVED_GAMES
    }
}