package freerails.util.network;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 *
 */
public class EchoServerTest extends TestCase {

    public void testIt() throws IOException {

        InetSocketAddress address = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
        EchoServer server = new EchoServer();
        server.startRunning(address);
        assertTrue(server.isRunning());
        address = new InetSocketAddress(InetAddress.getLoopbackAddress(), server.getLocalPort());
        Connection c = Connection.make(address);
        assertTrue(c.isOpen());

        try {
            Thread.sleep(20);
        } catch (InterruptedException ignored) {};

        assertEquals(1, server.getNumberActiveConnections());

        String word = "ping";
        c.sendObject(word);
        Serializable object = c.receiveObject();
        assertEquals(word, object);

        c.close();
        assertTrue(!c.isOpen());

        try {
            Thread.sleep(20);
        } catch (InterruptedException ignored) {};

        assertEquals(0, server.getNumberActiveConnections());


        server.stopRunning();
        assertTrue(!server.isRunning());
    }


}
