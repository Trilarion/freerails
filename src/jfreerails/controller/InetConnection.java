package jfreerails.controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
public class InetConnection implements ConnectionToServer {
    /**
     * The socket which clients should connect to.
     */
    public static final int SERVER_PORT = 55000;
    private ConnectionState state = ConnectionState.CLOSED;
    private InetAddress serverAddress;
    private ServerSocket serverSocket;
    Socket socket;
    World world;
    ConnectionListener connectionListener;
    private Dispatcher dispatcher;
    private Sender sender;

    public InetAddress getRemoteAddress() {
        return socket.getInetAddress();
    }

    public void addConnectionListener(ConnectionListener l) {
        synchronized (dispatcher) {
            connectionListener = l;

            /*
             * wake up the dispatcher if it is waiting for the
             * connectionListener to be activated
             */
            dispatcher.notifyAll();
        }
    }

    public void removeConnectionListener(ConnectionListener l) {
        connectionListener = null;
    }

    /**
     * Constructor called by the server.
     * The state of this socket is always WAITING.
     * @throws IOException if the socket couldn't be created.
     * @throws SecurityException if we're not allowed to create the socket.
     */
    public InetConnection(ConnectionListener cl, World w, int port) throws
	IOException {
        world = w;
	connectionListener = cl;
        System.out.println("Server listening for new connections on port " +
            port);
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(port));
        setState(ConnectionState.WAITING);

	Thread thread = new Thread(new InetGameServer());
	thread.start();
    }

    /**
     * called when an incoming connection is attempted
     */
    private InetConnection(Socket acceptedConnection, World w)
        throws IOException {
        world = w;
        socket = acceptedConnection;
        System.out.println("accepted incoming client connection from " +
            socket.getRemoteSocketAddress());
        sender = new Sender(this.socket.getOutputStream());
        dispatcher = new Dispatcher(this);
    }

    /**
     * called by the client to create a new connection
     */
    public InetConnection(InetAddress serverAddress) {
        this.serverAddress = serverAddress;
	world = null;
        dispatcher = new Dispatcher(this);

        Thread receiveThread = new Thread(dispatcher, "InetConnection");
        receiveThread.start();
    }

    /**
     * Called to accept client connections.
     * @throws IOException if an IO error occurred.
     * @return The new connection, or null if the socket is closed.
     */
    private InetConnection accept() throws IOException {
        return new InetConnection(serverSocket.accept(), world);
    }

    public void sendCommand(ServerCommand s) {
        send(s);
        flush();
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

    public void addMoveReceiver(SourcedMoveReceiver m) {
        dispatcher.addMoveReceiver(m);
    }

    public void removeMoveReceiver(SourcedMoveReceiver m) {
        dispatcher.removeMoveReceiver(m);
    }

    public void flush() {
        if (sender != null) {
            sender.flush();
        }
    }

    /**
     * Called by the client to get a copy of the world.
     */
    public World loadWorldFromServer() throws IOException {
        setState(ConnectionState.INITIALISING);
        sendCommand(new LoadWorldCommand());
        sender.flush();
        return dispatcher.receiveWorld();
    }

    /**
     * Closes the socket. Called by either client or server. Notifies the
     * remote side and then calls disconnect().
     */
    public synchronized void close() {
        if (state == ConnectionState.CLOSED) {
            return;
        }

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
            sendCommand(new CloseConnectionCommand());
            flush();
            disconnect();
        }

        System.out.println("Connection to remote peer closed");
    }

    void send(FreerailsSerializable s) {
	assert sender != null;
        if (sender != null) {
            try {
                sender.send(s);
            } catch (SocketException e) {
                e.printStackTrace();

                /*
                 * call disconnect instead of close, since we can't send a
                 * CloseConnectionCommand
                 */
                disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * connect to the remote peer
     */
    public void open() throws IOException {
        socket = new Socket(serverAddress, SERVER_PORT);
        sender = new Sender(this.socket.getOutputStream());
        dispatcher.open();
        System.out.println("Successfully opened connection to remote peer");
        setState(ConnectionState.WAITING);
    }

    /**
     * Actually closes the connection. Notifies the local side that the
     * connection has been disconnected.
     */
    void disconnect() {
        /* To allow this method to be called without risk of deadlock from synchronized methods in the dispatcher,
        * we must acquire the lock on the dispatcher before locking on this object.       
        */
        synchronized (dispatcher) {
            synchronized (this) {
                try {
                    System.out.println("disconnecting from remote peer!");
                    setState(ConnectionState.CLOSED);

                    if (dispatcher != null) {
                        dispatcher.close();
                    }

                    if (sender != null) {
                        sender.close();
                    }

                    sender = null;

                    if (socket != null) {
                        socket.close();
                    }

                    socket = null;
                } catch (IOException e) {
                    System.out.println("Caught an IOException disconnecting " +
                        e);
                }

                if (connectionListener != null) {
                    connectionListener.connectionClosed(this);
                }
            }
        }
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

    void setState(ConnectionState s) {
        state = s;

        if (connectionListener != null) {
            connectionListener.connectionStateChanged(this);
        }
    }
    
    /**
     * Sits in a loop and accepts incoming connections over the network
     */
    private class InetGameServer implements Runnable {
        public void run() {
            Thread.currentThread().setName("InetGameServer");

            try {
                while (true) {
                    try {
                        InetConnection c = InetConnection.this.accept();
                        connectionListener.connectionOpened(c);

			/*
			 * Don't start the receive thread until the server has
			 * been notified that the connection is being opened
			 */
			Thread receiveThread = new Thread(c.dispatcher,
				"InetConnection");
			receiveThread.start();
			c.dispatcher.open();
			c.setState(ConnectionState.WAITING);
                    } catch (IOException e) {
                        if (e instanceof SocketException) {
                            throw (SocketException)e;
                        } else {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (SocketException e) {
                /* The socket was probably closed, exit */
            }
        }
    }
}
