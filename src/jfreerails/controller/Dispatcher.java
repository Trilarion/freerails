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
import jfreerails.controller.ConnectionToServer.ConnectionState;
import jfreerails.move.Move;
import jfreerails.world.top.World;


class Dispatcher implements Runnable {
    private final InetConnection connection;

    Dispatcher(InetConnection connection) {
        this.connection = connection;
    }

    private SourcedMoveReceiver moveReceiver;
    private ObjectInputStream objectInputStream;
    private boolean worldNotYetLoaded = true;

    public synchronized void addMoveReceiver(SourcedMoveReceiver m) {
        moveReceiver = m;
    }

    public synchronized void removeMoveReceiver(SourcedMoveReceiver m) {
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
                    System.out.println("Received garbage whilst loading world:" +
                        o);
                }
            }
        } catch (ObjectStreamException e) {
            System.out.println("Caught object stream exception whilst loading " +
                "world");
            throw new IOException(e.toString());
        } catch (ClassNotFoundException e) {
            System.out.println("Received unknown class instead of world " + e);
            throw new IOException(e.toString());
        }

        worldNotYetLoaded = false;

        /*
         * wake up any thread waiting
         */
        notifyAll();
        System.out.println("World received from server");
    }

    private void processServerCommand(ServerCommand c) {
        if (c instanceof CloseConnectionCommand) {
            System.out.println("CloseConnectionCommand received");
            //Can cause deadlock!
            this.connection.disconnect();
        } else if (c instanceof LoadWorldCommand) {
            System.out.println("LoadWorldCommand received");
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
                moveReceiver.processMove((Move)o, this.connection);
            } else {
                System.out.println("Invalid class sent in stream");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Unrecognisable command received by " +
                "server!");
        } catch (InvalidClassException e) {
            System.out.println("Invalid class exception received " + e);
        } catch (StreamCorruptedException e) {
            System.out.println("StreamCorruptedException received " + e);
            e.printStackTrace();
            throw new RuntimeException(e);
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

            if (e instanceof EOFException) {
                //remote side probably disconnected abruptly               
                this.connection.disconnect();
            }
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
            System.out.println("Caught an IOException disconnecting " + e);
        }

        worldNotYetLoaded = true;
        objectInputStream = null;
    }
}