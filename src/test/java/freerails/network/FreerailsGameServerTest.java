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

import freerails.io.scenario.ClasspathScenarioSource;
import freerails.model.scenario.ScenarioData;
import freerails.model.world.World;
import freerails.move.MoveReceiver;
import freerails.server.FreerailsGameServer;
import freerails.server.ScenarioSource;
import freerails.server.ServerGameModel;
import freerails.server.TestSaveGamesManager;
import freerails.util.network.Connection;
import junit.framework.TestCase;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;
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

    /**
     * Ensures server updates cannot send game moves before a new game's world is installed.
     *
     * @throws Exception if the concurrency test fails.
     */
    public void testNewGameBlocksServerUpdatesUntilWorldIsInstalled() throws Exception {
        CountDownLatch scenarioLoadStarted = new CountDownLatch(1);
        CountDownLatch allowScenarioLoad = new CountDownLatch(1);
        CountDownLatch updateAttempted = new CountDownLatch(1);
        ClasspathScenarioSource scenarioSource = new ClasspathScenarioSource();
        ScenarioSource blockingScenarioSource = new ScenarioSource() {
            /** Returns the scenarios available from the classpath source. */
            @Override
            public String[] getAvailableScenarioNames() {
                return scenarioSource.getAvailableScenarioNames();
            }

            /** Waits until the test permits scenario loading to complete. */
            @Override
            public ScenarioData loadScenario(String scenarioName) throws IOException {
                scenarioLoadStarted.countDown();
                try {
                    if (!allowScenarioLoad.await(2, TimeUnit.SECONDS)) {
                        throw new IOException("Timed out waiting to release scenario loading.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while waiting to load the scenario.", e);
                }
                return scenarioSource.loadScenario(scenarioName);
            }
        };
        TrackingServerGameModel gameModel = new TrackingServerGameModel();
        FreerailsGameServer concurrentServer =
                new FreerailsGameServer(blockingScenarioSource, new TestSaveGamesManager());
        concurrentServer.setServerGameModel(gameModel);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> newGame = executor.submit(() -> concurrentServer.newGame(
                    scenarioSource.getAvailableScenarioNames()[0]));
            assertTrue("New-game setup should start loading the scenario.",
                    scenarioLoadStarted.await(2, TimeUnit.SECONDS));

            Future<?> update = executor.submit(() -> {
                updateAttempted.countDown();
                concurrentServer.update();
            });
            assertTrue("The server update should attempt to run.",
                    updateAttempted.await(2, TimeUnit.SECONDS));
            assertEquals("Server updates must wait for the initial world broadcast.",
                    0, gameModel.updateCount.get());

            allowScenarioLoad.countDown();
            newGame.get(5, TimeUnit.SECONDS);
            update.get(5, TimeUnit.SECONDS);

            assertNotNull("New-game setup should install its world.", gameModel.getWorld());
            assertEquals("The waiting server update should run after game setup.", 1,
                    gameModel.updateCount.get());
        } finally {
            allowScenarioLoad.countDown();
            executor.shutdownNow();
        }
    }

    /** Tracks whether an update overlaps server game creation. */
    private static final class TrackingServerGameModel implements ServerGameModel {

        private World world;
        private String[] passwords = new String[0];
        private final AtomicInteger updateCount = new AtomicInteger();

        /** Records server model updates. */
        @Override
        public void update() {
            updateCount.incrementAndGet();
        }

        /** Replaces the tracked game world and credentials. */
        @Override
        public void setWorld(World world, String[] passwords) {
            this.world = world;
            this.passwords = passwords.clone();
        }

        /** Returns the installed world. */
        @Override
        public World getWorld() {
            return world;
        }

        /** Returns the tracked player credentials. */
        @Override
        public String[] getPasswords() {
            return passwords.clone();
        }

        /** Accepts the receiver without scheduling game updates. */
        @Override
        public void initialize(MoveReceiver moveReceiver) {}
    }
}