/*
 * Created on Feb 18, 2004
 */
package jfreerails.server;

import java.io.IOException;
import java.util.Vector;
import javax.swing.table.TableModel;
import jfreerails.controller.AddPlayerCommand;
import jfreerails.controller.AddPlayerResponseCommand;
import jfreerails.controller.ConnectionListener;
import jfreerails.controller.ConnectionToServer;
import jfreerails.controller.InetConnection;
import jfreerails.controller.LocalConnection;
import jfreerails.controller.MoveChainFork;
import jfreerails.controller.ServerCommand;
import jfreerails.controller.ServerControlInterface;
import jfreerails.controller.SpeedChangedCommand;
import jfreerails.controller.WorldChangedCommand;
import jfreerails.util.FreerailsProgressMonitor;


/**
 * associates an instance of ServerGameEngine with a set of game controls.
 * Manages connectivity to the ServerGameEngine.
 */
class ServerGameController implements ServerControlInterface,
    ConnectionListener {
    /**
     * The connections that this server has
     */
    private Vector connections = new Vector();
    private MoveChainFork moveChainFork;
    private InetConnection serverSocket;
    ServerGameEngine gameEngine;
    private ClientConnectionTableModel tableModel = new ClientConnectionTableModel(this);

    public ServerGameController(ServerGameEngine engine, int port) {
        moveChainFork = engine.getMoveChainFork();
        gameEngine = engine;

        if (port != 0) {
            /* Open our server socket */
            try {
                serverSocket = new InetConnection(engine.getWorld(),
                        InetConnection.SERVER_PORT);
            } catch (IOException e) {
                System.err.println("Couldn't open the server socket!!!" + e);
                throw new RuntimeException(e);
            }

            Thread thread = new Thread(new InetGameServer(serverSocket, this));
            thread.start();
        }
    }

    /**
     * return a brand new local connection.
     */
    public synchronized LocalConnection getLocalConnection() {
        LocalConnection connection = new LocalConnection(gameEngine.getWorld());
        addConnection(connection);

        return connection;
    }

    public synchronized void connectionClosed(ConnectionToServer c) {
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
            tableModel.stateChanged(c, connections.indexOf(c));
        }
    }

    private synchronized void removeConnection(ConnectionToServer c) {
        gameEngine.getIdentityProvider().removeConnection(c);
        tableModel.removeRow(connections.indexOf(c));
        moveChainFork.remove(c);
        connections.remove(c);
        c.removeMoveReceiver(gameEngine.getMoveExecuter());
        c.removeConnectionListener(this);
    }

    synchronized void addConnection(ConnectionToServer c) {
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
        sendToAllConections(new SpeedChangedCommand(ticksPerSecond));
    }

    /**
     * Sends a server commamnd to all connections
     * @param serverCommand
     */
    public void sendToAllConections(ServerCommand serverCommand) {
        for (int i = 0; i < connections.size(); i++) {
            ConnectionToServer c = (ConnectionToServer)connections.get(i);

            c.sendCommand(serverCommand);
            c.flush();
        }
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
    private synchronized void transferClients(ServerGameEngine newGame) {
        Vector localConnections = new Vector();

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
            lc.sendCommand(new WorldChangedCommand());
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
                c.sendCommand(new WorldChangedCommand());
                c.flush();
            }
        }
    }

    public TableModel getClientConnectionTableModel() {
        return tableModel;
    }

    public void connectionStateChanged(ConnectionToServer c) {
        tableModel.stateChanged(c, connections.indexOf(c));
    }

    public synchronized void quitGame() {
        while (!connections.isEmpty()) {
            ConnectionToServer c = (ConnectionToServer)connections.get(0);

            c.close();
            removeConnection(c);
        }

        gameEngine.stop();
    }

    public void processServerCommand(ConnectionToServer c, ServerCommand s) {
        if (s instanceof AddPlayerCommand) {
            AddPlayerCommand apc = (AddPlayerCommand)s;

            synchronized (this) {
                System.out.println("Received request to authenticate player" +
                    " " + apc.getPlayer());

                if (!gameEngine.getIdentityProvider().addConnection(c,
                            apc.getPlayer(), apc.getSignature())) {
                    c.sendCommand(new AddPlayerResponseCommand(apc, ""));
                } else {
                    c.sendCommand(new AddPlayerResponseCommand(
                            gameEngine.getIdentityProvider().getPrincipal(c)));
                }

                tableModel.stateChanged(c, connections.indexOf(c));
            }
        }
    }
}