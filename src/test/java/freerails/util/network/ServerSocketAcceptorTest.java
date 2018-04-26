package freerails.util.network;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 */
public class ServerSocketAcceptorTest extends TestCase {

    public void testIt() throws IOException {
        // the connected sockets on the server side
        BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>(2);

        // a new ServerSocketAcceptor
        InetSocketAddress address = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
        ServerSocketAcceptor acceptor = new ServerSocketAcceptor(address, sockets);
        assertTrue(acceptor.isOpen());

        // the address for connection sockets on the client side
        address = new InetSocketAddress(InetAddress.getLoopbackAddress(), acceptor.getLocalPort());
        Socket s1 = new Socket(address.getAddress(), address.getPort());
        Socket s2 = new Socket(address.getAddress(), address.getPort());

        try {
            Thread.sleep(20);
        } catch (InterruptedException ignored) {};

        // close acceptor
        acceptor.close();
        assertTrue(!acceptor.isOpen());

        // check number of connected sockets
        assertEquals(2, sockets.size());
    }
}
