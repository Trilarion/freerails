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
    public static final String FREERAILS_SAV = "freerails.sav";

    public static final String VERSION = "CVS";

    void loadgame(String saveGameName) throws IOException;

    void savegame(String saveGameName);

    void stopGame();

    void refreshSavedGames();

    void newGame(String mapName);
}