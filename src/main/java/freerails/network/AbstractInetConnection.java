/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.network;

import freerails.util.SychronizedQueue;
import freerails.util.SynchronizedFlag;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

/**
 * Has the code that is shared by the client and server versions of
 * InetConnection.
 */
@SuppressWarnings("unused")
abstract class AbstractInetConnection implements Runnable {

    private static final Logger logger = Logger.getLogger(AbstractInetConnection.class.getName());
    private final SychronizedQueue inbound = new SychronizedQueue();
    private final Connection inetConnection;
    private final SynchronizedFlag readerThreadStatus = new SynchronizedFlag(false);
    private final SynchronizedFlag status = new SynchronizedFlag(true);
    private int timeout = 1000 * 5; // 5 seconds.

    public AbstractInetConnection(Socket s) throws IOException {
        inetConnection = new Connection(s);
        open();
    }

    public AbstractInetConnection(String ip, int port) throws IOException {
        inetConnection = new Connection(ip, port);
        open();
    }

    public void disconnect() throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug(this + "Initiating shutdown..");
        }
        shutdownOutput();

        long waitUntil = System.currentTimeMillis() + timeout;

        synchronized (readerThreadStatus) {
            while (readerThreadStatus.isOpen()) {
                long currentTime = System.currentTimeMillis();

                if (currentTime >= waitUntil) {
                    shutDownInput();
                    throw new IOException("Time-out while trying to disconnect.");
                }

                try {
                    readerThreadStatus.wait(timeout);
                } catch (InterruptedException e) {
                    // do nothing.
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(this + "Finished shutdown!! --status=" + String.valueOf(status.isOpen()));
        }
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
                Serializable fs = inetConnection.receive();

                synchronized (inbound) {
                    inbound.write(fs);
                    inbound.notifyAll();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
        }

        if (logger.isDebugEnabled()) {
            logger.debug(this + "Reciprocating shutdown..");
        }
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
            if (logger.isDebugEnabled()) {
                logger.debug(this + "Shut down input.");
            }

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
        if (logger.isDebugEnabled()) {
            logger.debug(this + "Shut down output.");
        }
    }

    abstract String getThreadName();

    Serializable[] read() throws IOException {
        if (status.isOpen()) {
            return inbound.read();
        }
        throw new IOException();
    }

    void send(Serializable object) throws IOException {
        inetConnection.send(object);
    }

    void setTimeOut(int i) {
        timeout = i;
    }

    Serializable waitForObject() throws InterruptedException, IOException {
        if (status.isOpen()) {
            synchronized (inbound) {
                if (inbound.size() > 0) {
                    return inbound.getFirst();
                }
                inbound.wait();

                if (inbound.size() > 0) {
                    return inbound.getFirst();
                }
                throw new IllegalStateException();
            }
        }
        throw new IOException("The connection is close.");
    }
}