package freerails.network;

import freerails.world.FreerailsSerializable;
import freerails.world.common.Money;

/**
 * JUnit test for EchoGameServer.
 *
 */
public class EchoGameServerTest extends AbstractEchoGameServerTestCase {
    /**
     * Tests connecting to an EchoGameServer using instances of
     * InetConnectionToServer.
     */
    public void testConnecting() {
        try {
            assertEquals(0, echoGameServer.countOpenConnections());

            InetConnectionToServer con1 = new InetConnectionToServer(ipAddress,
                    server.getLocalPort());
            InetConnectionToServer con2 = new InetConnectionToServer(ipAddress,
                    server.getLocalPort());
            assertEquals(2, echoGameServer.countOpenConnections());
            con1.writeToServer(new Money(99));
            con1.flush();

            FreerailsSerializable fs = con2.waitForObject();
            assertEquals(new Money(99), fs);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}