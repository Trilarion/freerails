/*
 * Created on Apr 14, 2004
 */
package jfreerails.network;


/**
 *  Defines the methods that a client can call on the server using a ServerCommand.
 * @see ServerCommand
 *  @author Luke
 *
 */
public interface ServerControlInterface {
    public static final String FREERAILS_SAV = "freerails.sav";
    public static final String VERSION = "CVS";

    void loadgame(String saveGameName);

    void savegame(String saveGameName);

    void stopGame();

    void newGame(String mapName);
}