package jfreerails.controller;

import jfreerails.move.Move;
import jfreerails.world.top.World;


/**
 * This class implements a local connection to a server contained within the
 * same JVM.
 * @author rob
 */
public class LocalConnection implements ConnectionToServer {
    private MoveReceiver moveReceiver;
    private LocalConnection peer;
    private World world;
    private ConnectionListener connectionListener;
    private ConnectionState state = ConnectionState.CLOSED;

    /**
    * Indicates whether the connection should forward moves the the remote side.
    */
    private boolean sendMoves = false;

    public void flush() {
        // do nothing
    }

    public void addConnectionListener(ConnectionListener l) {
        connectionListener = l;
    }

    public void removeConnectionListener() {
        connectionListener = null;
    }

    public void addMoveReceiver(MoveReceiver m) {
        moveReceiver = m;
    }

    public void removeMoveReceiver(MoveReceiver m) {
        moveReceiver = null;
    }

    public void processMove(Move move) {
        if (sendMoves) {
            peer.sendMove(move);
        }
    }

    /**
    * This constructor is called by the server.
    */
    public LocalConnection(World w) {
        world = w;
        setState(ConnectionState.WAITING);
    }

    /**
    * This constructor is called by the client.
    */
    public LocalConnection(LocalConnection peer) {
        this.peer = peer;
    }

    private void sendMove(Move move) {
        if (moveReceiver != null) {
            moveReceiver.processMove(move);
        }
    }

    /**
    * This is called by the client connection object on the servers connection
    * object.
    */
    private void connect(LocalConnection peer) {
        this.peer = peer;
        sendMoves = true;
    }

    /**
    * This is called by the client connection object on the servers connection
    * object.
    */
    private void disconnect() {
        sendMoves = false;
        this.peer = null;

        if (connectionListener != null) {
            connectionListener.connectionClosed(this);
        }
    }

    //    private Serializable defensiveCopy(Serializable s) {
    //        try {
    //            ByteArrayOutputStream out = new ByteArrayOutputStream();
    //            ObjectOutputStream objectOut = new ObjectOutputStream(out);
    //            objectOut.writeObject(s);
    //            objectOut.flush();
    //
    //            byte[] bytes = out.toByteArray();
    //
    //            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
    //            ObjectInputStream objectIn = new ObjectInputStream(in);
    //            Object o = objectIn.readObject();
    //
    //            return (Serializable)o;
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //            throw new IllegalStateException(e.getMessage());
    //        }
    //    }
    public World loadWorldFromServer() {
        sendMoves = true;

        /* set the state on the server connection to say that the client is
        * ready to receive moves */
        setState(ConnectionState.READY);
        peer.setState(ConnectionState.READY);

        assert peer.world != null;

        /* create a copy of the world */
        return peer.world.defensiveCopy();
    }

    public void open() {
        peer.connect(this);
        setState(ConnectionState.WAITING);
        peer.setState(ConnectionState.WAITING);
    }

    public void close() {
        sendMoves = false;
        world = null;
        peer.disconnect();
        setState(ConnectionState.CLOSED);

        if (connectionListener != null) {
            connectionListener.connectionClosed(this);
        }
    }

    /**
    * Called by the server.
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

    private void sendServerCommand(ServerCommand s) {
        if (connectionListener != null) {
            connectionListener.processServerCommand(this, s);
        }
    }

    /**
    * Send a server command to the remote peer.
    */
    public void sendCommand(ServerCommand s) {
        peer.sendServerCommand(s);
    }
}