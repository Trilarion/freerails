/*
 * Created on Apr 14, 2004
 */
package jfreerails.controller.net;


/**
 *  Defines the methods that a client can call on the server using a ServerCommand.
 * @see ServerCommand
 *  @author Luke
 *
 */
public interface ServerControlInterface {
    void loadgame(String saveGameName);

    void savegame();

    void stopGame();

    void newGame(String mapName);
}