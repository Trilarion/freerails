package jfreerails.controller;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import jfreerails.move.Move;
import jfreerails.move.TimeTickMove;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.top.World;

/**
 * Implements a ConnectionToServer over the internet. Note that a single
 * InetConnection can be open() and close()d many times on the client, but is
 * only used once on the server (attempts to reconnect cause the socket to spawn
 * another instance).
 */
public class InetConnection extends Socket implements ConnectionToServer {
    /**
     * The socket which clients should connect to.
     * TODO determine this 'scientifically'
     */
    public static final int SERVER_PORT = 55000;

    private ConnectionState state = ConnectionState.CLOSED;

    private InetAddress serverAddress;

    private Object mutex;

    private ServerSocket serverSocket;
    private Socket socket;
    
    private World world;

    private ConnectionListener connectionListener;

    private Dispatcher dispatcher;
    
    private boolean worldNotYetLoaded = true;

    private Sender sender;

    public InetAddress getRemoteAddress() {
	return socket.getInetAddress();
    }

    private class Sender {
	private BufferedOutputStream sendQueue;

	private ObjectOutputStream objectOutputStream;

	public Sender() throws IOException {
	    /* open the ObjectOutputStream first and then flush it so that the
	     * remote side doesn't block waiting for it */
	    sendQueue = new BufferedOutputStream(socket.getOutputStream());
	    objectOutputStream = new
		ObjectOutputStream(sendQueue);
	    objectOutputStream.flush();
	    sendQueue.flush();
	}

