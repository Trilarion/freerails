/*
 * Created on Apr 18, 2004
 */
package jfreerails.network;

import jfreerails.controller.ClientControlInterface;
import jfreerails.controller.Message2Client;
import jfreerails.controller.Message2Server;
import jfreerails.controller.MessageStatus;
import jfreerails.controller.PreMove;
import jfreerails.controller.PreMoveStatus;
import jfreerails.controller.TimeTickPreMove;
import jfreerails.controller.ClientControlInterface.ClientProperty;
import jfreerails.move.AddTransactionMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.accounts.Receipt;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.ImStringList;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.World;
import junit.framework.TestCase;

/**
 * This test uses clients connected to a local server. This means anything sent
 * to the server arrives instantly, which makes writing the test easier.
 * 
 * @author Luke
 * @see FreerailsClientTest
 */
public class FreerailsClientWithLocalServerTest extends TestCase {
    private FreerailsGameServer server;

    private SavedGamesManager4UnitTests savedGamesManager;

    @Override
    protected void setUp() throws Exception {
        savedGamesManager = new SavedGamesManager4UnitTests();
        server = new FreerailsGameServer(savedGamesManager);
    }

    /** Copy & pasted from FreerailsClientTest, then edited. */
    public void testLogon() {
        try {
            /* Test 1 : connecting a client. */
            assertEquals("No client connected yet.", 0, server
                    .countOpenConnections());

            FreerailsClient client = new FreerailsClient();
            LogOnResponse response = client.connect(server, "name", "password");
            assertTrue(response.isSuccessful());
            assertEquals(1, server.countOpenConnections());

            // Check the client gets its properties updated.
            client.update();
            assertNotNull(client
                    .getProperty(ClientControlInterface.ClientProperty.CONNECTED_CLIENTS));
            assertNotNull(client
                    .getProperty(ClientControlInterface.ClientProperty.MAPS_AVAILABLE));
            assertNotNull(client
                    .getProperty(ClientControlInterface.ClientProperty.SAVED_GAMES));

            /* Test 2 : a client that has already logged on. */
            FreerailsClient client1 = new FreerailsClient();
            response = client1.connect(server, "name", "password");
            assertFalse("The player is already logged on.", response
                    .isSuccessful());
            assertEquals(1, server.countOpenConnections());

            /* Test 3 : connecting a client. */
            FreerailsClient client3 = new FreerailsClient();
            response = client3.connect(server, "name3", "password");
            assertTrue(response.isSuccessful());
            assertEquals(2, server.countOpenConnections());

            /* Test 4 : disconnect the client from test 1. */
            client.disconnect();
            assertEquals(1, server.countOpenConnections());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testNewGame() {
        assertEquals(0, server.countOpenConnections());

        /* Connect 2 clients. */
        FreerailsClient client0 = new FreerailsClient();
        LogOnResponse response0 = client0
                .connect(server, "client0", "password");
        assertTrue(response0.isSuccessful());
        FreerailsClient client1 = new FreerailsClient();
        LogOnResponse response1 = client1
                .connect(server, "client1", "password");
        assertTrue(response1.isSuccessful());
        assertEquals(2, server.countOpenConnections());
        client0.update();
        client1.update();

        /* Start a new game. */
        assertNull(client0.getWorld());
        assertNull(client1.getWorld());

        ImStringList mapNames = (ImStringList) client0
                .getProperty(ClientProperty.MAPS_AVAILABLE);

        final int commandID = 66;
        Message2Server message2 = new NewGameMessage2Server(commandID, mapNames
                .get(0));
        client0.write(message2);
        assertTrue(server.isNewPlayersAllowed());
        server.update();
        assertFalse("New players cannot be added once the game has started.",
                server.isNewPlayersAllowed());

        /*
         * Note, the following would have happened anyway when client0.update();
         * gets called.
         */
        FreerailsSerializable obj = client0.read();
        Message2Client cc = (Message2Client) obj;
        client0.write(cc.execute(client0));

        obj = client0.read();
        MessageStatus status = (MessageStatus) obj;
        assertTrue(status.isSuccessful());
        assertEquals(commandID, status.getId());

        /* The server should have sent a message2 that sets the world object. */
        client0.update();
        client1.update();
        assertNotNull(client0.getWorld());
        assertNotNull(client1.getWorld());

        /* The server will not have read the players confirmation yet. */
        assertFalse(server.isConfirmed(0));
        assertFalse(server.isConfirmed(1));
        server.update();

        /* Now it will. */
        assertTrue(server.isConfirmed(0));
        assertTrue(server.isConfirmed(1));

        /*
         * The number of players on the world object should be the same as the
         * number of players under the ClientControlInterface.CONNECTED_CLIENTS
         * key
         */
        int connectedPlayers = ((ImStringList) client0
                .getProperty(ClientProperty.CONNECTED_CLIENTS)).size();
        int playersOnWorldObject = client0.getWorld().getNumberOfPlayers();
        assertEquals(connectedPlayers, playersOnWorldObject);

        World w = client0.getWorld();
        assertNotNull(w.getPlayer(0));
        assertNotNull(w.getPlayer(1));

        /*
         * Now check that attempts to log on by new players are rejected.
         */

        assertEquals(2, server.countOpenConnections());
        FreerailsClient client = new FreerailsClient();
        LogOnResponse response = client.connect(server, "Late player",
                "password");
        assertFalse(response.isSuccessful());
        assertEquals(2, server.countOpenConnections());

    }

    /** Tests sending moves between client and server. */
    public void testSendingMoves() {
        try {
            /* Set up and start a game with 2 clients. */
            FreerailsClient client0 = new FreerailsClient();
            LogOnResponse response0 = client0.connect(server, "client0",
                    "password");
            assertTrue(response0.isSuccessful());
            FreerailsClient client1 = new FreerailsClient();
            LogOnResponse response1 = client1.connect(server, "client1",
                    "password");
            assertTrue(response1.isSuccessful());
            client0.update();
            client1.update();

            ImStringList mapNames = (ImStringList) client0
                    .getProperty(ClientProperty.MAPS_AVAILABLE);
            Message2Server message2 = new NewGameMessage2Server(99, mapNames
                    .get(0));
            client0.write(message2);
            server.update();
            client0.update();
            client1.update();

            /* Now try sending some moves. */
            World world = client0.getWorld();
            Player player0 = world.getPlayer(0);
            FreerailsPrincipal principal0 = player0.getPrincipal();
            Transaction t = new Receipt(new Money(100),
                    Transaction.Category.MISC_INCOME);
            Move move = new AddTransactionMove(principal0, t);
            World copyOfWorld = world.defensiveCopy();
            assertEquals(copyOfWorld, world);

            MoveStatus status = move.doMove(copyOfWorld, principal0);

            assertTrue(status.isOk());

            // client0.write(move);
            client0.processMove(move);
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

            /* Test disconnecting and reconnecting during play. */
            client0.disconnect();
            client1.processMove(move);
            // client1.write(move);
            move.doMove(client1.getWorld(), principal0);

            client1.update();
            server.update();
            client1.update();
            assertFalse(world.equals(client1.getWorld()));
            response0 = client0.connect(server, "client0", "password");
            assertTrue(response0.isSuccessful());
            assertEquals(0, response0.getPlayerID());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /** Tests sending premoves between client and server. */
    public void testSendingPreMoves() {
        try {
            /* Set up and start a game with 2 clients. */
            FreerailsClient client0 = new FreerailsClient();
            LogOnResponse response0 = client0.connect(server, "client0",
                    "password");
            assertTrue(response0.isSuccessful());
            FreerailsClient client1 = new FreerailsClient();
            LogOnResponse response1 = client1.connect(server, "client1",
                    "password");
            assertTrue(response1.isSuccessful());
            client0.update();
            client1.update();

            ImStringList mapNames = (ImStringList) client0
                    .getProperty(ClientProperty.MAPS_AVAILABLE);
            Message2Server message2 = new NewGameMessage2Server(99, mapNames
                    .get(0));
            client0.write(message2);
            server.update();
            client0.update();
            client1.update();

            /* Now try sending some premoves. */
            Player player0 = client0.getWorld().getPlayer(0);
            FreerailsPrincipal principal0 = player0.getPrincipal();

            PreMove pm = TimeTickPreMove.INSTANCE;

            World copyOfWorld = client0.getWorld().defensiveCopy();
            assertEquals(copyOfWorld, client0.getWorld());

            Move move = pm.generateMove(copyOfWorld);
            MoveStatus status = move.doMove(copyOfWorld, principal0);
            assertTrue(status.isOk());
            client0.processPreMove(pm);
            server.update();

            PreMoveStatus reply = (PreMoveStatus) client0.read();
            assertEquals(PreMoveStatus.PRE_MOVE_OK, reply);
            client0.processMessage(reply);

            server.update();
            client0.update();
            client1.update();
            assertEquals(copyOfWorld, client0.getWorld());
            assertEquals(copyOfWorld, client1.getWorld());
            assertEquals(copyOfWorld, server.getCopyOfWorld());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testLoadingGame() {
        try {
            int commandID = 0;
            // Add client to server.
            FreerailsClient client0 = new FreerailsClient();
            LogOnResponse response0 = client0.connect(server, "client0",
                    "password");
            assertTrue(response0.isSuccessful());
            client0.update();

            // Start game
            ImStringList mapNames = (ImStringList) client0
                    .getProperty(ClientProperty.MAPS_AVAILABLE);
            Message2Server newGameMessage2 = new NewGameMessage2Server(
                    commandID++, mapNames.get(0));
            MessageStatus cm = newGameMessage2.execute(server);
            assertTrue(cm.isSuccessful());

            // Save game and stop server
            String savedGameName = "game1";
            Message2Server saveGameMessage2 = new SaveGameMessage2Server(
                    commandID++, savedGameName);
            cm = saveGameMessage2.execute(server);
            assertTrue(cm.isSuccessful());
            server.stopGame();

            // Start 2nd server with saved game
            server = new FreerailsGameServer(savedGamesManager);
            server.loadgame(savedGameName);
            assertEquals(0, server.countOpenConnections());
            // Attempt to attach invalid player.
            client0 = new FreerailsClient();
            response0 = client0.connect(server, "client0", "batman");
            assertFalse("bad password", response0.isSuccessful());
            assertEquals(0, server.countOpenConnections());

            response0 = client0.connect(server, "client1", "password");
            assertFalse("bad username", response0.isSuccessful());
            assertEquals(0, server.countOpenConnections());

            response0 = client0.connect(server, "client0", "password");
            assertTrue("Ok, same username and password as before.", response0
                    .isSuccessful());
            assertEquals(1, server.countOpenConnections());

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }
}