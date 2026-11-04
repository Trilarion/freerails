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
import junit.framework.TestCase;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test for FreerailsGameServer - tests logging on.
 */
public class FreerailsGameServerTest extends TestCase {

    private FreerailsGameServer server;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        server = new FreerailsGameServer(new ClasspathScenarioSource(), new TestSaveGamesManager());
    }

    /**
     * Tests logon behavior.
     */
    public void testLogon() {
        LogOnResponse response;

        // Test 1
        LogOnCredentials request1 = new LogOnCredentials("Name", "password");
        response = server.logon(request1);
        assertTrue("Simple case, should go through.", response.success());
        assertEquals("1st logon is player 0", 0, response.id());

        // Test 2
        LogOnCredentials request2 = new LogOnCredentials("Name2", "password2");
        response = server.logon(request2);
        assertTrue("Simple case, should go through.", response.success());
        assertEquals("2nd logon is player 1", 1, response.id());

        // Test 3: When player is already logged on.
        LogOnCredentials request3 = new LogOnCredentials("Name", "password");
        response = server.logon(request3);
        assertFalse("Player is already logged on.", response.success());

        // Test 4: When new log-ons are not allowed.
        server.setNewPlayersAllowed(false);

        LogOnCredentials request4 = new LogOnCredentials("Name4", "password4");
        response = server.logon(request4);
        assertFalse("New logons are not allowed.", response.success());

        // Test 5: When the player has logged off, then tries to log on.
        server.logoff(0);

        response = server.logon(request1);
        assertTrue("Player 0 has logged off, so should succeed.", response
                .success());
        assertEquals("Should keep the same player id", 0, response
                .id());

        /*
         * Test 6: When the player has logged off, then tries to log on with
         * wrong password.
         */
        server.logoff(0);

        LogOnCredentials request6 = new LogOnCredentials("Name", "batman");
        response = server.logon(request6);
        assertFalse("Player 0 has logged off, but the password is wrong.", response.success());
    }

    /**
     * Tests that a disconnected client is removed and not processed repeatedly.
     * @throws Exception if the connection test fails.
     */
    public void testDisconnectedClientIsRemovedAfterUpdate() throws Exception {
        try (ServerSocket listener = new ServerSocket(0, 0, InetAddress.getLoopbackAddress())) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Connection> serverConnectionFuture = executor.submit(() ->
                    Connection.make(listener.accept()));
            Connection clientConnection = Connection.make(new InetSocketAddress(
                    InetAddress.getLoopbackAddress(), listener.getLocalPort()));
            Connection serverConnection = serverConnectionFuture.get(2, TimeUnit.SECONDS);
            AtomicInteger connectionChangeEvents = new AtomicInteger();
            server.addPropertyChangeListener(event -> connectionChangeEvents.incrementAndGet());

            try {
                clientConnection.sendObject(new LogOnCredentials("Name", "password"));
                server.addConnection(serverConnection);
                connectionChangeEvents.set(0);

                clientConnection.close();
                long deadline = System.currentTimeMillis() + 2_000;
                while (serverConnection.isOpen() && System.currentTimeMillis() < deadline) {
                    Thread.sleep(10);
                }
                assertFalse("Server connection should close after the client disconnects.",
                        serverConnection.isOpen());

                server.update();
                server.update();

                assertEquals("Disconnected clients should only be removed once.",
                        1, connectionChangeEvents.get());
            } finally {
                clientConnection.close();
                serverConnection.close();
                executor.shutdownNow();
            }
        }

    }
}