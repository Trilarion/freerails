/*
 * Created on Apr 18, 2004
 */
package jfreerails.network;

import jfreerails.controller.PreMove;
import jfreerails.controller.PreMoveStatus;
import jfreerails.controller.TimeTickPreMove;
import jfreerails.move.AddTransactionMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.accounts.Receipt;
import jfreerails.world.accounts.Transaction;
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

    protected void setUp() throws Exception {
        server = new FreerailsGameServer(new SavedGamesManager4UnitTests());
    }

    /** Copy & pasted from FreerailsClientTest, then edited. */
    public void testLogon() {
        try {
            /* Test 1 : connecting a client. */
            assertEquals("No client connected yet.", 0,
                server.countOpenConnections());

            FreerailsClient client = new FreerailsClient();
            LogOnResponse response = client.connect(server, "name", "password");
            assertTrue(response.isSuccessful());
            assertEquals(1, server.countOpenConnections());

            //Check the client gets its properties updated.
            client.update();
            assertNotNull(client.getProperty(
                    ClientControlInterface.CONNECTED_CLIENTS));
            assertNotNull(client.getProperty(
                    ClientControlInterface.MAPS_AVAILABLE));
            assertNotNull(client.getProperty(ClientControlInterface.SAVED_GAMES));

            /* Test 2 : a client that has already logged on. */
            FreerailsClient client1 = new FreerailsClient();
            response = client1.connect(server, "name", "password");
            assertFalse("The player is already logged on.",
                response.isSuccessful());
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
        LogOnResponse response0 = client0.connect(server, "client0", "password");
        assertTrue(response0.isSuccessful());
        FreerailsClient client1 = new FreerailsClient();
        LogOnResponse response1 = client1.connect(server, "client1", "password");
        assertTrue(response1.isSuccessful());
        assertEquals(2, server.countOpenConnections());
        client0.update();
        client1.update();

        /* Start a new game. */
        assertNull(client0.getWorld());
        assertNull(client1.getWorld());

        String[] mapNames = (String[])client0.getProperty(ClientControlInterface.MAPS_AVAILABLE);

        final int commandID = 66;
        ServerCommand command = new NewGameServerCommand(commandID, mapNames[0]);
        client0.write(command);
        assertTrue(server.isNewPlayersAllowed());
        server.update();
        assertFalse("New players cannot be added once the game has started.",
            server.isNewPlayersAllowed());

        /* Note, the following would have happened anyway when client0.update();
        gets called.*/
        ClientCommand cc = (ClientCommand)client0.read();
        client0.write(cc.execute(client0));

        CommandStatus status = (CommandStatus)client0.read();
        assertTrue(status.isSuccessful());
        assertEquals(commandID, status.getId());

        /* The server should have sent a command that sets the world object. */
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
        int connectedPlayers = ((String[])client0.getProperty(ClientControlInterface.CONNECTED_CLIENTS)).length;
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
        LogOnResponse response = client.connect(server, "Late player", "password");
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

            String[] mapNames = (String[])client0.getProperty(ClientControlInterface.MAPS_AVAILABLE);
            ServerCommand command = new NewGameServerCommand(99, mapNames[0]);
            client0.write(command);
            server.update();
            client0.update();
            client1.update();

            /* Now try sending some moves. */
            Player player0 = client0.getWorld().getPlayer(0);
            FreerailsPrincipal principal0 = player0.getPrincipal();
            Transaction t = new Receipt(new Money(100), Transaction.Category.MISC_INCOME);
            Move move = new AddTransactionMove(principal0, t);
            World copyOfWorld = client0.getWorld().defensiveCopy();
            assertEquals(copyOfWorld, client0.getWorld());

            MoveStatus status = move.doMove(copyOfWorld, principal0);

            assertTrue(status.isOk());

            //client0.write(move);
            client0.processMove(move);
            server.update();

            MoveStatus reply = (MoveStatus)client0.read();
            assertEquals(MoveStatus.MOVE_OK, reply);
            client0.processMessage(reply);

            /* After performing an update, the server and 2 clients' copies of the world object
             * should be in the same state.
             */
            server.update();
            client0.update();
            client1.update();
            assertEquals(copyOfWorld, client0.getWorld());
            assertEquals(copyOfWorld, client1.getWorld());
            assertEquals(copyOfWorld, server.getCopyOfWorld());

            /* Test disconnecting and reconnecting during play.*/
            client0.disconnect();
            client1.processMove(move);
            // client1.write(move);
            move.doMove(client1.getWorld(), principal0);

            client1.update();
            server.update();
            client1.update();
            assertFalse(client0.getWorld().equals(client1.getWorld()));
            response0 = client0.connect(server, "client0", "password");
            assertTrue(response0.isSuccessful());
            assertEquals(0, response0.getPlayerID());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
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

            String[] mapNames = (String[])client0.getProperty(ClientControlInterface.MAPS_AVAILABLE);
            ServerCommand command = new NewGameServerCommand(99, mapNames[0]);
            client0.write(command);
            server.update();
            client0.update();
            client1.update();

            /* Now try sending some premoves. */
            Player player0 = client0.getWorld().getPlayer(0);
            FreerailsPrincipal principal0 = player0.getPrincipal();

            PreMove pm = new TimeTickPreMove();

            World copyOfWorld = client0.getWorld().defensiveCopy();
            assertEquals(copyOfWorld, client0.getWorld());

            Move move = pm.generateMove(copyOfWorld);
            MoveStatus status = move.doMove(copyOfWorld, principal0);
            assertTrue(status.isOk());
            client0.processPreMove(pm);
            server.update();

            PreMoveStatus reply = (PreMoveStatus)client0.read();
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
    
    public void testLoadingGame(){
    	
    }
}