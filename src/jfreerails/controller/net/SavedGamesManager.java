/*
 * Created on Jun 26, 2004
 */
package jfreerails.controller.net;

import java.io.IOException;
import java.io.Serializable;


/**
 *  Defines methods that let the server load and save game states, and
 * get blank maps for new games.
 *
 *  @author Luke
 *
 */
public interface SavedGamesManager {
    String[] getSaveGameNames();

    String[] getNewMapNames();

    void saveGame(Serializable w, String s) throws IOException;

    Serializable loadGame(String name) throws IOException;

    Serializable newMap(String name) throws IOException;
}