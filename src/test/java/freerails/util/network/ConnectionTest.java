package freerails.util.network;

import freerails.network.LogOnCredentials;
import freerails.model.player.Player;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackPiece;
import freerails.model.world.World;
import freerails.move.mapupdatemove.ChangeTrackPieceCompositeMove;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.util.IntPoint2D;
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

/**
 * Tests connection behavior.
 */
public class ConnectionTest extends TestCase {

    /**
     * Tests allowed protocol messages round trip behavior.
     * @throws Exception if the test allowed protocol messages round trip operation fails.
     */
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

    /**
     * Tests inbound queue backpressures when messages are not consumed behavior.
     * @throws Exception if the test inbound queue backpressures when messages are not consumed operation fails.
     */
    public void testInboundQueueBackpressuresWhenMessagesAreNotConsumed() throws Exception {
        try (ServerSocket listener = new ServerSocket(0, 0, InetAddress.getLoopbackAddress())) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Connection> serverConnectionFuture = executor.submit(() ->
                    Connection.make(listener.accept()));
            Connection client = Connection.make(new InetSocketAddress(
                    InetAddress.getLoopbackAddress(), listener.getLocalPort()));
            Connection serverConnection = serverConnectionFuture.get(2, TimeUnit.SECONDS);

            try {
                int messageCount = 100;
                for (int i = 0; i < messageCount; i++) {
                    client.sendObject(i);
                }

                for (int i = 0; i < messageCount; i++) {
                    assertEquals(i, serverConnection.receiveObject());
                }

                assertTrue(serverConnection.isOpen());
                assertNull(serverConnection.getReaderFailure());
            } finally {
                client.close();
                serverConnection.close();
                executor.shutdownNow();
            }
        }
    }

    /**
     * Tests closes when peer sends disallowed class behavior.
     * @throws Exception if the test closes when peer sends disallowed class operation fails.
     */
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

    /**
     * Tests receive fails when peer closes behavior.
     * @throws Exception if the test receive fails when peer closes operation fails.
     */
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

    /**
     * Tests default world passes deserialization filter behavior.
     * @throws Exception if the test default world passes deserialization filter operation fails.
     */
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

    /**
     * Tests composite track moves pass deserialization filtering.
     * @throws Exception if the composite track move cannot be sent.
     */
    public void testCompositeTrackMovePassesDeserializationFilter() throws Exception {
        try (ServerSocket listener = new ServerSocket(0, 0, InetAddress.getLoopbackAddress())) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Connection> serverConnectionFuture = executor.submit(() ->
                    Connection.make(listener.accept()));
            Connection client = Connection.make(new InetSocketAddress(
                    InetAddress.getLoopbackAddress(), listener.getLocalPort()));
            Connection serverConnection = serverConnectionFuture.get(2, TimeUnit.SECONDS);
            Player player = new Player(3, "Test player");
            TrackPiece trackPiece = new TrackPiece(
                    TrackConfiguration.getFlatInstance("00000001"),
                    WorldGenerator.testTrackTypes().first(),
                    player.getId());
            ChangeTrackPieceCompositeMove move = new ChangeTrackPieceCompositeMove(
                    new ChangeTrackPieceMove(null, trackPiece, new IntPoint2D(2, 3)),
                    new ChangeTrackPieceMove(null, trackPiece, new IntPoint2D(3, 3)),
                    player);

            try {
                client.sendObject(move);
                assertEquals(move, serverConnection.receiveObject());
                assertTrue(serverConnection.isOpen());
                assertNull(serverConnection.getReaderFailure());
            } finally {
                client.close();
                serverConnection.close();
                executor.shutdownNow();
            }
        }
    }

    /**
     * Performs await connection close.
     * @param connection the connection to use in the test.
     * @throws InterruptedException if the await connection close operation fails.
     */
    private static void awaitConnectionClose(Connection connection) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 2_000;
        while (connection.isOpen() && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        assertFalse("Connection should close after rejected input.", connection.isOpen());
    }

    /**
     * Asserts receive fails after remote closure.
     * @param connection the connection to use in the test.
     */
    private static void assertReceiveFailsAfterRemoteClosure(Connection connection) {
        try {
            connection.receiveObject();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // expected
        }
    }
}
