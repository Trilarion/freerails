/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * Created on Feb 18, 2004
 */
package org.railz.controller;

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import org.railz.controller.ConnectionToServer.ConnectionState;
import org.railz.move.*;
import org.railz.world.top.World;

/**
 * It may be necessary to call notifyAll() on this object if the associated
 * connection does not have a connectionListener attached when the thread is
 * started.
 */
class Dispatcher implements Runnable {
    private final InetConnection connection;

    Dispatcher(InetConnection connection) {
        this.connection = connection;
    }

    private SourcedMoveReceiver moveReceiver;
    private ObjectInputStream objectInputStream;

    /**
     * wait() on this object when we need the client to wait for a response
     */
    private Object clientSemaphore = new Integer(1);

    public void addMoveReceiver(SourcedMoveReceiver m) {
	moveReceiver = m;
    }

    public void removeMoveReceiver(SourcedMoveReceiver m) {
	moveReceiver = null;
    }

    public World receiveWorld() throws IOException {
	World w;
	synchronized (clientSemaphore) {
	    while (connection.world == null) {
		try {
		    clientSemaphore.wait();
		} catch (InterruptedException e) {
		    // do nothing
		}
	    }
	}

        System.out.println("World received from server");
	return connection.world;
    }

    private void processServerCommand(ServerCommand c) {
        if (c instanceof CloseConnectionCommand) {
            System.out.println("CloseConnectionCommand received");
            //Can cause deadlock!
            connection.disconnect();
        } else if (c instanceof LoadWorldCommand) {
            System.out.println("LoadWorldCommand received");
            connection.setState(ConnectionState.INITIALISING);

	    // purge the ObjectOutputStream of any cached objects
	    connection.reset();
            /*
             * TODO in the future, queue up moves from the server whilst
             * the client gets a copy of the World, for now just have a
             * crude lock
             */
	    connection.send(connection.world);
	    connection.flush();

            connection.setState(ConnectionState.READY);
        } else {
	    ConnectionListener cl = connection.connectionListener;

            cl.processServerCommand(this.connection, c);
        }
    }

    private void processNextObject() throws IOException {
	Object o = null;
	synchronized (this) {
	    while ((objectInputStream == null) ||
		    connection.connectionListener == null) {
		/*
		 * if we are closed then wait until we are opened.
		 */
		try {
		    wait();
		} catch (InterruptedException e) {
		    /* ignore */
		}
	    }

	    try {
		o = objectInputStream.readObject();

	    } catch (ClassNotFoundException e) {
		System.out.println("Unrecognisable command received by " +
			"server!");
		return;
	    } catch (InvalidClassException e) {
		System.out.println("Invalid class exception received " + e);
		return;
	    } catch (StreamCorruptedException e) {
		System.out.println("StreamCorruptedException received " + e);
		e.printStackTrace();
		throw new RuntimeException(e);
	    } catch (OptionalDataException e) {
		System.out.println("OptionalDataException received " + e);
		return;
	    }
	}

	if (!(o instanceof TimeTickMove)) {
	    System.out.println ("processing received object " + o);
	}
	if (o instanceof ServerCommand) {
	    processServerCommand((ServerCommand)o);
	} else if (o instanceof Move) {
	    if (connection.world == null) {
		System.out.println("Discarding move");
	    } else {
		SourcedMoveReceiver mr = moveReceiver;
		assert mr != null;
		mr.processMove((Move)o, this.connection);
	    }
	} else if ((o instanceof World) && connection.world == null) {
	    connection.world = (World)o;
	    connection.setState(ConnectionState.READY);
	    synchronized (clientSemaphore) {
		clientSemaphore.notify();
	    }
	} else {
	    System.out.println("Invalid class sent in stream");
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
        objectInputStream = new ObjectInputStream(connection.socket.getInputStream());

        /*
         * wake up the thread if it's waiting for the connection to open
         */
        notifyAll();
    }

    public synchronized void close() {
        try {
            if (objectInputStream != null) {
                objectInputStream.close();
            }
        } catch (IOException e) {
            System.out.println("Caught an IOException disconnecting " + e);
        }

        objectInputStream = null;
    }

    synchronized void reset() {
	try {
	    objectInputStream.reset();
	} catch (IOException e) {
	    System.err.println ("Caught an IOException resetting the " +
		    "ObjectInputStream:" + e);
	}
    }
}
