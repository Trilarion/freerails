/*
 * Created on Apr 14, 2004
 */
package freerails.controller;

import java.io.IOException;

/**
 * Defines the methods that a client can call on the server using a
 * Message2Server.
 *
 * @author Luke
 * @see Message2Server
 */
public interface ServerControlInterface {

    /**
     *
     */
    String FREERAILS_SAV = "freerails.sav";

    /**
     *
     */
    String VERSION = "CVS";

    /**
     *
     * @param saveGameName
     * @throws IOException
     */
    void loadgame(String saveGameName) throws IOException;

    /**
     *
     * @param saveGameName
     */
    void savegame(String saveGameName);

    /**
     *
     */
    void stopGame();

    /**
     *
     */
    void refreshSavedGames();

    /**
     *
     * @param mapName
     */
    void newGame(String mapName);
}