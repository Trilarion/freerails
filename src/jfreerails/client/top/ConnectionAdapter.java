package jfreerails.client.top;

import jfreerails.controller.LocalConnection;
import jfreerails.controller.ConnectionToServer;
import jfreerails.controller.UntriedMoveReceiver;
import jfreerails.controller.MoveReceiver;
import jfreerails.world.top.World;
import jfreerails.move.MoveStatus;
import jfreerails.move.WorldChangedEvent;
import jfreerails.move.Move;

/**
 * This class receives moves from the client. This class tries out moves on the
 * world if necessary, and passes them to the connection.
 */
public class ConnectionAdapter implements UntriedMoveReceiver {

	ConnectionToServer connection;
	MoveReceiver moveReceiver;
	World world;
	/**
	 * The mutex that controls access to the local DB
	 */
	Object mutex;

	/**
	 * This class receives moves from the connection and updates the world
	 * accordingly.
	 * TODO - this cannot be completed until all world changes are serialized
	 * into moves. This will eventually implement EventReceiver instead of
	 * MoveReceiver.
	 * XXX large numbers of incoming moves may cause problems competing for
	 * mutex?? Not a problem for local connection as we already have the lock.
	 */
	public class WorldUpdater implements MoveReceiver {
		public void processMove(Move move) {
			/* synchronize on local copy of World */
			synchronized (mutex) {
				if (move instanceof WorldChangedEvent) {
					System.out.println("received world changed event");
					setConnection(connection);
				}
				moveReceiver.processMove(move);
			}
		}
	}

	private WorldUpdater worldUpdater = new WorldUpdater();

	public void processMove(Move move) {
		connection.processMove(move);
	}

	public void undoLastMove() {
		connection.undoLastMove();
	}

	public MoveStatus tryDoMove(Move move) {
		return move.tryDoMove(world);
	}

	public MoveStatus tryUndoMove(Move move) {
		return move.tryUndoMove(world);
	}

	public void setConnection(ConnectionToServer c) {
		if (connection != null) {
			connection.close();
			connection.removeMoveReceiver(worldUpdater);
			System.out.println("Connection closed");
		}
		connection = c;
		connection.addMoveReceiver(worldUpdater);
		connection.open();
		System.out.println("Connection opened");
		world = connection.loadWorldFromServer();
		if (connection instanceof LocalConnection) {
			mutex = ((LocalConnection) connection).getMutex();
		} else {
			/* mutex = some other object */
		}
	}

	public void setMoveReceiver(MoveReceiver m) {
		moveReceiver = m;
	}
}
