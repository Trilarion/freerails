/*
 * Created on Apr 13, 2004
 */
package jfreerails.controller.net;

import junit.framework.TestCase;


/**
 *  Testcases that use EchoGameServer should extend this class.
 *  @author Luke
 *
 */
public class AbstractEchoGameServerTestCase extends TestCase {
    private InetConnectionAccepter server;
    EchoGameServer echoGameServer;
    final int port = 6666;
    final String ipAddress = "127.0.0.1";

    protected synchronized void setUp() throws Exception {
        echoGameServer = EchoGameServer.startServer();
        server = new InetConnectionAccepter(port, echoGameServer);

        Thread serverThread = new Thread(server);
        serverThread.start();
    }

    protected synchronized void tearDown() throws Exception {
        server.stop();
    }
}