package jfreerails.client.top;

import java.io.IOException;

import jfreerails.controller.CompositeMoveSplitter;
import jfreerails.controller.InetConnection;
import jfreerails.controller.LocalConnection;
import jfreerails.controller.ConnectionToServer;
import jfreerails.controller.UntriedMoveReceiver;
import jfreerails.controller.MoveExecuter;
import jfreerails.controller.MoveReceiver;
import jfreerails.world.top.World;
import jfreerails.move.MoveStatus;
import jfreerails.move.TimeTickMove;
import jfreerails.move.WorldChangedEvent;
import jfreerails.move.Move;

/**
 * This class receives moves from the client. This class tries out moves on the
 * world if necessary, and passes them to the connection.
 */
public class ConnectionAdapter implements UntriedMoveReceiver {

    private MoveExecuter moveExecuter;

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
	private MoveReceiver moveReceiver;

	public void processMove(Move move) {
	    if (move instanceof WorldChangedEvent) {
		System.out.println("received world changed event");
		try {
		    setConnection(connection);
		} catch (IOException e) {
		    System.out.println("IOException occurred whilst " +
			    " whilst setting connection");
		    closeConnection();	
		}
	    } else if (move instanceof TimeTickMove) {
		/*
		 * flush our outgoing moves prior to receiving next tick
		 * TODO improve our buffering strategy
		 */
		connection.flush();
	    }
	    synchronized(mutex) {
		moveReceiver.processMove(move);
	    }
	}

	public void setMoveReceiver(MoveReceiver moveReceiver) {
	    synchronized(mutex) {
		this.moveReceiver = moveReceiver;
	    }
	}
    }

    private WorldUpdater worldUpdater = new WorldUpdater();

    public void processMove(Move move) {
	if (connection != null)
	    connection.processMove(move);
    }

    public void undoLastMove() {
	if (connection != null)
	    connection.undoLastMove();
    }

    public MoveStatus tryDoMove(Move move) {
	synchronized(mutex) {
	    return move.tryDoMove(world);
	}
    }

    public MoveStatus tryUndoMove(Move move) {
	synchronized(mutex) {
	    return move.tryUndoMove(world);
	}
    }

    private void closeConnection() {
	connection.close();
	connection.removeMoveReceiver(worldUpdater);
	System.out.println("ConnectionToServer closed");
    }

    public void setConnection(ConnectionToServer c) throws IOException {
	if (connection != null) {
	    closeConnection();
	    connection.removeMoveReceiver(worldUpdater);
	}
	connection = c;
	connection.open();
	    
	if (connection instanceof LocalConnection) {
	    mutex = ((LocalConnection) connection).getMutex();
	    worldUpdater.setMoveReceiver(moveReceiver);
	} else {
	    /* mutex = some other object */
	    mutex = new Integer (0);
	}
	/* don't allow other events to update until we've downloaded our copy of
	 * the World */
	synchronized(mutex) {
	    connection.addMoveReceiver(worldUpdater);
	    if (connection instanceof InetConnection) {
		Thread receiveThread = new Thread((InetConnection) connection);
		receiveThread.start();
	    }
	    world = connection.loadWorldFromServer();
	    if (! (connection instanceof LocalConnection)) {
		MoveExecuter.init(world, moveReceiver, mutex);
		moveExecuter = MoveExecuter.getMoveExecuter();
		worldUpdater.setMoveReceiver(moveExecuter);
	    }
	}
    }

    public void setMoveReceiver(MoveReceiver m) {
	moveReceiver = new CompositeMoveSplitter(m);
    }

    public Object getMutex() {
	return mutex;
    }
}
