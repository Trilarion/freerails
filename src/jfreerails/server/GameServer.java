package jfreerails.server;

import java.util.Vector;

import jfreerails.controller.CalcSupplyAtStations;
import jfreerails.controller.ConnectionToServer;
import jfreerails.controller.LocalConnection;
import jfreerails.controller.MoveChainFork;
import jfreerails.controller.MoveExecuter;
import jfreerails.controller.ServerControlInterface;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldListListener;

/**
 * This implements a game server and keeps track of connections to clients and
 * suchlike.
 *
 * @author lindsal
 */

public class GameServer {
	private World world;
	private ServerGameEngine gameEngine;
	private MoveChainFork moveChainFork;

	/**
	 * The connections that this server has
	 */
	private Vector connections = new Vector();

	/**
	 * starts the server running initialised from a new map
	 */
	public GameServer(String mapName) {
		moveChainFork = new MoveChainFork();
		world = OldWorldImpl.createWorldFromMapFile(mapName);
		MoveExecuter.init(world, moveChainFork);
		gameEngine = new ServerGameEngine(world, this, moveChainFork);
		WorldListListener listener = new CalcSupplyAtStations(world);
		moveChainFork.addListListener(listener);
		/**
		 * start the server thread
		 */
		Thread thread = new Thread(gameEngine);
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
			MoveExecuter.init(world, moveChainFork);
			for (int i = 0; i < connections.size(); i++) {
				ConnectionToServer c = (ConnectionToServer) connections.get(i);
				if (c instanceof LocalConnection) {
					((LocalConnection) c).setWorld(world);
					((LocalConnection) c).removeMoveReceiver(oldExecuter);
					((LocalConnection) c).addMoveReceiver(
						MoveExecuter.getMoveExecuter());
				}
			}
		}
	}
	public World getWorld() {
		return world;
	}

}
