/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.network;

import junit.framework.TestCase;

/**
 * Test cases that use EchoGameServer should extend this class.
 */
public abstract class AbstractEchoGameServerTestCase extends TestCase {
    final String ipAddress = "127.0.0.1";
    InetConnectionAccepter server;
    EchoGameServer echoGameServer;

    /**
     * @throws Exception
     */
    @Override
    protected synchronized void setUp() throws Exception {
        echoGameServer = EchoGameServer.startServer();

        /*
         * There was a problem that occurred intermittently when the unit tests
         * were run as a batch. I think it was to do with reusing ports in quick
         * succession. Passing 0 as the port allow us to listen on an
         * unspecified port whose number we obtain by calling getLocalPort().
         * This making this change, the problem has not occurred.
         */
        server = new InetConnectionAccepter(0, echoGameServer);

        Thread serverThread = new Thread(server);
        serverThread.start();
    }

    /**
     * @throws Exception
     */
    @Override
    protected synchronized void tearDown() throws Exception {
        server.stop();
    }
}