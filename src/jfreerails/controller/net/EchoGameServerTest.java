/*
 * Created on Apr 13, 2004
 */
package jfreerails.controller.net;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.Money;
import junit.framework.TestCase;


/**
 *
 *  @author Luke
 *
 */
public class EchoGameServerTest extends TestCase {
    InetConnectionAccepter server;
    EchoGameServer echoGameServer;
    final int port = 6666;
    final String ipAddress = "127.0.0.1";

    protected void setUp() throws Exception {
        echoGameServer = EchoGameServer.startServer();
        server = new InetConnectionAccepter(port, echoGameServer);

        Thread serverThread = new Thread(server);
        serverThread.start();
    }

    public void testConnecting() {
        try {
            assertEquals(0, echoGameServer.countOpenConnections());

            InetConnection2Server con1 = new InetConnection2Server(ipAddress,
                    port);
            InetConnection2Server con2 = new InetConnection2Server(ipAddress,
                    port);
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

    protected void tearDown() throws Exception {
        server.stop();
    }
}