/*
 * Created on Apr 17, 2004
 */
package jfreerails.network;

import java.io.IOException;
import java.util.Arrays;


/** Tests FreerailsClient with a network server.
 *
 *  @author Luke
 *
 */
public class FreerailsClientTest extends AbstractFreerailsServerTestCase {
    public void testLogon() {
        try {
            /* Test 1 : connecting a client.*/
            assertEquals("No client connected yet.", 0,
                server.countOpenConnections());

            FreerailsClient client = new FreerailsClient();
            LogOnResponse response = client.connect(ipAddress, port, "name",
                    "password");
            assertTrue(response.isSuccessful());
            assertEquals(1, server.countOpenConnections());

            assertMapsAndSaveGamesReceived(client);
            assertConnectClientsEquals(client, new String[] {"name"});

            /* Test 2 : a client that has already logged on.*/
            FreerailsClient client2 = new FreerailsClient();
            response = client2.connect(ipAddress, port, "name", "password");
            assertFalse("The player is already logged on.",
                response.isSuccessful());
            assertEquals(1, server.countOpenConnections());

            /* Test 3 :  connecting a client.*/
            FreerailsClient client3 = new FreerailsClient();
            response = client3.connect(ipAddress, port, "name3", "password");
            assertTrue(response.isSuccessful());
            assertEquals(2, server.countOpenConnections());

            /* read list of connected clients.*/
            assertConnectClientsEquals(client, new String[] {"name", "name3"});
            assertMapsAndSaveGamesReceived(client3);
            assertConnectClientsEquals(client3, new String[] {"name", "name3"});

            /* Test 4 : disconnect the client from test 1.*/
            client.disconnect();
            assertEquals(1, server.countOpenConnections());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private void assertConnectClientsEquals(FreerailsClient client,
        String[] expectedPlayerNames) throws IOException, InterruptedException {
        ClientCommand clientCommand = (ClientCommand)client.read();
        clientCommand.execute(client);

        String[] actualPlayerNames = (String[])client.getProperty(ClientControlInterface.CONNECTED_CLIENTS);
        assertNotNull(actualPlayerNames);
        assertTrue(Arrays.equals(expectedPlayerNames, actualPlayerNames));
    }

    private void assertMapsAndSaveGamesReceived(FreerailsClient client)
        throws IOException, InterruptedException {
        //2 commands to read.
        ClientCommand clientCommand = (ClientCommand)client.read();
        clientCommand.execute(client);
        clientCommand = (ClientCommand)client.read();
        clientCommand.execute(client);

        Object maps = client.getProperty(ClientControlInterface.MAPS_AVAILABLE);
        assertNotNull(maps);

        Object savedGames = client.getProperty(ClientControlInterface.SAVED_GAMES);
        assertNotNull(savedGames);
    }
}