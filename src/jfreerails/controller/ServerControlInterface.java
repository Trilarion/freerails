package jfreerails.controller;

/**
 * This class exposes controls for the server rather than changes to the game
 * being played.
 *
 * XXX need to figure out which clients should have access to this interface -
 * should all clients be able to control the server, or only one? Permissions
 * for changing server settings?
 */
public interface ServerControlInterface {
    /**
     * Creates a new game based on the specified map.
     * Procedure for changing the map:
     * <ol>
     * <li>Client calls newGame/saveGame over the ServerControlInterface.
     * <li>Server sends WorldChanged signal.
     * <li>Client closes connection (to prevent any further moves from being received)
     * <li>Client opens new connection to server.
     * <li>Server connection discards all moves.
     * <li>Client requests new world via loadWorldFromServer() on connection.
     * <li>Client receives new world with timestamp. Server starts sending
     * moves. (Moves may have to be queued until client has finished receiving
     * map).
     * <li>Client proceeds with new world as normal.
     * </ol>
     */
    public void newGame(String mapName);

    public void loadGame();

    public void saveGame();

    /**
     * Retrieve a list of map names for use with newGame()
     */
    public String[] getMapNames();

    public void setTargetTicksPerSecond(int ticksPerSecond);
}
