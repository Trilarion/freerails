package jfreerails.controller;

import javax.swing.table.TableModel;


/**
 * This class exposes controls for a single game running on the server rather
 * than changes to the game being played.
 *
 * XXX need to figure out which clients should have access to this interface -
 * should all clients be able to control the server, or only one? Permissions
 * for changing server settings?
 *
 * @author rtuck99
 */
public interface ServerControlInterface {
    /**
     * Creates a new game based on the specified map, replacing the currently
     * ongoing game.
     * Procedure for changing the map:
     * <ol>
     * <li>Client calls newGame/saveGame over the ServerControlInterface.
     * <li>Server sends WorldChanged signal.
     * <li>Client closes connection (to prevent any further moves from being received)
     * <li>Client opens new connection to server.
     * <li>Server connection discards all moves.
     * <li>Client requests new world via loadWorldFromServer() on connection.
     * <li>Client receives new world with timestamp.
     * <li>The game starts running at the speed of the previous game
     * </ol>
     */
    public void newGame(String mapName);

    public void loadGame();

    public void saveGame();

    public void quitGame();

    /**
     * Retrieve a list of map names for use with newGame()
     */
    public String[] getMapNames();

    public void setTargetTicksPerSecond(int ticksPerSecond);

    public int getTargetTicksPerSecond();

    /**
     * @return a TableModel which provides information about the current
     * connections to this game
     */
    public TableModel getClientConnectionTableModel();

    public LocalConnection getLocalConnection();
}