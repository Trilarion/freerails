/*
 * Created on Apr 13, 2004
 */
package jfreerails.controller.net;

import junit.framework.TestCase;


/**
 *  Test cases that use FreerailsGameServer <b>and</b> connect
 * over the Internet should extend this class  .
 *  @author Luke
 *
 */
public class AbstractFreerailsServerTestCase extends TestCase {
    private InetConnectionAccepter connectionAccepter;
    FreerailsGameServer server;
    static final int port = 6666;
    static final String ipAddress = "127.0.0.1";

    protected synchronized void setUp() throws Exception {
        server = FreerailsGameServer.startServer(new SavedGamesManager4UnitTests());
        connectionAccepter = new InetConnectionAccepter(port, server);

        Thread serverThread = new Thread(connectionAccepter);
        serverThread.start();
    }

    protected synchronized void tearDown() throws Exception {
        connectionAccepter.stop();
    }
}