package jfreerails.client.top;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.IOException;
import jfreerails.client.common.SynchronizedEventQueue;
import jfreerails.client.view.ModelRoot;
import jfreerails.controller.ConnectionToServer;
import jfreerails.controller.LocalConnection;
import jfreerails.controller.MoveExecuter;
import jfreerails.controller.MoveReceiver;
import jfreerails.controller.UncommittedMoveReceiver;
import jfreerails.controller.UntriedMoveReceiver;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.TimeTickMove;
import jfreerails.move.WorldChangedEvent;
import jfreerails.world.top.World;


/**
 * This class receives moves from the client. This class tries out moves on the
 * world if necessary, and passes them to the connection.
 */
public class ConnectionAdapter implements UntriedMoveReceiver {
    private MoveExecuter moveExecuter;
    private ModelRoot modelRoot;
    ConnectionToServer connection;

    /**
     * we forward outbound moves from the client to this.
     */
    UncommittedMoveReceiver uncommittedReceiver;
    MoveReceiver moveReceiver;
    World world;

    /**
     * The mutex that controls access to the local DB
     */
    Object mutex;

    public ConnectionAdapter(ModelRoot mr) {
        modelRoot = mr;
    }

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

        /**
         * Processes inbound moves from the server
         */
        public void processMove(Move move) {
            if (move instanceof WorldChangedEvent) {
                try {
                    setConnection(connection);
                } catch (IOException e) {
                    modelRoot.getUserMessageLogger().println("Unable to open" +
                        " remote connection");
                    closeConnection();
                }
            } else if (move instanceof TimeTickMove) {
                /*
                 * flush our outgoing moves prior to receiving next tick
                 * TODO improve our buffering strategy
                 */
                connection.flush();
            }

            synchronized (mutex) {
                moveReceiver.processMove(move);
            }
        }

        public void setMoveReceiver(MoveReceiver moveReceiver) {
            synchronized (mutex) {
                this.moveReceiver = moveReceiver;
            }
        }
    }

    private WorldUpdater worldUpdater = new WorldUpdater();

    /**
     * Processes outbound moves to the server
     */
    public void processMove(Move move) {
        if (uncommittedReceiver != null) {
            uncommittedReceiver.processMove(move);
        }
    }

    public void undoLastMove() {
        if (uncommittedReceiver != null) {
            uncommittedReceiver.undoLastMove();
        }
    }

    public MoveStatus tryDoMove(Move move) {
        synchronized (mutex) {
            return move.tryDoMove(world);
        }
    }

    public MoveStatus tryUndoMove(Move move) {
        synchronized (mutex) {
            return move.tryUndoMove(world);
        }
    }

    private void closeConnection() {
        connection.close();
        connection.removeMoveReceiver(worldUpdater);
        modelRoot.getUserMessageLogger().println("Connection to server closed");
    }

    public void setConnection(ConnectionToServer c) throws IOException {
        SynchronizedEventQueue eventQueue = SynchronizedEventQueue.getInstance();

        if (connection != null) {
            closeConnection();
            connection.removeMoveReceiver(worldUpdater);

            if ((mutex != null)) {
                eventQueue.removeMutex(mutex);
            }
        }

        connection = c;
        connection.open();

        if (connection instanceof LocalConnection) {
            mutex = ((LocalConnection)connection).getMutex();
            worldUpdater.setMoveReceiver(moveReceiver);
        } else {
            /* mutex = some other object */
            mutex = new Integer(0);
        }

        /* add our mutex to the AWT event queue's mutex list */
        eventQueue.addMutex(mutex);

        /* don't allow other events to update until we've downloaded our copy of
         * the World */
        synchronized (mutex) {
            connection.addMoveReceiver(worldUpdater);
            world = connection.loadWorldFromServer();
            modelRoot.setWorld(world);

            if (!(connection instanceof LocalConnection)) {
                NonAuthoritativeMoveExecuter moveExecuter = new NonAuthoritativeMoveExecuter(world,
                        moveReceiver, mutex, modelRoot);
                worldUpdater.setMoveReceiver(moveExecuter);
                uncommittedReceiver = moveExecuter.getUncommittedMoveReceiver();
                ((NonAuthoritativeMoveExecuter.PendingQueue)uncommittedReceiver).addMoveReceiver(connection);
            } else {
                uncommittedReceiver = connection;
            }
        }
    }

    public void setMoveReceiver(MoveReceiver m) {
        //moveReceiver = new CompositeMoveSplitter(m);
        //I don't want moves split at this stage since I want to be able
        //to listen for composite moves.
        moveReceiver = m;
    }

    /**
     * @deprecated
     */
    public Object getMutex() {
        return mutex;
    }
}