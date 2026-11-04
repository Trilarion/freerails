/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.server.FreerailsGameServer;
import freerails.io.scenario.ClasspathScenarioSource;
import freerails.server.TestSaveGamesManager;
import freerails.util.network.Connection;
import freerails.util.network.ServerSocketAcceptor;
import junit.framework.TestCase;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Test cases that use FreerailsGameServer <b>and</b> connect over the Internet
 * should extend this class.
 */
public abstract class AbstractFreerailsServerTestCase extends TestCase {

    private static final int PORT = 13856;
    public FreerailsGameServer server;
    private ServerSocketAcceptor acceptor;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected synchronized void setUp() throws Exception {
        super.setUp();

        FreerailsGameServer result;
        FreerailsGameServer server1 = new FreerailsGameServer(new ClasspathScenarioSource(), new TestSaveGamesManager());
        Thread t = new Thread(server1);
        t.start();

        try {
            // Wait for the server to start before returning.
            CountDownLatch status = server1.getStatus();
            status.await();

            result = server1;
        } catch (InterruptedException e) {
            throw new IllegalStateException();
        }
        server = result;

        InetSocketAddress address = new InetSocketAddress(InetAddress.getLoopbackAddress(), PORT);

        BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
        try {
            acceptor = new ServerSocketAcceptor(address, sockets);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Thread bridgeThread = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = sockets.take();
                    Connection connection = Connection.make(socket);
                    server.addConnection(connection);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "Server Connection Acceptor");
        bridgeThread.start();
    }

    /**
     * Releases resources after each test.
     * @throws Exception if the tear down operation fails.
     */
    @Override
    protected synchronized void tearDown() throws Exception {
        super.tearDown();
        acceptor.close();
    }

    /**
     * Returns port.
     * @return the result of get port.
     */
    public int getPort() {
        return PORT;
    }

    /**
     * Returns ip address.
     * @return the result of get ip address.
     */
    public String getIpAddress() {
        return "127.0.0.1";
    }
}