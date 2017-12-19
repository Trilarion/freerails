package freerails.network;

/**
 * Junit test for NewInetConnection.
 *
 */
public class InetConnectionTest extends AbstractEchoGameServerTestCase {

    /**
     *
     */
    public void testConnecting() {
        try {
            assertEquals(0, echoGameServer.countOpenConnections());

            InetConnection connection = new InetConnection(ipAddress, server
                    .getLocalPort());
            connection.open();
            assertEquals(1, echoGameServer.countOpenConnections());

            InetConnection connection2 = new InetConnection(ipAddress, server
                    .getLocalPort());
            connection2.open();
            assertEquals(2, echoGameServer.countOpenConnections());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}