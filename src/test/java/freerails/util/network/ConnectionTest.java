package freerails.util.network;

import freerails.network.LogOnCredentials;
import freerails.model.world.World;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ConnectionTest extends TestCase {

    public void testAllowedProtocolMessagesRoundTrip() throws Exception {
        EchoServer server = new EchoServer();
        server.startRunning(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        Connection client = Connection.make(new InetSocketAddress(
                InetAddress.getLoopbackAddress(), server.getLocalPort()));

        try {
            client.sendObject("ping");
            assertEquals("ping", client.receiveObject());
            LogOnCredentials credentials = new LogOnCredentials("player", "secret");
            client.sendObject(credentials);
            assertEquals(credentials, client.receiveObject());
        } finally {
            client.close();
            server.stopRunning();
        }
    }

    public void testClosesWhenPeerSendsDisallowedClass() throws Exception {
        try (ServerSocket listener = new ServerSocket(0, 0, InetAddress.getLoopbackAddress())) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Connection> serverConnectionFuture = executor.submit(() -> {
                Socket socket = listener.accept();
                return Connection.make(socket);
            });
            Connection client = Connection.make(new InetSocketAddress(
                    InetAddress.getLoopbackAddress(), listener.getLocalPort()));
            Connection serverConnection = serverConnectionFuture.get(2, TimeUnit.SECONDS);

            try {
                client.sendObject(new Date());
                awaitConnectionClose(serverConnection);
                assertNotNull(serverConnection.getReaderFailure());
                assertReceiveFailsAfterRemoteClosure(client);
            } finally {
                client.close();
                serverConnection.close();
                executor.shutdownNow();
            }
        }
    }

    public void testReceiveFailsWhenPeerCloses() throws Exception {
        EchoServer server = new EchoServer();
        server.startRunning(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        Connection client = Connection.make(new InetSocketAddress(
                InetAddress.getLoopbackAddress(), server.getLocalPort()));

        client.close();
        try {
            assertReceiveFailsAfterRemoteClosure(client);
        } finally {
            server.stopRunning();
        }
    }

    public void testDefaultWorldPassesDeserializationFilter() throws Exception {
        try (ServerSocket listener = new ServerSocket(0, 0, InetAddress.getLoopbackAddress())) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Connection> serverConnectionFuture = executor.submit(() ->
                    Connection.make(listener.accept()));
            Connection client = Connection.make(new InetSocketAddress(
                    InetAddress.getLoopbackAddress(), listener.getLocalPort()));
            Connection serverConnection = serverConnectionFuture.get(2, TimeUnit.SECONDS);
            World world = WorldGenerator.defaultWorld();

            try {
                client.sendObject(world);
                Serializable received;
                try {
                    received = serverConnection.receiveObject();
                } catch (IllegalStateException e) {
                    fail("World was rejected by the input filter: " + e.getCause());
                    return;
                }
                assertEquals(world, received);
            } finally {
                client.close();
                serverConnection.close();
                executor.shutdownNow();
            }
        }
    }

    private static void awaitConnectionClose(Connection connection) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 2_000;
        while (connection.isOpen() && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        assertFalse("Connection should close after rejected input.", connection.isOpen());
    }

    private static void assertReceiveFailsAfterRemoteClosure(Connection connection) {
        try {
            connection.receiveObject();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // expected
        }
    }
}
