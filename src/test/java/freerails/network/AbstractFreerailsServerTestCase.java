package freerails.network;

import junit.framework.TestCase;

/**
 * Test cases that use FreerailsGameServer <b>and</b> connect over the Internet
 * should extend this class .
 *
 */
public abstract class AbstractFreerailsServerTestCase extends TestCase {
    FreerailsGameServer server;
    private InetConnectionAccepter connectionAccepter;

    /**
     *
     * @throws Exception
     */
    @Override
    protected synchronized void setUp() throws Exception {
        server = FreerailsGameServer
                .startServer(new SavedGamesManager4UnitTests());
        connectionAccepter = new InetConnectionAccepter(0, server);

        Thread serverThread = new Thread(connectionAccepter);
        serverThread.start();
    }

    /**
     *
     * @throws Exception
     */
    @Override
    protected synchronized void tearDown() throws Exception {
        connectionAccepter.stop();
    }

    int getPort() {
        return connectionAccepter.getLocalPort();
    }

    String getIpAddress() {
        return "127.0.0.1";
    }
}