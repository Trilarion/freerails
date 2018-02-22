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

import freerails.network.gameserver.EchoGameServer;
import freerails.util.SynchronizedFlag;
import junit.framework.TestCase;

/**
 * Test cases that use EchoGameServer should extend this class.
 */
public abstract class AbstractEchoGameServerTestCase extends TestCase {

    final int PORT = 14392;
    final String ipAddress = "127.0.0.1";
    public IpConnectionAcceptor server;
    EchoGameServer echoGameServer;

    /**
     * @throws Exception
     */
    @Override
    protected synchronized void setUp() throws Exception {
        super.setUp();
        EchoGameServer result;
        EchoGameServer server1 = new EchoGameServer();
        Thread t = new Thread(server1);
        t.start();

        try {
            // Wait for the server to start before returning.
            SynchronizedFlag status = server1.getStatus();
            synchronized (status) {
                status.wait();
            }

            result = server1;
        } catch (InterruptedException e) {
            throw new IllegalStateException();
        }
        echoGameServer = result;

        /*
         * There was a problem that occurred intermittently when the unit tests
         * were run as a batch. I think it was to do with reusing ports in quick
         * succession. Passing 0 as the port allow us to listen on an
         * unspecified port whose number we obtain by calling getLocalPort().
         * This making this change, the problem has not occurred.
         */
        server = new IpConnectionAcceptor(echoGameServer, PORT);

        Thread serverThread = new Thread(server);
        serverThread.start();
    }

    /**
     * @throws Exception
     */
    @Override
    protected synchronized void tearDown() throws Exception {
        super.tearDown();
        server.stop();
    }
}