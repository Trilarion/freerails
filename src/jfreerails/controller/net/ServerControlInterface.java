/*
 * Created on Apr 14, 2004
 */
package jfreerails.controller.net;


/**
 *
 *  @author Luke
 *
 */
public interface ServerControlInterface {
    void loadgame(String saveGameName);

    void savegame();

    void stopGame();

    void newGame(String mapName);
}