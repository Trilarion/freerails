/*
 * Created on Apr 16, 2004
 */
package jfreerails.controller.net;


/**
 *
 *  @author Luke
 *
 */
public class NewInetConnectionTest extends AbstractEchoGameServerTestCase {
    public void testConnecting() {
        try {
            assertEquals(0, echoGameServer.countOpenConnections());

            NewInetConnection connection = new NewInetConnection(ipAddress, port);
            connection.open();
            assertEquals(1, echoGameServer.countOpenConnections());

            NewInetConnection connection2 = new NewInetConnection(ipAddress,
                    port);
            connection2.open();
            assertEquals(2, echoGameServer.countOpenConnections());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}