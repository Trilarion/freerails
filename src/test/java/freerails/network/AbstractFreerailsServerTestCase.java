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

import freerails.network.gameserver.FreerailsGameServer;
import freerails.savegames.TestSaveGamesManager;
import freerails.util.SynchronizedFlag;
import junit.framework.TestCase;

/**
 * Test cases that use FreerailsGameServer <b>and</b> connect over the Internet
 * should extend this class.
 */
public abstract class AbstractFreerailsServerTestCase extends TestCase {

    private static final int PORT = 13856;
    public FreerailsGameServer server;
    private IpConnectionAcceptor connectionAccepter;

    /**
     * @throws Exception
     */
    @Override
    protected synchronized void setUp() throws Exception {
        super.setUp();

        FreerailsGameServer result;
        FreerailsGameServer server1 = new FreerailsGameServer(new TestSaveGamesManager());
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
        server = result;
        connectionAccepter = new IpConnectionAcceptor(server, PORT);

        Thread serverThread = new Thread(connectionAccepter);
        serverThread.start();
    }

    /**
     * @throws Exception
     */
    @Override
    protected synchronized void tearDown() throws Exception {
        super.tearDown();
        connectionAccepter.stop();
    }

    public int getPort() {
        return PORT;
    }

    public String getIpAddress() {
        return "127.0.0.1";
    }
}