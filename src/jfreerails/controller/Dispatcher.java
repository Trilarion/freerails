/*
 * Created on Feb 18, 2004
 */
package jfreerails.controller;

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.logging.Logger;
import jfreerails.controller.ConnectionToServer.ConnectionState;
import jfreerails.move.Move;
import jfreerails.world.top.World;


class Dispatcher implements Runnable {
    private static final Logger logger = Logger.getLogger(Dispatcher.class.getName());
    private final InetConnection connection;

    Dispatcher(InetConnection connection) {
        this.connection = connection;
    }

    private MoveReceiver moveReceiver;
    private ObjectInputStream objectInputStream;
    private boolean worldNotYetLoaded = true;

    public synchronized void addMoveReceiver(MoveReceiver m) {
        moveReceiver = m;
    }

    public synchronized void removeMoveReceiver() {
        moveReceiver = null;
    }

    public synchronized void receiveWorld() throws IOException {
        try {
            while (true) {
                Object o = objectInputStream.readObject();

                if (o instanceof World) {
                    this.connection.world = (World)o;
                    this.connection.setState(ConnectionState.READY);

                    break;
                } else {
                    logger.warning("Received garbage whilst loading world:" +
                        o);
                }
            }
        } catch (ObjectStreamException e) {
            logger.warning("Caught object stream exception whilst loading " +
                "world");
            throw new IOException(e.toString());
        } catch (ClassNotFoundException e) {
            logger.warning("Received unknown class instead of world " + e);
            throw new IOException(e.toString());
        }

        worldNotYetLoaded = false;

        /*
         * wake up any thread waiting
         */
        notifyAll();
        logger.fine("World received from server");
    }

    private void processServerCommand(ServerCommand c) {
        if (c instanceof CloseConnectionCommand) {
            logger.fine("CloseConnectionCommand received");
            this.connection.disconnect();
        } else if (c instanceof LoadWorldCommand) {
            logger.fine("LoadWorldCommand received");
            this.connection.setState(ConnectionState.INITIALISING);

            /*
             * TODO in the future, queue up moves from the server whilst
             * the client gets a copy of the World, for now just have a
             * crude lock
             */
            synchronized (this.connection.mutex) {
                this.connection.send(this.connection.world);
                this.connection.flush();
            }

            this.connection.setState(ConnectionState.READY);
        } else {
            synchronized (this) {
                while (this.connection.connectionListener == null) {
                    /* if the connectionListener has not yet been added then
                     * sleep until one has been added to avoid losing the
                     * event */
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        assert false;
                    }
                }

                this.connection.connectionListener.processServerCommand(this.connection,
                    c);
            }
        }
    }

    private synchronized void processNextObject() throws IOException {
        while ((objectInputStream == null) ||
                ((this.connection.mutex == null) && worldNotYetLoaded)) {
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
                processServerCommand((ServerCommand)o);
            } else if ((o instanceof Move) && (moveReceiver != null)) {
                moveReceiver.processMove((Move)o);
            } else {
                logger.fine("Invalid class sent in stream");
            }
        } catch (ClassNotFoundException e) {
            logger.fine("Unrecognisable command received by " + "server!");
        } catch (InvalidClassException e) {
            logger.fine("Invalid class exception received " + e);
        } catch (StreamCorruptedException e) {
            logger.fine("StreamCorruptedException received " + e);
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (OptionalDataException e) {
            logger.fine("OptionalDataException received " + e);
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
            logger.warning("IOException occurred " + e.toString());

            if (e instanceof EOFException) {
                //remote side probably disconnected abruptly               
                this.connection.disconnect();
            }

            /*
             * If something goes wrong, lets kill the application straight
             * away to avoid hard-to-track-down bugs.
             */
            logger.warning("About to quit..");
            System.exit(1);
        }
    }

    public synchronized void open() throws IOException {
        worldNotYetLoaded = true;
        objectInputStream = new ObjectInputStream(this.connection.socket.getInputStream());

        /*
         * wake up the thread if it's waiting for the connection to open
         */
        notifyAll();
    }

    /** Note, although this method is not synchronized, it is only called from InetConnection.disconnect()
     * which does synchronized on this object, hence, it is effectively synchronized.
     */
    public void close() {
        try {
            if (objectInputStream != null) {
                objectInputStream.close();
            }
        } catch (IOException e) {
            logger.fine("Caught an IOException disconnecting " + e);
        }

        worldNotYetLoaded = true;
        objectInputStream = null;
    }
}