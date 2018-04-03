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

package freerails.client;

import freerails.move.*;
import freerails.move.generator.TimeTickMoveGenerator;
import freerails.network.*;
import freerails.server.FreerailsGameServer;
import freerails.network.command.*;
import freerails.savegames.TestSaveGamesManager;
import freerails.server.TestServerGameModel;
import freerails.util.ImmutableList;
import freerails.model.world.World;
import freerails.model.finances.Money;
import freerails.model.finances.MoneyTransaction;
import freerails.model.finances.Transaction;
import freerails.model.finances.TransactionCategory;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.player.Player;
import junit.framework.TestCase;

import java.io.Serializable;

/**
 * This test uses clients connected to a local server. This means anything sent
 * to the server arrives instantly, which makes writing the test easier.
 *
 * @see FreerailsClientTest
 */
public class FreerailsClientWithLocalServerTest extends TestCase {

    private FreerailsGameServer server;
    private TestSaveGamesManager savedGamesManager;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        savedGamesManager = new TestSaveGamesManager();
        server = new FreerailsGameServer(savedGamesManager);
        server.setServerGameModel(new TestServerGameModel());
    }

    /**
     * Copy & pasted from FreerailsClientTest, then edited.
     */
    public void testLogon() {
        try {
            // Test 1 : connecting a client.
            assertEquals("No client connected yet.", 0, server.getNumberOpenConnections());

            FreerailsClient client = new FreerailsClient();
            LogOnResponse response = client.connect(server, "name", "password");
            assertTrue(response.isSuccess());
            assertEquals(1, server.getNumberOpenConnections());

            // Check the client gets its properties updated.
            client.update();
            assertNotNull(client.getProperty(ClientProperty.CONNECTED_CLIENTS));
            assertNotNull(client.getProperty(ClientProperty.MAPS_AVAILABLE));
            assertNotNull(client.getProperty(ClientProperty.SAVED_GAMES));

            // Test 2 : a client that has already logged on.
            FreerailsClient client1 = new FreerailsClient();
            response = client1.connect(server, "name", "password");
            assertFalse("The player is already logged on.", response.isSuccess());
            assertEquals(1, server.getNumberOpenConnections());

            // Test 3 : connecting a client.
            FreerailsClient client3 = new FreerailsClient();
            response = client3.connect(server, "name3", "password");
            assertTrue(response.isSuccess());
            assertEquals(2, server.getNumberOpenConnections());

            // Test 4 : disconnect the client from test 1.
            client.disconnect();
            assertEquals(1, server.getNumberOpenConnections());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     *
     */
    public void testNewGame() {
        assertEquals(0, server.getNumberOpenConnections());

        // Connect 2 clients.
        FreerailsClient client0 = new FreerailsClient();
        LogOnResponse response0 = client0.connect(server, "client0", "password");
        assertTrue(response0.isSuccess());
        FreerailsClient client1 = new FreerailsClient();
        LogOnResponse response1 = client1.connect(server, "client1", "password");
        assertTrue(response1.isSuccess());
        assertEquals(2, server.getNumberOpenConnections());
        client0.update();
        client1.update();

        // Start a new game.
        assertNull(client0.getWorld());
        assertNull(client1.getWorld());

        ImmutableList<String> mapNames = (ImmutableList<String>) client0.getProperty(ClientProperty.MAPS_AVAILABLE);

        final int commandID = 66;
        CommandToServer message2 = new NewGameCommandToServer(commandID, mapNames.get(0));
        client0.write(message2);
        assertTrue(server.isNewPlayersAllowed());
        server.update();
        assertFalse("New players cannot be added once the game has started.",
                server.isNewPlayersAllowed());

        /*
         * Note, the following would have happened anyway when client0.update();
         * gets called.
         */
        Serializable obj = client0.read();
        CommandToClient cc = (CommandToClient) obj;
        client0.write(cc.execute(client0));

        obj = client0.read();
        CommandStatus status = (CommandStatus) obj;
        assertTrue(status.isSuccessful());
        assertEquals(commandID, status.getId());

        // The server should have sent a message2 that sets the world object.
        client0.update();
        client1.update();
        assertNotNull(client0.getWorld());
        assertNotNull(client1.getWorld());

        // The server will not have read the players confirmation yet.
        assertFalse(server.isConfirmed(0));
        assertFalse(server.isConfirmed(1));
        server.update();

        // Now it will.
        assertTrue(server.isConfirmed(0));
        assertTrue(server.isConfirmed(1));

        /*
         * The number of players on the world object should be the same as the
         * number of players under the ClientControlInterface.CONNECTED_CLIENTS
         * key
         */
        int connectedPlayers = ((ImmutableList<String>) client0.getProperty(ClientProperty.CONNECTED_CLIENTS)).size();
        int playersOnWorldObject = client0.getWorld().getNumberOfPlayers();
        assertEquals(connectedPlayers, playersOnWorldObject);

        World world = client0.getWorld();
        assertNotNull(world.getPlayer(0));
        assertNotNull(world.getPlayer(1));

        /*
         * Now check that attempts to log on by new players are rejected.
         */

        assertEquals(2, server.getNumberOpenConnections());
        FreerailsClient client = new FreerailsClient();
        LogOnResponse response = client.connect(server, "Late player","password");
        assertFalse(response.isSuccess());
        assertEquals(2, server.getNumberOpenConnections());
    }

    /**
     * Tests sending moves between client and server.
     */
    public void testSendingMoves() {
        try {
            // Set up and start a game with 2 clients.
            FreerailsClient client0 = new FreerailsClient();
            LogOnResponse response0 = client0.connect(server, "client0", "password");
            assertTrue(response0.isSuccess());
            FreerailsClient client1 = new FreerailsClient();
            LogOnResponse response1 = client1.connect(server, "client1",
                    "password");
            assertTrue(response1.isSuccess());
            client0.update();
            client1.update();

            ImmutableList<String> mapNames = (ImmutableList<String>) client0.getProperty(ClientProperty.MAPS_AVAILABLE);
            CommandToServer message2 = new NewGameCommandToServer(99, mapNames.get(0));
            client0.write(message2);
            server.update();
            client0.update();
            client1.update();

            // Now try sending some moves.
            World world = client0.getWorld();
            Player player0 = world.getPlayer(0);
            FreerailsPrincipal principal0 = player0.getPrincipal();
            Transaction transaction = new MoneyTransaction(new Money(100), TransactionCategory.MISC_INCOME);
            Move move = new AddTransactionMove(principal0, transaction);
            World copyOfWorld = world.defensiveCopy();
            assertEquals(copyOfWorld, world);

            MoveStatus status = move.doMove(copyOfWorld, principal0);

            assertTrue(status.succeeds());

            // client0.write(move);
            client0.process(move);
            server.update();

            MoveStatus reply = (MoveStatus) client0.read();
            assertEquals(MoveStatus.MOVE_OK, reply);
            client0.processMessage(reply);

            /*
             * After performing an update, the server and 2 clients' copies of
             * the world object should be in the same state.
             */
            server.update();
            client0.update();
            client1.update();
            assertEquals(copyOfWorld, world);
            assertEquals(copyOfWorld, client1.getWorld());
            assertEquals(copyOfWorld, server.getCopyOfWorld());

            // Test disconnecting and reconnecting during play.
            client0.disconnect();
            client1.process(move);
            // client1.write(move);
            move.doMove(client1.getWorld(), principal0);

            client1.update();
            server.update();
            client1.update();
            assertFalse(world.equals(client1.getWorld()));
            response0 = client0.connect(server, "client0", "password");
            assertTrue(response0.isSuccess());
            assertEquals(0, response0.getId());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests sending premoves between client and server.
     */
    public void testSendingPreMoves() {
        try {
            // Set up and start a game with 2 clients.
            FreerailsClient client0 = new FreerailsClient();
            LogOnResponse response0 = client0.connect(server, "client0", "password");
            assertTrue(response0.isSuccess());
            FreerailsClient client1 = new FreerailsClient();
            LogOnResponse response1 = client1.connect(server, "client1", "password");
            assertTrue(response1.isSuccess());
            client0.update();
            client1.update();

            ImmutableList<String> mapNames = (ImmutableList<String>) client0.getProperty(ClientProperty.MAPS_AVAILABLE);
            CommandToServer message2 = new NewGameCommandToServer(99, mapNames
                    .get(0));
            client0.write(message2);
            server.update();
            client0.update();
            client1.update();

            // Now try sending some premoves.
            Player player0 = client0.getWorld().getPlayer(0);
            FreerailsPrincipal principal0 = player0.getPrincipal();

            World copyOfWorld = client0.getWorld().defensiveCopy();
            assertEquals(copyOfWorld, client0.getWorld());

            Move move = TimeTickMove.generate(copyOfWorld);
            MoveStatus status = move.doMove(copyOfWorld, principal0);
            assertTrue(status.succeeds());
            client0.processMoveGenerator(TimeTickMoveGenerator.INSTANCE);
            server.update();

            TryMoveStatus reply = (TryMoveStatus) client0.read();
            assertEquals(TryMoveStatus.TRY_MOVE_OK, reply);
            client0.processMessage(reply);

            server.update();
            client0.update();
            client1.update();
            assertEquals(copyOfWorld, client0.getWorld());
            assertEquals(copyOfWorld, client1.getWorld());
            assertEquals(copyOfWorld, server.getCopyOfWorld());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     *
     */
    public void testLoadingGame() {
        try {
            int commandID = 0;
            // Add client to server.
            FreerailsClient client0 = new FreerailsClient();
            LogOnResponse response0 = client0.connect(server, "client0", "password");
            assertTrue(response0.isSuccess());
            client0.update();

            // Start game
            ImmutableList<String> mapNames = (ImmutableList<String>) client0.getProperty(ClientProperty.MAPS_AVAILABLE);
            CommandToServer newGameMessage2 = new NewGameCommandToServer(commandID++, mapNames.get(0));
            CommandStatus commandStatus = newGameMessage2.execute(server);
            assertTrue(commandStatus.isSuccessful());

            // Save game and stop server
            String savedGameName = "game1";
            CommandToServer saveGameMessage2 = new SaveGameCommandToServer(commandID++, savedGameName);
            commandStatus = saveGameMessage2.execute(server);
            assertTrue(commandStatus.isSuccessful());
            server.stopGame();

            // Start 2nd server with saved game
            server = new FreerailsGameServer(savedGamesManager);
            server.loadGame(savedGameName);
            assertEquals(0, server.getNumberOpenConnections());
            // Attempt to attach invalid player.
            client0 = new FreerailsClient();
            response0 = client0.connect(server, "client0", "batman");
            assertFalse("bad password", response0.isSuccess());
            assertEquals(0, server.getNumberOpenConnections());

            response0 = client0.connect(server, "client1", "password");
            assertFalse("bad username", response0.isSuccess());
            assertEquals(0, server.getNumberOpenConnections());

            response0 = client0.connect(server, "client0", "password");
            assertTrue("Ok, same username and password as before.", response0
                    .isSuccess());
            assertEquals(1, server.getNumberOpenConnections());
        } catch (Exception e) {
            fail();
        }
    }
}