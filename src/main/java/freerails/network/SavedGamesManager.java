/*
 * Created on Jun 26, 2004
 */
package freerails.network;

import java.io.IOException;
import java.io.Serializable;

/**
 * Defines methods that let the server load and save game states, and get blank
 * maps for new games.
 *
 * @author Luke
 */
public interface SavedGamesManager {

    /**
     *
     * @return
     */
    String[] getSaveGameNames();

    /**
     *
     * @return
     */
    String[] getNewMapNames();

    /**
     *
     * @param w
     * @param s
     * @throws IOException
     */
    void saveGame(Serializable w, String s) throws IOException;

    /**
     *
     * @param name
     * @return
     * @throws IOException
     */
    Serializable loadGame(String name) throws IOException;

    /**
     *
     * @param name
     * @return
     * @throws IOException
     */
    Serializable newMap(String name) throws IOException;
}