package jfreerails.server;

import java.io.IOException;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import jfreerails.controller.ConnectionListener;
import jfreerails.controller.ConnectionToServer;
import jfreerails.controller.InetConnection;
import jfreerails.controller.LocalConnection;
import jfreerails.controller.MoveChainFork;
import jfreerails.controller.MoveReceiver;
import jfreerails.controller.ServerControlInterface;
import jfreerails.move.WorldChangedEvent;
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
        private ClientConnectionTableModel tableModel = new ClientConnectionTableModel(connections);

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
                            gameEngine.getGameMutex(),
                            InetConnection.SERVER_PORT);
                } catch (IOException e) {
                    System.err.println("Couldn't open the server socket!!!" +
                        e);
                    throw new RuntimeException(e);
                }

                Thread thread = new Thread(new InetGameServer(serverSocket, this));
                thread.start();
            }

            startGame();
        }

        /**
         * return a brand new local connection.
         */
        public LocalConnection getLocalConnection() {
            synchronized (connections) {
                LocalConnection connection = new LocalConnection(gameEngine.getWorld(),
                        gameEngine.getGameMutex());
                addConnection(connection);

                return connection;
            }
        }

        public void connectionClosed(ConnectionToServer c) {
            synchronized (connections) {
                if (!(c instanceof LocalConnection)) {
                    tableModel.removeRow(c);
                    connections.remove(c);
                    moveChainFork.remove(c);
                }
            }
        }

        private void removeConnection(ConnectionToServer c) {
            synchronized (connections) {
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
                    ((LocalConnection)c).setMutex(gameEngine.getGameMutex());
                }
            }
        }

        /**
         * Starts the server thread.
         * TODO control of whether clients can issue moves prior to the thread being
         * started.
         */
        private void startGame() {
            Thread thread = new Thread(gameEngine);
            thread.start();
        }

        /**
         * Create a new ServerGameEngine instance and transfer all clients of
         * this game to the new one.
         */
        public void loadGame() {
            /* open a new controller */
            ServerGameEngine newGame = ServerGameEngine.loadGame();

            transferClients(newGame);
            startGame();
        }

        public void saveGame() {
            gameEngine.saveGame();
        }

        public String[] getMapNames() {
            return GameServer.this.getMapNames();
        }

        public void setTargetTicksPerSecond(int ticksPerSecond) {
            gameEngine.setTargetTicksPerSecond(ticksPerSecond);
        }

        /**
         * stop the current game and transfer the current local connections to a
         * new game running the specified map.
         */
        public void newGame(String mapName) {
            ServerGameEngine newGame = new ServerGameEngine(mapName,
                    FreerailsProgressMonitor.NULL_INSTANCE);
            transferClients(newGame);

            startGame();
        }

        /**
         * transfer all clients of this game to the new game
         */
        private void transferClients(ServerGameEngine newGame) {
            Vector localConnections = new Vector();
            MoveReceiver oldExecuter = gameEngine.getMoveExecuter();

            synchronized (connections) {
                Iterator i = connections.iterator();

                while (i.hasNext()) {
                    ConnectionToServer c = (ConnectionToServer)i.next();

                    /* Local connections must be transferred manually - remote
                     * connections are sent a WorldChangedEvent later */
                    if (c instanceof LocalConnection) {
                        i.remove();
                        localConnections.add(c);
                    }
                }

                gameEngine.stop();
                newGame.setTargetTicksPerSecond(gameEngine.getTargetTicksPerSecond());
                gameEngine = newGame;
                moveChainFork = newGame.getMoveChainFork();
                serverSocket.setWorld(gameEngine.getWorld());

                while (!localConnections.isEmpty()) {
                    LocalConnection lc = (LocalConnection)localConnections.remove(0);
                    addConnection(lc);
                    lc.processMove(new WorldChangedEvent());
                }

                /* send all remaining clients notification that this game is
                 * about to end */
                for (i = connections.iterator(); i.hasNext();) {
                    ConnectionToServer c = (ConnectionToServer)i.next();

                    if (!(c instanceof LocalConnection)) {
                        c.processMove(new WorldChangedEvent());
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
        FreerailsProgressMonitor pm) {
        ServerGameEngine gameEngine = new ServerGameEngine(mapName, pm);
        ServerGameController sgc = new ServerGameController(gameEngine,
                InetConnection.SERVER_PORT /* TODO */);
        gameControllers.add(sgc);

        return sgc;
    }

    /**
     * Load a saved game
     */
    public ServerControlInterface getSavedGame(FreerailsProgressMonitor pm) {
        ServerGameEngine gameEngine = ServerGameEngine.loadGame();
        ServerGameController sgc = new ServerGameController(gameEngine,
                InetConnection.SERVER_PORT /* TODO */);
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

        public ClientConnectionTableModel(Vector connections) {
            super(new String[] {"Client address", "State"}, 0);
            this.connections = connections;
        }

        public void addRow(ConnectionToServer c, String address) {
            addRow(new String[] {address, c.getConnectionState().toString()});
        }

        public void stateChanged(ConnectionToServer c) {
            int i;

            synchronized (connections) {
                i = connections.indexOf(c);
                setValueAt(c.getConnectionState().toString(), i, 1);
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
    public String[] getMapNames() {
        return OldWorldImpl.getMapNames();
    }
}