	public synchronized void send(Serializable s) {
	    try {
		objectOutputStream.writeObject(s);
	    } catch (SocketException e) {
		e.printStackTrace();
		/*
		 * call disconnect instead of close, since we can't send a
		 * CloseConnectionCommand
		 */
		InetConnection.this.disconnect();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}

	public synchronized void flush() {
	    try {
		objectOutputStream.flush();
	    } catch (IOException e) {
		// ignore it
	    }
	}

	/**
	 * Closes the output stream. Called by disconnect()
	 */
	public synchronized void close() {
	    try {
		objectOutputStream.flush();
	    } catch (IOException e) {
	    	e.printStackTrace();		
	    }
	    try {
		objectOutputStream.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    try {
		sendQueue.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }	   
	}
    }

    public void addConnectionListener(ConnectionListener l) {
	connectionListener = l;
    }

    public void removeConnectionListener(ConnectionListener l) {
	connectionListener = null;
    }

    private class Dispatcher implements Runnable {
	private MoveReceiver moveReceiver;
	private ObjectInputStream objectInputStream;
	private boolean worldNotYetLoaded = true;

	public synchronized void addMoveReceiver(MoveReceiver m) {
	    moveReceiver = m;
	}

	public synchronized void removeMoveReceiver(MoveReceiver m) {
	    moveReceiver = null;
	}

	public synchronized void receiveWorld() throws IOException {
	    try {
		while (true) {
		    Object o = objectInputStream.readObject();
		    if (o instanceof World) {
			world = (World) o;
			setState(ConnectionState.READY);
			break;
		    } else {
			System.out.println("Received garbage whilst loading world:" +
				o );
		    }
		}
	    }  catch (ObjectStreamException e) {
		System.out.println("Caught object stream exception whilst loading "
			+ "world");
		throw new IOException(e.toString());
	    } catch (ClassNotFoundException e) {
		System.out.println("Received unknown class instead of world " + e);
		throw new IOException(e.toString());
	    }
	    worldNotYetLoaded = false;
	    /*
	     * wake up any thread waiting
	     */
	    notify();
	    System.out.println("World received from server");
	}

	private void processServerCommand(ServerCommand c) {
	    if (c instanceof CloseConnectionCommand) {
		System.out.println("CloseConnectionCommand received");
		disconnect();
	    } else if (c instanceof LoadWorldCommand) {
		System.out.println("LoadWorldCommand received");
		setState(ConnectionState.INITIALISING);
		/*
		 * TODO in the future, queue up moves from the server whilst
		 * the client gets a copy of the World, for now just have a
		 * crude lock
		 */
		synchronized (mutex) {
		    send(world);
		    flush();
		}
		setState(ConnectionState.READY);
	    }
	}

	private synchronized void processNextObject() throws IOException {
	    while ((objectInputStream == null) ||
		    ((mutex == null) && worldNotYetLoaded)) {
		/*
		 * if we are closed, or if we are open and the world is not yet
		 * loaded, then wait until the world has been
		 * loaded. Test mutex to see if we are in a client + therefore
		 * require the world to be loaded.
		 */
		try {
		    wait();
		} catch (InterruptedException e) {
		    /* ignore */
		}
	    }
	    try {
		Object o;
		o = objectInputStream.readObject();
		if (o instanceof ServerCommand) {
		    processServerCommand((ServerCommand) o);
		} else if ((o instanceof Move) && (moveReceiver != null)) {

		    moveReceiver.processMove((Move) o);
		} else {
		    System.out.println("Invalid class sent in stream");
		}
	    } catch (ClassNotFoundException e) {
		System.out.println("Unrecognisable command received by "
			+ "server!");
	    } catch (InvalidClassException e) {
		System.out.println("Invalid class exception received " + e);
	    } catch (StreamCorruptedException e) {
		System.out.println("StreamCorruptedException received " +
			e);
		e.printStackTrace();
		throw new RuntimeException (e);
	    } catch (OptionalDataException e) {
		System.out.println("OptionalDataException received " + e);
	    }
	}

	/**
	 * Entry point for the thread dispatching incoming Moves from the remote
	 * side. Processes any moves waiting.
	 */
	public void run() {
	    try {	    
		while (true) {
		    processNextObject();
		}
	    } catch (IOException e) {
		System.out.println("IOException occurred " + e);
	    }
	}

	public synchronized void open() throws IOException {
	    worldNotYetLoaded = true;
	    objectInputStream = new ObjectInputStream(socket.getInputStream());
	    /*
	     * wake up the thread if it's waiting for the connection to open
	     */
	    notify();
	}

	public synchronized void close() {
	    try {
		objectInputStream.close();
	    } catch (IOException e) {
		System.out.println("Caught an IOException disconnecting " + e);
	    }
	    worldNotYetLoaded = true;
	    objectInputStream = null;
	}
    }

    /**
     * Constructor called by the server.
     * The state of this socket is always WAITING.
     * @throws IOException if the socket couldn't be created.
     * @throws SecurityException if we're not allowed to create the socket.
     */
    public InetConnection(World w, Object mutex) throws IOException {
	world = w;
	this.mutex = mutex;
	System.out.println("Server listening for new connections on port " +
		SERVER_PORT);
	serverSocket = new ServerSocket(SERVER_PORT);
	setState(ConnectionState.WAITING);
    }

    /**
     * called when an incoming connection is attempted
     */
    private InetConnection(Socket acceptedConnection, World w, Object mutex) throws
	IOException {
	this.mutex = mutex;
	world = w;
	socket = acceptedConnection;
	System.out.println("accepted incoming client connection from " +
		socket.getRemoteSocketAddress());
	sender = new Sender();
	dispatcher = new Dispatcher();
	Thread receiveThread = new Thread(dispatcher, "InetConnection");
	receiveThread.start();
	dispatcher.open();
	setState(ConnectionState.WAITING);
    }

    /**
     * called by the client to create a new connection
     */
    public InetConnection(InetAddress serverAddress) {
	this.serverAddress = serverAddress;
	dispatcher = new Dispatcher();
	Thread receiveThread = new Thread(dispatcher, "InetConnection");
	receiveThread.start();
    }

    /**
     * Called by the server to accept client connections.
     * @throws IOException if an IO error occurred.
     * @return The new connection, or null if the socket is closed.
     */
    public ConnectionToServer accept() throws IOException {
	return new InetConnection (serverSocket.accept(), world, mutex);
    }

    public void processMove(Move move) {
	send(move);
	if (move instanceof TimeTickMove) {
	    flush();
	}
    }

    public void undoLastMove() {
	/* TODO implement this */
    }

    public void addMoveReceiver(MoveReceiver m) {
	dispatcher.addMoveReceiver(m);
    }

    public void removeMoveReceiver(MoveReceiver m) {
	dispatcher.removeMoveReceiver(m);
    }

    public void flush() {
	sender.flush();
    }

    /**
     * Called by the client to get a copy of the world.
     */
    public World loadWorldFromServer() throws IOException {
	setState(ConnectionState.INITIALISING);
	send(new LoadWorldCommand());
	sender.flush();
	dispatcher.receiveWorld();
	return world;
    }

    /**
     * Closes the socket. Called by either client or server. Notifies the
     * remote side and then calls disconnect().
     */
    public void close() {
	if (serverSocket != null) {
	    /*
	     * If we are the parent server socket, close it.
	     */
	    try {
		setState(ConnectionState.CLOSED);
		serverSocket.close();
	    } catch (IOException e) {
		System.out.println("Caught an IOException whilst closing the " +
		"server socket " + e);
	    }
	} else {
	    setState(ConnectionState.CLOSED);
	    send(new CloseConnectionCommand());
	    flush();
	    disconnect();
	} 
	System.out.println("Connection to remote peer closed");
    }

    private void send(FreerailsSerializable s) {
	if (sender != null)
	    sender.send(s);
    }

    /**
     * connect to the remote peer
     */
    public void open() throws IOException {
	socket = new Socket(serverAddress, SERVER_PORT);
	sender = new Sender();
	dispatcher.open();
	System.out.println("Successfully opened connection to remote peer");
	setState(ConnectionState.WAITING);
    }
    
    /**
     * Actually closes the connection. Notifies the local side that the
     * connection has been disconnected.
     */
    private void disconnect() {
	try {
	    System.out.println("disconnecting from remote peer!");
	    setState(ConnectionState.CLOSED);
	    dispatcher.close();
	    sender.close();
	    sender = null;
	    socket.close();
	} catch (IOException e) {
	    System.out.println("Caught an IOException disconnecting " + e);
	}
	if (connectionListener != null)
	    connectionListener.connectionClosed(this);
    }

    /**
     * Called by the server when a new world has been loaded.
     */
    public void setWorld(World w) {
	world = w;
    }

    public ConnectionState getConnectionState() {
	return state;
    }

    private void setState(ConnectionState s) {
	state = s;
	if (connectionListener != null)
	    connectionListener.connectionStateChanged(this);
    }
}
