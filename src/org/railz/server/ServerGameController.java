/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * Created on Feb 18, 2004
 */
package org.railz.server;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.TableModel;

import org.railz.config.LogManager;
import org.railz.controller.AddPlayerCommand;
import org.railz.controller.AddPlayerResponseCommand;
import org.railz.controller.ConnectionListener;
import org.railz.controller.ConnectionToServer;
import org.railz.controller.InetConnection;
import org.railz.controller.LocalConnection;
import org.railz.controller.MoveChainFork;
import org.railz.controller.MoveReceiver;
import org.railz.controller.ResourceBundleManager;
import org.railz.controller.ServerCommand;
import org.railz.controller.ServerCommandReceiver;
import org.railz.controller.ServerControlInterface;
import org.railz.controller.ServerMessageCommand;
import org.railz.controller.WorldChangedCommand;
import org.railz.util.FreerailsProgressMonitor;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;

/**
 * associates an instance of ServerGameEngine with a set of game controls.
 * Manages connectivity to the ServerGameEngine.
 */
class ServerGameController implements ServerControlInterface, ConnectionListener,
	ServerCommandReceiver {
    /**
     * The connections that this server has
     */
    private Vector connections = new Vector();
    private MoveChainFork moveChainFork;
    private InetConnection serverSocket;
    ServerGameEngine gameEngine;
    private ClientConnectionTableModel tableModel = new ClientConnectionTableModel(this);
    
    private static final String CLASS_NAME = ServerGameController.class.getName();
    private static final Logger logger = LogManager.getLogger(CLASS_NAME);
    
    public ServerGameController(ServerGameEngine engine, int port) {
	moveChainFork = engine.getMoveChainFork();
	gameEngine = engine;
	gameEngine.setServerCommandReceiver(this);
	
	if (port != 0) {
	    /* Open our server socket */
	    try {
		serverSocket = new InetConnection(this, InetConnection.SERVER_PORT);
	    } catch (IOException e) {
		logger.log(Level.SEVERE, "Couldn't open the server socket!!!" + e, e);
		throw new RuntimeException(e);
	    }
	}
    }
    
    private synchronized void sendMessageToClients(String message, Serializable[] objects) {
	ServerMessageCommand smc = new ServerMessageCommand(message, objects);
	for (int i = 0; i < connections.size(); i++) {
	    ConnectionToServer c = (ConnectionToServer) connections.get(i);
	    c.sendCommand(smc);
	}
    }
    
    private String getPlayerName(ConnectionToServer c) {
	IdentityProvider ip = gameEngine.getIdentityProvider();
	FreerailsPrincipal p = ip.getPrincipal(c);
	if (p != Player.NOBODY && ip.getPlayer(p) != null)
	    return (p.getName());
	else
	    return "Unknown player";
    }
    
    /**
     * return a brand new local connection.
     */
    public synchronized LocalConnection getLocalConnection() {
	LocalConnection connection = new LocalConnection();
	addConnection(connection);
	
	return connection;
    }
    
    public void connectionOpened(ConnectionToServer c) {
	addConnection(c);
    }
    
    public synchronized void connectionClosed(ConnectionToServer c) {
	/*
	 * If the player is connected locally, the connection is still active,
	 * so that the server may still be controlled via the connection, but
	 * the player must re-authenticate themselves in order to play
	 */
	String playerName = getPlayerName(c);
	if (!(c instanceof LocalConnection)) {
	    removeConnection(c);
	} else {
	    gameEngine.getIdentityProvider().removeConnection(c);
	    tableModel.stateChanged(c, connections.indexOf(c));
	}
	sendMessageToClients("{0} left the game.", new Serializable[] { playerName });
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
	    tableModel.addRow(c, ((InetConnection) c).getRemoteAddress().toString());
	} else if (c instanceof LocalConnection) {
	    ((LocalConnection) c).setWorld(gameEngine.getWorld());
	    tableModel.addRow(c, "Local connection");
	}
    }
    
    private abstract class LoadSaveAdapter extends Thread {
	public File file;
    }
    
    /**
     * Create a new ServerGameEngine instance and transfer all clients of this
     * game to the new one.
     */
    public void loadGame(File filename) {
	LoadSaveAdapter t = new LoadSaveAdapter() {
	    public void run() {
		int ticksPerSec = gameEngine.getTargetTicksPerSecond();
		
		/* open a new controller */
		ServerGameEngine newGame;
		try {
		    newGame = ServerGameEngine.loadGame(file);
		} catch (IOException e) {
		    sendMessageToClients("There was a problem loading the game: {0}",
			    new Serializable[] { e.getMessage() });
		    return;
		}
		
		transferClients(newGame);
		setTargetTicksPerSecond(ticksPerSec);
	    }
	};
	sendMessageToClients("Server is loading new saved game: {0}",
		new Serializable[] { filename.toString() });
	t.file = filename;
	t.start();
    }
    
    public void saveGame(File filename) {
	LoadSaveAdapter t = new LoadSaveAdapter() {
	    public void run() {
		gameEngine.saveGame(file);
	    }
	};
	t.file = filename;
	sendMessageToClients("Server is saving the game...", new Serializable[0]);
	t.start();
    }
    
    public String[] getMapNames() {
	return GameServer.getMapNames();
    }
    
    public String[] getScenarioNames() {
	Scenario[] scenarios = ScenarioManager.getScenarios();
	String[] names = new String[scenarios.length];
	for (int i = 0; i < scenarios.length; i++)
	    names[i] = scenarios[i].getName();
	return names;
    }
    
    public void setTargetTicksPerSecond(int ticksPerSecond) {
	gameEngine.setTargetTicksPerSecond(ticksPerSecond);
    }
    
    /**
     * stop the current game and transfer the current local connections to a new
     * game running the specified map.
     */
    public void newGame(String map, String scenarioName) {
	final String mapName = map;
	final Scenario scenario = ScenarioManager.getScenario(scenarioName);
	if (scenario == null)
	    return;
	
	Thread t = new Thread() {
	    public void run() {
		int ticksPerSec = gameEngine.getTargetTicksPerSecond();
		ServerGameEngine newGame = new ServerGameEngine(mapName,
			FreerailsProgressMonitor.NULL_INSTANCE, scenario);
		transferClients(newGame);
		
		setTargetTicksPerSecond(ticksPerSec);
	    }
	};
	sendMessageToClients("Server is starting a new map: {0}", new Serializable[] { map });
	t.start();
    }
    
    /**
     * transfer all clients of this game to the new game
     */
    private synchronized void transferClients(ServerGameEngine newGame) {
	Vector localConnections = new Vector();
	MoveReceiver oldExecuter = gameEngine.getMoveExecuter();
	
	for (int i = 0; i < connections.size(); i++) {
	    ConnectionToServer c = (ConnectionToServer) connections.get(i);
	    
	    /*
	     * Local connections must be transferred manually - remote
	     * connections are sent a WorldChangedCommand later
	     */
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
	    LocalConnection lc = (LocalConnection) localConnections.remove(0);
	    addConnection(lc);
	    lc.sendCommand(new WorldChangedCommand());
	}
	
	/*
	 * send all remaining clients notification that this game is about to
	 * end
	 */
	for (int i = 0; i < connections.size(); i++) {
	    ConnectionToServer c = (ConnectionToServer) connections.get(i);
	    
	    /*
	     * don't send locally connected clients the WorldChangedCommand as
	     * they have already been sent one
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
	sendMessageToClients("Server is shutting down. Bye...", new Serializable[0]);
	while (!connections.isEmpty()) {
	    ConnectionToServer c = (ConnectionToServer) connections.get(0);
	    
	    c.close();
	    removeConnection(c);
	}
	
	gameEngine.stop();
    }
    
    public void processServerCommand(ConnectionToServer c, ServerCommand s) {
	if (s instanceof AddPlayerCommand) {
	    AddPlayerCommand apc = (AddPlayerCommand) s;
	    
	    synchronized (this) {
		sendMessageToClients("Server received request to authenticate " + "player {0}",
			new Serializable[] { apc.getPlayer().getName() });
		
		if (!gameEngine.getIdentityProvider().addConnection(c, apc.getPlayer(),
			apc.getSignature())) {
		    c.sendCommand(new AddPlayerResponseCommand(apc, ""));
		    sendMessageToClients("{0}''s attempt to join was " + "rejected by the server",
			    new Serializable[] { apc.getPlayer().getName() });
		} else {
		    logger.log(Level.FINE, "sending addplayerresponsecommand");
		    c.sendCommand(new AddPlayerResponseCommand(gameEngine.getIdentityProvider()
			    .getPrincipal(c)));
		    logger.log(Level.FINE, "sending sendmessagetoclients");
		    sendMessageToClients("{0} joined the game", new Serializable[] { apc
			    .getPlayer().getName() });
		}
		
		tableModel.stateChanged(c, connections.indexOf(c));
	    }
	} else if (s instanceof ResourceBundleManager.GetResourceCommand) {
	    ResourceBundleManager.GetResourceCommand grc = (ResourceBundleManager.GetResourceCommand) s;
	    c.sendCommand(new ResourceBundleManager.GetResourceResponseCommand(
		    ResourceBundleManager.getResourceByteArray(grc.locale, grc.baseName)));
	}
    }
    
    public synchronized void sendCommand(ServerCommand s) {
	for (int i = 0; i < connections.size(); i++) {
	    ConnectionToServer c = (ConnectionToServer) connections.get(i);
	    c.sendCommand(s);
	}
    }
}
