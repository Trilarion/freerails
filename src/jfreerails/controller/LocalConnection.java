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
    
    /**
     * Indicates whether the connection should forward moves the the remote side
     */
    private boolean sendMoves = false;

    /**
     * This object is used to hold a lock whilst the servers World is being
     * updated. Clients should acquire this lock whilst accessing the World.
     */
    private Object mutex;

    public Object getMutex() {
	return mutex;
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
	if (sendMoves)
	    peer.sendMove(move);
    }
    
    /**
     * TODO get rid of this
     */
    public void undoLastMove() {
	if (sendMoves)
	    peer.sendUndoLastMove();
    }

    /**
     * This constructor is called by the server
     */
    public LocalConnection(World w, Object mutex) {
	world = w;
	this.mutex = mutex;
    }

    /**
     * This constructor is called by the client
     */
    public LocalConnection(LocalConnection peer) {
	this.peer = peer;
    }

    protected void sendMove(Move move) {
	moveReceiver.processMove(move);
    }
    
    /**
     * TODO get rid of this
     */
    protected void sendUndoLastMove() {
	((UncommittedMoveReceiver) moveReceiver).undoLastMove();
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
    }

    public World loadWorldFromServer() {
	sendMoves = true;
	return world;
    }

    public void open() {
	peer.connect(this);
	mutex = peer.mutex;
	System.out.println("got mutex from remote side: " + mutex);
	world = peer.world;
    }

    public void close() {
	sendMoves = false;
	world = null;
	mutex = null;
	peer.disconnect();
    }

    /**
     * Called by the server
     */
    public void setWorld(World w) {
	world = w;
    }
}
