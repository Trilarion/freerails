package jfreerails.server;

import java.io.IOException;
import java.net.SocketException;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.controller.ConnectionListener;
import jfreerails.controller.ConnectionToServer;
import jfreerails.controller.InetConnection;
import jfreerails.controller.LocalConnection;
import jfreerails.controller.MoveChainFork;
import jfreerails.controller.MoveReceiver;
import jfreerails.controller.ServerControlInterface;
import jfreerails.controller.ServerCommand;
import jfreerails.controller.AddPlayerCommand;
import jfreerails.controller.AddPlayerResponseCommand;
import jfreerails.controller.WorldChangedCommand;
import jfreerails.util.FreerailsProgressMonitor;


/**
 * This implements a game server. A game server may host a number of independent
 * games all being played simultaneously.
 *
 * @author lindsal
 * @author rtuck99@users.sourceforge.net
 */
public class GameServer {
    private FreerailsProgressMonitor pm;

    /**
     * The set of games which this server is serving. Vector of
     * ServerGameController.
     */
    private Vector gameControllers = new Vector();

    /**
     * associates an instance of ServerGameEngine with a set of game controls.
     * Manages connectivity to the ServerGameEngine.
     */
    private class ServerGameController implements ServerControlInterface,
        ConnectionListener {
        /**
         * The connections that this server has
         */
        private Vector connections = new Vector();
        private MoveChainFork moveChainFork;
        private InetConnection serverSocket;
        private ServerGameEngine gameEngine;
        private ClientConnectionTableModel tableModel = new ClientConnectionTableModel(connections,
                this);

        /**
         * Port number the game is available on
         */
        private int port;

        public ServerGameController(ServerGameEngine engine, int port) {
            moveChainFork = engine.getMoveChainFork();
            gameEngine = engine;
            this.port = port;

            if (port != 0) {
                /* Open our server socket */
                try {
                    serverSocket = new InetConnection(engine.getWorld(),
                            InetConnection.SERVER_PORT);
                } catch (IOException e) {
                    System.err.println("Couldn't open the server socket!!!" +
                        e);
                    throw new RuntimeException(e);
                }

                Thread thread = new Thread(new InetGameServer(serverSocket, this));
                thread.start();
            }
        }

        /**
         * return a brand new local connection.
         */
        public LocalConnection getLocalConnection() {
            synchronized (connections) {
                LocalConnection connection = new LocalConnection(gameEngine.getWorld());
                addConnection(connection);

                return connection;
            }
        }

        public void connectionClosed(ConnectionToServer c) {
            synchronized (connections) {
                /*
                 * If the player is connected locally, the connection is still
                 * active, so that the server may still be controlled via the
                 * connection, but the player must re-authenticate themselves
                 * in order to play
                 */
                if (!(c instanceof LocalConnection)) {
                    removeConnection(c);
                } else {
                    gameEngine.getIdentityProvider().removeConnection(c);
                    tableModel.stateChanged(c);
                }
            }
        }

        private void removeConnection(ConnectionToServer c) {
            synchronized (connections) {
                gameEngine.getIdentityProvider().removeConnection(c);
                tableModel.removeRow(c);
                moveChainFork.remove(c);
                connections.remove(c);
                c.removeMoveReceiver(gameEngine.getMoveExecuter());
                c.removeConnectionListener(this);
            }
        }

        private void addConnection(ConnectionToServer c) {
            synchronized (connections) {
                c.addConnectionListener(this);
                c.addMoveReceiver(gameEngine.getMoveExecuter());
                connections.add(c);
                moveChainFork.add(c);

                if (c instanceof InetConnection) {
                    tableModel.addRow(c,
                        ((InetConnection)c).getRemoteAddress().toString());
                } else if (c instanceof LocalConnection) {
                    ((LocalConnection)c).setWorld(gameEngine.getWorld());
                    tableModel.addRow(c, "Local connection");
                }
            }
        }

        /**
         * Create a new ServerGameEngine instance and transfer all clients of
         * this game to the new one.
         */
        public void loadGame() {
            int ticksPerSec = gameEngine.getTargetTicksPerSecond();

            /* open a new controller */
            ServerGameEngine newGame = ServerGameEngine.loadGame();

            transferClients(newGame);
            setTargetTicksPerSecond(ticksPerSec);
        }

        public void saveGame() {
            gameEngine.saveGame();
        }

        public String[] getMapNames() {
            return GameServer.getMapNames();
        }

        public void setTargetTicksPerSecond(int ticksPerSecond) {
            gameEngine.setTargetTicksPerSecond(ticksPerSecond);
        }

        /**
         * stop the current game and transfer the current local connections to a
         * new game running the specified map.
         */
        public void newGame(String mapName) {
            int ticksPerSec = gameEngine.getTargetTicksPerSecond();
            ServerGameEngine newGame = new ServerGameEngine(mapName,
                    FreerailsProgressMonitor.NULL_INSTANCE);
            transferClients(newGame);

            setTargetTicksPerSecond(ticksPerSec);
        }

        /**
         * transfer all clients of this game to the new game
         */
        private void transferClients(ServerGameEngine newGame) {
            Vector localConnections = new Vector();
            MoveReceiver oldExecuter = gameEngine.getMoveExecuter();

            synchronized (connections) {
                for (int i = 0; i < connections.size(); i++) {
                    ConnectionToServer c = (ConnectionToServer)connections.get(i);

                    /* Local connections must be transferred manually - remote
                     * connections are sent a WorldChangedCommand later */
                    if (c instanceof LocalConnection) {
                        localConnections.add(c);
                        removeConnection(c);
                        i--;
                    }
                }

                gameEngine.stop();
                gameEngine = newGame;
                moveChainFork = newGame.getMoveChainFork();

                if (serverSocket != null) {
                    serverSocket.setWorld(gameEngine.getWorld());
                }

                while (!localConnections.isEmpty()) {
                    LocalConnection lc = (LocalConnection)localConnections.remove(0);
                    addConnection(lc);
                    lc.send(new WorldChangedCommand());
                }

                /* send all remaining clients notification that this game is
                 * about to end */
                for (int i = 0; i < connections.size(); i++) {
                    ConnectionToServer c = (ConnectionToServer)connections.get(i);

                    /*
                     * don't send locally connected clients the
                     * WorldChangedCommand as they have already been sent one
                     */
                    if (!(c instanceof LocalConnection)) {
                        c.send(new WorldChangedCommand());
                        c.flush();
                    }
                }
            }
        }

        public TableModel getClientConnectionTableModel() {
            return tableModel;
        }

        public void connectionStateChanged(ConnectionToServer c) {
            tableModel.stateChanged(c);
        }

        public void quitGame() {
            synchronized (connections) {
                while (!connections.isEmpty()) {
                    ConnectionToServer c = (ConnectionToServer)connections.get(0);

                    c.close();
                    removeConnection(c);
                }

                gameEngine.stop();
            }
        }

        public void processServerCommand(ConnectionToServer c, ServerCommand s) {
            if (s instanceof AddPlayerCommand) {
                AddPlayerCommand apc = (AddPlayerCommand)s;

                synchronized (connections) {
                    System.out.println(
                        "Received request to authenticate player" + " " +
                        apc.getPlayer());

                    if (!gameEngine.getIdentityProvider().addConnection(c,
                                apc.getPlayer(), apc.getSignature())) {
                        c.send(new AddPlayerResponseCommand(apc, ""));
                    } else {
                        c.send(new AddPlayerResponseCommand(
                                gameEngine.getIdentityProvider().getPrincipal(c)));
                    }

                    tableModel.stateChanged(c);
                }
            }
        }
    }

