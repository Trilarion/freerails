package jfreerails.server;

import java.util.Vector;
import jfreerails.controller.ServerControlInterface;
import jfreerails.util.FreerailsProgressMonitor;

/**
 * This implements a game server. A game server may host a number of independent
 * games all being played simultaneously.
 *
 * @author lindsal
 * @author rtuck99@users.sourceforge.net
 */
public class GameServer {
    /**
     * The set of games which this server is serving. Vector of
     * ServerGameController.
     */
    private Vector gameControllers = new Vector();

    /**
     * starts the server and creates a new ServerGameEngine running initialised
     * from a new map, accepting connections on the default port.
     */
    public ServerControlInterface getNewGame(String mapName,
        FreerailsProgressMonitor pm, int port) {
        ServerGameEngine gameEngine = new ServerGameEngine(mapName, pm);
        ServerGameController sgc = new ServerGameController(gameEngine, port);
        gameControllers.add(sgc);

        return sgc;
    }

    /**
     * Load a saved game
     * @param port port number on which to accept incoming connections, or 0 for
     * no network connections.
     */
    public ServerControlInterface getSavedGame(FreerailsProgressMonitor pm,
        int port) {
        ServerGameEngine gameEngine = ServerGameEngine.loadGame();
        ServerGameController sgc = new ServerGameController(gameEngine, port);
        gameControllers.add(sgc);

        return sgc;
    }

    /**
     * @return a list of possible map names that could be used to start a game
     */
    public static String[] getMapNames() {
        return OldWorldImpl.getMapNames();
    }
}
