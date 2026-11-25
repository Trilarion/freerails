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
import freerails.model.economy.Money;
import freerails.model.economy.transaction.Transaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.player.Player;
import freerails.model.scenario.ScenarioData;
import freerails.model.world.World;
import freerails.move.AddPlayerMove;
import freerails.move.AddTransactionMove;
import freerails.move.MoveReceiver;
import freerails.move.Status;
import freerails.move.ChangeGameSpeedMove;
import freerails.move.TryMoveStatus;
import freerails.move.generator.AddStationMoveGenerator;
import freerails.move.generator.IssueBondMoveGenerator;
import freerails.model.ModelConstants;
import freerails.model.simulation.Speed;
import freerails.util.IntPoint2D;
import freerails.server.FreerailsGameServer;
import freerails.server.ScenarioSource;
import freerails.server.ServerGameModel;
import freerails.server.TestSaveGamesManager;
import freerails.util.network.Connection;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
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
    private TestSaveGamesManager saveGamesManager;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        saveGamesManager = new TestSaveGamesManager();
        server = new FreerailsGameServer(new ClasspathScenarioSource(), saveGamesManager);
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

        // Test 4: When new log-ons are not allowed because a game has started.
        server.setServerGameModel(new TrackingServerGameModel());
        server.newGame(new ClasspathScenarioSource().getAvailableScenarioNames()[0]);

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
     * Tests that loading a game restores the saved players and their passwords.
     * @throws Exception if loading the game fails.
     */
    public void testLoadGameRestoresSavedCredentials() throws Exception {
        LogOnCredentials first = new LogOnCredentials("Name", "password");
        LogOnCredentials second = new LogOnCredentials("Name2", "password2");
        server.setServerGameModel(new TrackingServerGameModel());
        assertTrue(server.logon(first).success());
        assertTrue(server.logon(second).success());
        server.newGame(new ClasspathScenarioSource().getAvailableScenarioNames()[0]);
        server.saveGame("game.sav");

        FreerailsGameServer otherServer = new FreerailsGameServer(new ClasspathScenarioSource(), saveGamesManager);
        TrackingServerGameModel otherModel = new TrackingServerGameModel();
        otherServer.setServerGameModel(otherModel);
        assertTrue(otherServer.logon(new LogOnCredentials("Other", "other")).success());
        otherServer.loadGame("game.sav");

        assertEquals(2, otherModel.getWorld().getPlayers().size());
        assertEquals(List.of("Name", "Name2"), Arrays.asList(otherServer.getPlayerNames()));
        LogOnResponse response = otherServer.logon(second);
        assertTrue(response.success());
        assertEquals(1, response.id());
        assertFalse("Wrong password.", otherServer.logon(new LogOnCredentials("Name", "wrong")).success());
        assertFalse("New players are not allowed.", otherServer.logon(new LogOnCredentials("New", "new")).success());
    }

    /**
     * Tests that loading a game keeps logged-on participants under their saved
     * player id and logs off everyone else.
     * @throws Exception if loading the game fails.
     */
    public void testLoadGameRemapsLoggedOnPlayers() throws Exception {
        LogOnCredentials first = new LogOnCredentials("Name", "password");
        LogOnCredentials second = new LogOnCredentials("Name2", "password2");
        server.setServerGameModel(new TrackingServerGameModel());
        assertTrue(server.logon(first).success());
        assertTrue(server.logon(second).success());
        server.newGame(new ClasspathScenarioSource().getAvailableScenarioNames()[0]);
        server.saveGame("game.sav");

        FreerailsGameServer otherServer = new FreerailsGameServer(new ClasspathScenarioSource(), saveGamesManager);
        otherServer.setServerGameModel(new TrackingServerGameModel());
        assertEquals(0, otherServer.logon(second).id());
        assertEquals(1, otherServer.logon(new LogOnCredentials("Other", "other")).id());
        otherServer.loadGame("game.sav");

        assertEquals(List.of("Name2"), Arrays.asList(otherServer.getConnectedPlayerNames()));
        assertFalse("Name2 is still logged on.", otherServer.logon(second).success());
        LogOnResponse response = otherServer.logon(first);
        assertTrue(response.success());
        assertEquals(0, response.id());

        otherServer.logoff(1);
        assertEquals(List.of("Name"), Arrays.asList(otherServer.getConnectedPlayerNames()));
        assertEquals("Name2 was logged off under its new id.", 1, otherServer.logon(second).id());
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
     * Tests that the server applies a client's move as the player of the sending
     * connection, whatever player the move claims, and rejects moves a client may
     * not send.
     * @throws Exception if the connection test fails.
     */
    public void testClientMovesActForTheSendingPlayer() throws Exception {
        try (ServerSocket listener = new ServerSocket(0, 0, InetAddress.getLoopbackAddress())) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Connection[] clients = new Connection[2];
            Connection[] serverSides = new Connection[2];
            try {
                for (int i = 0; i < 2; i++) {
                    Future<Connection> serverSide = executor.submit(() -> Connection.make(listener.accept()));
                    clients[i] = Connection.make(new InetSocketAddress(
                            InetAddress.getLoopbackAddress(), listener.getLocalPort()));
                    serverSides[i] = serverSide.get(2, TimeUnit.SECONDS);
                    clients[i].sendObject(new LogOnCredentials("Name" + i, "password" + i));
                    server.addConnection(serverSides[i]);
                }
                TrackingServerGameModel gameModel = new TrackingServerGameModel();
                server.setServerGameModel(gameModel);
                server.newGame(new ClasspathScenarioSource().getAvailableScenarioNames()[0]);
                World world = gameModel.getWorld();
                Player sender = world.getPlayer(world.getPlayerIndex(0));
                Player other = world.getPlayer(world.getPlayerIndex(1));
                Money senderBalance = world.getCurrentBalance(sender);
                Money otherBalance = world.getCurrentBalance(other);

                // Player 0 claims to be player 1 when issuing a bond.
                clients[0].sendObject(new IssueBondMoveGenerator(other));
                assertTrue(awaitStatus(clients[0]).isSuccess());

                assertEquals(Money.add(senderBalance, ModelConstants.BOND_VALUE_ISSUE), world.getCurrentBalance(sender));
                assertEquals(otherBalance, world.getCurrentBalance(other));

                // Moves only the server may make are rejected, including hand-made transactions.
                int playerCount = world.getPlayers().size();
                clients[0].sendObject(new AddPlayerMove(new Player(playerCount, "Intruder")));
                assertFalse(awaitStatus(clients[0]).isSuccess());
                assertEquals(playerCount, world.getPlayers().size());
                Transaction gift = new Transaction(
                        TransactionCategory.MISC_INCOME, new Money(1000), world.getClock().getCurrentTime());
                clients[0].sendObject(new AddTransactionMove(sender, gift));
                assertFalse(awaitStatus(clients[0]).isSuccess());
                assertEquals(Money.add(senderBalance, ModelConstants.BOND_VALUE_ISSUE), world.getCurrentBalance(sender));

                // A generator that fails on the server is refused instead of ending the server update.
                clients[0].sendObject(new AddStationMoveGenerator(new IntPoint2D(0, 0), 0, sender));
                assertFalse(awaitStatus(clients[0]).isSuccess());

                // Only the host may change the game speed.
                Speed speed = world.getSpeed();
                Speed otherSpeed = speed == Speed.FAST ? Speed.SLOW : Speed.FAST;
                clients[1].sendObject(new ChangeGameSpeedMove(sender, otherSpeed));
                assertFalse(awaitStatus(clients[1]).isSuccess());
                assertEquals(speed, world.getSpeed());
                clients[0].sendObject(new ChangeGameSpeedMove(sender, otherSpeed));
                assertTrue(awaitStatus(clients[0]).isSuccess());
                assertEquals(otherSpeed, world.getSpeed());
            } finally {
                for (int i = 0; i < 2; i++) {
                    if (clients[i] != null) clients[i].close();
                    if (serverSides[i] != null) serverSides[i].close();
                }
                executor.shutdownNow();
            }
        }
    }

    /**
     * Updates the server until the client receives a move status.
     *
     * @param client the client connection
     * @return the received status
     * @throws InterruptedException if interrupted while waiting
     */
    private Status awaitStatus(Connection client) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5_000;
        while (System.currentTimeMillis() < deadline) {
            server.update();
            for (Serializable message : client.getReceivedObjects()) {
                if (message instanceof Status status) {
                    return status;
                }
                if (message instanceof TryMoveStatus tryMoveStatus) {
                    return tryMoveStatus.status;
                }
            }
            Thread.sleep(10);
        }
        throw new AssertionError("No move status received.");
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
        private final AtomicInteger updateCount = new AtomicInteger();

        /** Records server model updates. */
        @Override
        public void update(MoveReceiver moveReceiver) {
            updateCount.incrementAndGet();
        }

        /** Replaces the tracked game world. */
        @Override
        public void setWorld(World world) {
            this.world = world;
        }

        /** Returns the installed world. */
        @Override
        public World getWorld() {
            return world;
        }
    }
}