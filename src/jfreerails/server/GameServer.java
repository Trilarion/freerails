package jfreerails.server;

import java.util.Vector;

import java.io.IOException;
import java.net.SocketException;

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

    /**
     * The connections that this server has
     */
    private Vector connections = new Vector();

    public void connectionClosed(ConnectionToServer c) {
	synchronized (connections) {
	    connections.remove(c);
	    moveChainFork.remove(c);
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
			MoveExecuter executer = MoveExecuter.getMoveExecuter();
			c.addMoveReceiver(executer);
			synchronized (connections) {
			    connections.add(c);
			}
			Thread thread = new Thread((InetConnection) c);
			thread.start();
		    } catch (IOException e) {
			if (e instanceof SocketException) {
			    throw (SocketException) e;
			} else {
			    System.out.println("Caught an IOException whilst " +
				    "trying to accept an incoming connection" + e);
			}
		    }
		}
	    } catch (SocketException e) {
		/* The socket was probably closed, exit */
		System.out.println("Server socket closed??? - Exiting");
	    }
	}
    }
    
    /**
     * starts the server running initialised from a new map
     */
    public GameServer(String mapName, FreerailsProgressMonitor pm) {
	moveChainFork = new MoveChainFork();
	world = OldWorldImpl.createWorldFromMapFile(mapName, pm);
	MoveExecuter.init(world, moveChainFork, this);
	gameEngine = new ServerGameEngine(world, this, moveChainFork); 
	WorldListListener listener = new CalcSupplyAtStations(world);
	moveChainFork.addListListener(listener);
	/* Open our server socket */
	try {
	    serverSocket = new InetConnection(world, this);
	} catch (IOException e) {
	    System.out.println("Couldn't open the server socket!!!" + e);
	    throw new RuntimeException (e);
	}
	
	/**
	 * start the server thread
	 */
	Thread thread = new Thread(gameEngine);
	thread.start();

	thread = new Thread(new InetGameServer());
	thread.start();
    }
    
    public LocalConnection getLocalConnection() {
	synchronized (connections) {
	    LocalConnection connection = new LocalConnection(world, this);
	    moveChainFork.add(connection);
	    MoveExecuter executer = MoveExecuter.getMoveExecuter();
	    connection.addMoveReceiver(executer);
	    connections.add(connection);
	    return connection;
	}
    }

    public ServerControlInterface getServerControls() {
	return gameEngine;
    }

    void setWorld(World w) {
	synchronized (connections) {
	    world = w;
	    MoveExecuter oldExecuter = MoveExecuter.getMoveExecuter();
	    MoveExecuter.init(world, moveChainFork, this);
	    for (int i = 0; i < connections.size(); i++) {
		ConnectionToServer c = (ConnectionToServer) connections.get(i);
		if (c instanceof LocalConnection) {
		    ((LocalConnection) c).setWorld(world);
		    ((LocalConnection) c).removeMoveReceiver(oldExecuter);
		    ((LocalConnection)
		     c).addMoveReceiver(MoveExecuter.getMoveExecuter());
		}
	    }
	    if (serverSocket != null)
		serverSocket.setWorld(world);
	}
    }
}
