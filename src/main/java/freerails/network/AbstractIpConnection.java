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

// TODO difference between status and readerThreadStatus
/**
 * Code that is shared by the client and server versions of IpConnection.
 */
abstract class AbstractIpConnection {

    private static final Logger logger = Logger.getLogger(AbstractIpConnection.class.getName());
    private final SychronizedQueue inbound = new SychronizedQueue();
    private final IpConnection ipConnection;
    private final SynchronizedFlag readerThreadStatus = new SynchronizedFlag(false);
    private final SynchronizedFlag status = new SynchronizedFlag(true);
    private static final int TIMEOUT = 1000 * 5; // 5 seconds.

    AbstractIpConnection(Socket socket) throws IOException {
        ipConnection = new IpConnection(socket);
        open();
    }

    AbstractIpConnection(String host, int port) throws IOException {
        ipConnection = new IpConnection(host, port);
        open();
    }

    public void disconnect() throws IOException {
        logger.debug(this + "Initiating shutdown..");
        shutdownOutput();

        long waitUntil = System.currentTimeMillis() + TIMEOUT;

        synchronized (readerThreadStatus) {
            while (readerThreadStatus.isSet()) {
                long currentTime = System.currentTimeMillis();

                if (currentTime >= waitUntil) {
                    shutDownInput();
                    throw new IOException("Time-out while trying to disconnect.");
                }

                try {
                    readerThreadStatus.wait(TIMEOUT);
                } catch (InterruptedException e) {}
            }
        }

        logger.debug(this + "Finished shutdown!! --success=" + String.valueOf(status.isSet()));
    }

    public synchronized boolean isOpen() {
        return status.isSet();
    }

    private synchronized void open() throws IOException {
        ipConnection.initialize();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Serializable serializable = ipConnection.receive();

                        synchronized (inbound) {
                            inbound.write(serializable);
                            inbound.notifyAll();
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {}

                logger.debug(this + "Reciprocating shutdown..");
                shutDownInput();
                readerThreadStatus.unset();
            }
        });
        thread.setName(getThreadName());
        thread.start();
        readerThreadStatus.set();
    }

    private synchronized void shutDownInput() {
        try {
            ipConnection.shutdownInput();
            logger.debug(this + "Shut down input.");

            if (status.isSet()) {
                shutdownOutput();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void shutdownOutput() throws IOException {
        if (!status.isSet()) {
            throw new IllegalStateException();
        }

        status.unset();
        ipConnection.shutdownOutput();
        logger.debug(this + "Shut down output.");
    }

    abstract String getThreadName();

    Serializable[] read() throws IOException {
        if (status.isSet()) {
            return inbound.read();
        }
        throw new IOException();
    }

    void send(Serializable object) throws IOException {
        ipConnection.send(object);
    }

    Serializable waitForObject() throws InterruptedException, IOException {
        if (status.isSet()) {
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
        throw new IOException("The connection is closed.");
    }
}