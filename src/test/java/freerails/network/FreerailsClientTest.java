/*
 * Created on Apr 17, 2004
 */
package freerails.network;

import java.io.IOException;

import freerails.controller.Message2Client;
import freerails.controller.ClientControlInterface.ClientProperty;
import freerails.world.common.ImStringList;

/**
 * Tests FreerailsClient with a network server.
 * 
 * @author Luke
 * 
 */
public class FreerailsClientTest extends AbstractFreerailsServerTestCase {
    public void testLogon() {
        try {
            /* Test 1 : connecting a client. */
            assertEquals("No client connected yet.", 0, server
                    .countOpenConnections());

            FreerailsClient client = new FreerailsClient();
            LogOnResponse response = client.connect(getIpAddress(), getPort(),
                    "name", "password");
            assertTrue(response.isSuccessful());
            assertEquals(1, server.countOpenConnections());

            assertMapsAndSaveGamesReceived(client);
            assertConnectClientsEquals(client, new ImStringList("name"));

            /* Test 2 : a client that has already logged on. */
            FreerailsClient client2 = new FreerailsClient();
            response = client2.connect(getIpAddress(), getPort(), "name",
                    "password");
            assertFalse("The player is already logged on.", response
                    .isSuccessful());
            assertEquals(1, server.countOpenConnections());

            /* Test 3 : connecting a client. */
            FreerailsClient client3 = new FreerailsClient();
            response = client3.connect(getIpAddress(), getPort(), "name3",
                    "password");
            assertTrue(response.isSuccessful());
            assertEquals(2, server.countOpenConnections());

            /* read list of connected clients. */
            assertConnectClientsEquals(client,
                    new ImStringList("name", "name3"));
            assertMapsAndSaveGamesReceived(client3);
            assertConnectClientsEquals(client3, new ImStringList("name",
                    "name3"));

            /* Test 4 : disconnect the client from test 1. */
            client.disconnect();
            assertEquals(1, server.countOpenConnections());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private void assertConnectClientsEquals(FreerailsClient client,
            ImStringList expectedPlayerNames) throws IOException,
            InterruptedException {
        Message2Client message2Client = (Message2Client) client.read();
        message2Client.execute(client);

        ImStringList actualPlayerNames = (ImStringList) client
                .getProperty(ClientProperty.CONNECTED_CLIENTS);
        assertNotNull(actualPlayerNames);
        assertEquals(expectedPlayerNames, actualPlayerNames);
    }

    private void assertMapsAndSaveGamesReceived(FreerailsClient client)
            throws IOException, InterruptedException {
        // 2 commands to read.
        Message2Client message2Client = (Message2Client) client.read();
        message2Client.execute(client);
        message2Client = (Message2Client) client.read();
        message2Client.execute(client);

        Object maps = client.getProperty(ClientProperty.MAPS_AVAILABLE);
        assertNotNull(maps);

        Object savedGames = client.getProperty(ClientProperty.SAVED_GAMES);
        assertNotNull(savedGames);
    }
}