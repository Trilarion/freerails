/*
 * Created on Apr 13, 2004
 */
package jfreerails.network;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;
import jfreerails.world.common.FreerailsSerializable;


/**
 * This class has the code that is shared by the client and server versions of InetConnection.
 *
 * @author Luke
 */
abstract class AbstractInetConnection implements Runnable {
    private static final Logger logger = Logger.getLogger(AbstractInetConnection.class.getName());
    private final NewSychronizedQueue inbound = new NewSychronizedQueue();
    private final NewInetConnection inetConnection;
    private final SynchronizedFlag readerThreadStatus = new SynchronizedFlag(false);
    private final SynchronizedFlag status = new SynchronizedFlag(true);
    private int timeout = 1000 * 5; //5 seconds.

    public AbstractInetConnection(Socket s) throws IOException {
        inetConnection = new NewInetConnection(s);
        open();
    }

    public AbstractInetConnection(String ip, int port)
        throws IOException {
        inetConnection = new NewInetConnection(ip, port);
        open();
    }

    public void disconnect() throws IOException {
        logger.fine(this + "Initiating shutdown..");
        shutdownOutput();

        long waitUntil = System.currentTimeMillis() + timeout;

        synchronized (readerThreadStatus) {
            while (readerThreadStatus.isOpen()) {
                long currentTime = System.currentTimeMillis();

                if (currentTime >= waitUntil) {
                    shutDownInput();
                    throw new IOException(
                        "Time-out while trying to disconnect.");
                }

                try {
                    readerThreadStatus.wait(timeout);
                } catch (InterruptedException e) {
                    //do nothing.
                }
            }
        }

        logger.fine(this + "Finished shutdown!! --status=" +
            String.valueOf(status.isOpen()));
    }

    public void flush() throws IOException {
        inetConnection.flush();
    }

    public synchronized boolean isOpen() {
        return status.isOpen();
    }

    public void run() {
        try {
            while (true) {
                FreerailsSerializable fs = inetConnection.receive();

                synchronized (inbound) {
                    inbound.write(fs);
                    inbound.notifyAll();
                }
            }
        } catch (EOFException e) {
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        logger.fine(this + "Recipricating shutdown..");
        shutDownInput();
        readerThreadStatus.close();
    }

    private synchronized void open() throws IOException {
        Thread t = new Thread(this);
        t.setName(getThreadName());
        inetConnection.open();
        t.start();
        readerThreadStatus.open();
    }

    private synchronized void shutDownInput() {
        try {
            inetConnection.shutdownInput();
            logger.fine(this + "Shut down input.");

            if (status.isOpen()) {
                shutdownOutput();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private synchronized void shutdownOutput() throws IOException {
        if (!status.isOpen()) {
            throw new IllegalStateException();
        }

        status.close();
        inetConnection.shutdownOutput();
        logger.fine(this + "Shut down output.");
    }

    abstract String getThreadName();

    FreerailsSerializable[] read() throws IOException {
        if (status.isOpen()) {
            return inbound.read();
        } else {
            throw new IOException();
        }
    }

    void send(FreerailsSerializable object) throws IOException {
        inetConnection.send(object);
    }

    void setTimeOut(int i) {
        timeout = i;
    }

    FreerailsSerializable waitForObject()
        throws InterruptedException, IOException {
        if (status.isOpen()) {
            synchronized (inbound) {
                if (inbound.size() > 0) {
                    return inbound.getFirst();
                } else {
                    inbound.wait();

                    if (inbound.size() > 0) {
                        return inbound.getFirst();
                    } else {
                        throw new IllegalStateException();
                    }
                }
            }
        } else {
            throw new IOException("The connection is close.");
        }
    }
}