package jfreerails.controller;

import jfreerails.move.Move;
import jfreerails.world.top.World;


/**
 * This class implements a local connection to a server contained within the
 * same JVM.
 */
public class LocalConnection implements ConnectionToServer {
    private MoveReceiver moveReceiver;
    private LocalConnection peer;
    private World world;
    private ConnectionListener connectionListener;
    private ConnectionState state = ConnectionState.CLOSED;

    /**
     * Indicates whether the connection should forward moves the the remote side
     */
    private boolean sendMoves = false;

    /**
     * This object is used to hold a lock whilst the servers World is being
     * updated. Clients should acquire this lock whilst accessing the World.
     */
    private Object mutex;

    public void flush() {
        // do nothing
    }

    /**
     * @deprecated
     */
    public Object getMutex() {
        return mutex;
    }

    public void addConnectionListener(ConnectionListener l) {
        connectionListener = l;
    }

    public void removeConnectionListener(ConnectionListener l) {
        connectionListener = null;
    }

    public void addMoveReceiver(MoveReceiver m) {
        moveReceiver = m;
    }

    public void removeMoveReceiver(MoveReceiver m) {
        moveReceiver = null;
    }

    /*
     * TODO implement this
     public void send(FreerailsSerializable s);
     */

    /**
     * TODO get rid of this
     */
    public void processMove(Move move) {
        if (sendMoves) {
            peer.sendMove(move);
        }
    }

    /**
     * TODO get rid of this
     */
    public void undoLastMove() {
        if (sendMoves) {
            peer.sendUndoLastMove();
        }
    }

    /**
     * This constructor is called by the server
     * @deprecated
     */
    public LocalConnection(World w, Object mutex) {
        world = w;
        this.mutex = mutex;
        setState(ConnectionState.WAITING);
    }

    /**
     * This constructor is called by the client
     */
    public LocalConnection(LocalConnection peer) {
        this.peer = peer;
    }

    protected void sendMove(Move move) {
        if (moveReceiver != null) {
            moveReceiver.processMove(move);
        }
    }

    /**
     * TODO get rid of this
     */
    protected void sendUndoLastMove() {
        ((UncommittedMoveReceiver)moveReceiver).undoLastMove();
    }

    /**
     * This is called by the client connection object on the servers connection
     * object
     */
    protected boolean connect(LocalConnection peer) {
        this.peer = peer;
        sendMoves = true;

        return true;
    }

    /**
     * This is called by the client connection object on the servers connection
     * object
     */
    protected void disconnect() {
        sendMoves = false;
        this.peer = null;

        if (connectionListener != null) {
            connectionListener.connectionClosed(this);
        }
    }

    public World loadWorldFromServer() {
        sendMoves = true;

        /* set the state on the server connection to say that the client is
         * ready to receive moves */
        setState(ConnectionState.READY);
        peer.setState(ConnectionState.READY);

        return world;
    }

    public void open() {
        peer.connect(this);
        mutex = peer.mutex;

        world = peer.world;
        setState(ConnectionState.WAITING);
        peer.setState(ConnectionState.WAITING);
    }

    public void close() {
        sendMoves = false;
        world = null;
        mutex = null;
        peer.disconnect();
        setState(ConnectionState.CLOSED);

        if (connectionListener != null) {
            connectionListener.connectionClosed(this);
        }
    }

    /**
     * Called by the server
     */
    public void setWorld(World w) {
        world = w;
    }

    public ConnectionState getConnectionState() {
        return state;
    }

    private void setState(ConnectionState s) {
        state = s;

        if (connectionListener != null) {
            connectionListener.connectionStateChanged(this);
        }
    }

    /**
     * @deprecated
     */
    public void setMutex(Object mutex) {
        this.mutex = mutex;
    }
}