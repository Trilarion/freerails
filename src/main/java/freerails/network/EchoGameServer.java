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

import freerails.world.FreerailsSerializable;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Implementation of GameServer that simply echoes whatever clients send it.
 *
 */
public class EchoGameServer implements GameServer, Runnable {
    private static final Logger logger = Logger.getLogger(EchoGameServer.class
            .getName());

    private final Vector<ConnectionToClient> connections = new Vector<>();

    private final SynchronizedFlag status = new SynchronizedFlag(false);

    private final LinkedList<FreerailsSerializable> messsages2send = new LinkedList<>();

    private EchoGameServer() {
    }

    /**
     * Creates an EchoGameServer, starts it in a new Thread, and waits for its
     * status to change to isOpen before returning.
     * @return 
     */
    public static EchoGameServer startServer() {
        EchoGameServer server = new EchoGameServer();
        Thread t = new Thread(server);
        t.start();

        try {
            /* Wait for the server to start before returning. */
            synchronized (server.status) {
                server.status.wait();
            }

            return server;
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }

    /**
     *
     * @param connection
     */
    public synchronized void addConnection(ConnectionToClient connection) {
        if (null == connection) {
            throw new NullPointerException();
        }

        if (!status.isOpen()) {
            throw new IllegalArgumentException();
        }

        connections.add(connection);
    }

    /**
     *
     * @return
     */
    public synchronized int countOpenConnections() {
        Iterator<ConnectionToClient> it = connections.iterator();

        while (it.hasNext()) {
            ConnectionToClient connection = it.next();

            if (!connection.isOpen()) {
                it.remove();
            }
        }

        return connections.size();
    }

    /**
     *
     */
    public synchronized void stop() {
        status.close();

        for (ConnectionToClient connection1 : connections) {
            AbstractInetConnection connection = (AbstractInetConnection) connection1;

            if (connection.isOpen()) {
                try {
                    connection.setTimeOut(0);
                    connection.disconnect();
                } catch (Exception e) {
                    // Do nothing.
                }
            }
        }
    }

    public void run() {
        status.open();

        while (status.isOpen()) {
            update();

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                // do nothing.
            }
        }
    }

    synchronized void sendMessage(FreerailsSerializable m) {
        /* Send messages. */
        for (ConnectionToClient connection : connections) {
            try {
                connection.writeToClient(m);
                connection.flush();
                if (logger.isDebugEnabled()) {
                    logger.debug("Sent ok: " + m);
                }
            } catch (IOException e) {
                try {
                    if (connection.isOpen()) {
                        connection.disconnect();
                    }
                } catch (IOException e1) {
                    // hope this doesn't happen.
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     *
     */
    public void update() {
        synchronized (this) {
            /* Read messages. */
            for (ConnectionToClient connection : connections) {
                try {
                    FreerailsSerializable[] messages = connection
                            .readFromClient();

                    Collections.addAll(messsages2send, messages);
                } catch (IOException e) {
                    try {
                        if (connection.isOpen()) {
                            connection.disconnect();
                        }
                    } catch (IOException e1) {
                        // 
                        e1.printStackTrace();
                    }
                }
            }

            /* Send messages. */

            for (FreerailsSerializable message : messsages2send) {
                sendMessage(message);
            }
        }
    }
}