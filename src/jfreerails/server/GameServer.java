package jfreerails.server;

import java.util.Vector;

import java.io.IOException;
import java.net.SocketException;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import jfreerails.controller.CalcSupplyAtStations;
import jfreerails.controller.ConnectionToServer;
import jfreerails.controller.ConnectionListener;
import jfreerails.controller.InetConnection;
import jfreerails.controller.LocalConnection;
import jfreerails.controller.MoveChainFork;
import jfreerails.controller.MoveExecuter;
import jfreerails.controller.ServerControlInterface;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldListListener;

/**
 * This implements a game server and keeps track of connections to clients and
 * suchlike.
 *
 * @author lindsal
 */

public class GameServer implements ConnectionListener {
    private World world;
    private ServerGameEngine gameEngine;
    private MoveChainFork moveChainFork;
    private InetConnection serverSocket;
    private FreerailsProgressMonitor pm;
    private MoveExecuter moveExecuter;

    /**
     * The connections that this server has
     */
    private Vector connections = new Vector();

    public void connectionClosed(ConnectionToServer c) {
	synchronized (connections) {
	    if (! (c instanceof LocalConnection)) {
		tableModel.removeRow(c);
		connections.remove(c);
		moveChainFork.remove(c);
	    }
	}
    }
    
    /**
     * Sits in a loop and accepts incoming connections over the network
     */
    private class InetGameServer implements Runnable {
	public void run() {
	    try {
		while (true) {
		    try {
			ConnectionToServer c = serverSocket.accept();
			c.addConnectionListener(GameServer.this);
			moveChainFork.add(c);		
			c.addMoveReceiver(moveExecuter);
			synchronized (connections) {
			    connections.add(c);
			}
			tableModel.addRow (c,
				((InetConnection) c).getRemoteAddress().toString());
		    } catch (IOException e) {
			if (e instanceof SocketException) {
			    throw (SocketException) e;
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
     * starts the server running initialised from a new map
     */
    public GameServer(String mapName, FreerailsProgressMonitor pm) {
	moveChainFork = new MoveChainFork();
	world = OldWorldImpl.createWorldFromMapFile(mapName, pm);
	moveExecuter = new MoveExecuter(world, moveChainFork, this);
	gameEngine = new ServerGameEngine(world, this, moveChainFork); 
	WorldListListener listener = new CalcSupplyAtStations(world);
	moveChainFork.addListListener(listener);
	/* Open our server socket */
	try {
	    serverSocket = new InetConnection(world, this);
	} catch (IOException e) {
	    System.err.println("Couldn't open the server socket!!!" + e);
	    throw new RuntimeException (e);
	}
	
	Thread thread = new Thread(new InetGameServer());
	thread.start();
    }
    
    /**
     * Starts the server thread.
     * TODO control of whether clients can issue moves prior to the thread being
     * started.
     */
    public void startGame() {
	Thread thread = new Thread(gameEngine);
	thread.start();
    }

    public LocalConnection getLocalConnection() {
	synchronized (connections) {
	    LocalConnection connection = new LocalConnection(world, this);
	    moveChainFork.add(connection);	    
	    connection.addMoveReceiver(moveExecuter);
	    connections.add(connection);
	    tableModel.addRow(connection, "Local connection");
	    connection.addConnectionListener(this);
	    return connection;
	}
    }

    public ServerControlInterface getServerControls() {
	return gameEngine;
    }

    void setWorld(World w) {
	synchronized (connections) {
	    world = w;
	    MoveExecuter oldExecuter = moveExecuter;
		moveExecuter = new MoveExecuter(world, moveChainFork, this);
	    for (int i = 0; i < connections.size(); i++) {
		ConnectionToServer c = (ConnectionToServer) connections.get(i);
		if (c instanceof LocalConnection) {
		    ((LocalConnection) c).setWorld(world);
		    ((LocalConnection) c).removeMoveReceiver(oldExecuter);
		    ((LocalConnection)
		     c).addMoveReceiver(moveExecuter);
		}
	    }
	    if (serverSocket != null)
		serverSocket.setWorld(world);
	}
    }

    public void connectionStateChanged(ConnectionToServer c) {
	tableModel.stateChanged(c);
    }

    /**
     * Table model which represents currently connected clients.
     * Connection states are described as follows:
     * <ol>
     */
    private class ClientConnectionTableModel extends DefaultTableModel {
	public ClientConnectionTableModel() {
	    super(new String[]{"Client address", "State"}, 0);
	}

	public void addRow(ConnectionToServer c, String address) {
	    addRow(new String[]{address, c.getConnectionState().toString()});
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

    private ClientConnectionTableModel tableModel = new
	ClientConnectionTableModel();

    public TableModel getClientConnectionTableModel() {
	return tableModel;
    }
	/**
	 * @return Returns the moveExecuter.
	 */
	public MoveExecuter getMoveExecuter() {
		return moveExecuter;
	}

}
