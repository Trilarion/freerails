/*
 * Created on Apr 14, 2004
 */
package jfreerails.controller.net;

import java.io.Serializable;
import jfreerails.world.common.FreerailsSerializable;


/**
 *
 *  @author Luke
 *
 */
public interface ClientControlInterface {
    public static final String CONNECTED_CLIENTS = "CONNECTED_CLIENTS";
    public static final String MAPS_AVAILABLE = "MAPS_AVAILABLE";
    public static final String SAVED_GAMES = "SAVED_GAMES";

    void setGameModel(FreerailsSerializable world);

    void setProperty(String propertyName, Serializable value);
}