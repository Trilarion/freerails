/*
 * Created on Apr 11, 2004
 */
package freerails.network;

import freerails.world.common.FreerailsSerializable;

import java.io.IOException;

/**
 * A connection between the a client and server in the same JVM.
 *
 * @author Luke
 */
public class LocalConnection implements Connection2Client, Connection2Server {
    public static final String SERVER_IN_SAME_JVM = "server in same JVM";

    private final SychronizedQueue fromServer = new SychronizedQueue();

    private final SychronizedQueue fromClient = new SychronizedQueue();

    private final SynchronizedFlag status = new SynchronizedFlag(true);

    public FreerailsSerializable[] readFromClient() throws IOException {
        if (status.isOpen()) {
            return fromClient.read();
        }
        throw new IOException();
    }

    public FreerailsSerializable waitForObjectFromClient() throws IOException,
            InterruptedException {
        synchronized (fromClient) {
            if (fromClient.size() == 0) {
                fromClient.wait();
            }

            if (status.isOpen()) {
                return fromClient.getFirst();
            }
            throw new IOException();
        }
    }

    public void writeToClient(FreerailsSerializable object) throws IOException {
        if (status.isOpen()) {
            synchronized (fromServer) {
                fromServer.write(object);
                fromServer.notifyAll();
            }
        } else {
            throw new IOException();
        }
    }

    public FreerailsSerializable[] readFromServer() throws IOException {
        if (status.isOpen()) {
            return fromServer.read();
        }
        throw new IOException();
    }

    public FreerailsSerializable waitForObjectFromServer() throws IOException,
            InterruptedException {
        if (status.isOpen()) {
            synchronized (fromServer) {
                if (fromServer.size() == 0) {
                    fromServer.wait();
                }

                return fromServer.getFirst();
            }
        }
        throw new IOException();
    }

    public void writeToServer(FreerailsSerializable object) throws IOException {
        if (status.isOpen()) {
            synchronized (fromClient) {
                fromClient.write(object);
                fromClient.notifyAll();
            }
        } else {
            throw new IOException();
        }
    }

    public boolean isOpen() {
        return status.isOpen();
    }

    public void flush() {
        // No need to do anything.
    }

    public synchronized void disconnect() {
        status.close();
    }

    public String getServerDetails() {
        return SERVER_IN_SAME_JVM;
    }
}