    public LocalConnection getLocalConnection(ServerControlInterface i) {
        return ((ServerGameController)i).getLocalConnection();
    }

    /**
     * Sits in a loop and accepts incoming connections over the network
     */
    private class InetGameServer implements Runnable {
        private InetConnection serverSocket;
        private ServerGameController sgc;

        public InetGameServer(InetConnection serverSocket,
            ServerGameController sgc) {
            this.serverSocket = serverSocket;
            this.sgc = sgc;
        }

        public void run() {
            Thread.currentThread().setName("GameServer.InetGameServer");

            try {
                while (true) {
                    try {
                        ConnectionToServer c = serverSocket.accept();
                        sgc.addConnection(c);
                    } catch (IOException e) {
                        if (e instanceof SocketException) {
                            throw (SocketException)e;
                        } else {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (SocketException e) {
                /* The socket was probably closed, exit */
            }
        }
    }

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
     * Table model which represents currently connected clients.
     * Connection states are described as follows:
     * <ol>
     */
    private class ClientConnectionTableModel extends DefaultTableModel {
        /**
         * reference to ServerGameController's connections
         */
        private Vector connections;
        private ServerGameController gameController;

        public ClientConnectionTableModel(Vector connections,
            ServerGameController sgc) {
            super(new String[] {"Client address", "State", "Player"}, 0);
            this.connections = connections;
            gameController = sgc;
        }

        private String getPlayerName(ConnectionToServer c) {
            IdentityProvider ip = gameController.gameEngine.getIdentityProvider();
            FreerailsPrincipal p = ip.getPrincipal(c);

            if (p != null) {
                Player pl;

                if ((pl = ip.getPlayer(p)) == null) {
                    return p.getName();
                } else {
                    return pl.getName();
                }
            } else {
                return "Player not authenticated.";
            }
        }

        public void addRow(ConnectionToServer c, String address) {
            addRow(new String[] {
                    address, c.getConnectionState().toString(), getPlayerName(c)
                });
        }

        public void stateChanged(ConnectionToServer c) {
            int i;

            synchronized (connections) {
                i = connections.indexOf(c);
                setValueAt(c.getConnectionState().toString(), i, 1);
                setValueAt(getPlayerName(c), i, 2);
            }
        }

        public void removeRow(ConnectionToServer c) {
            synchronized (connections) {
                removeRow(connections.indexOf(c));
            }
        }

        public boolean isCellEditable(int r, int c) {
            return false;
        }
    }

    /**
     * @return a list of possible map names that could be used to start a game
     */
    public static String[] getMapNames() {
        return OldWorldImpl.getMapNames();
    }
}