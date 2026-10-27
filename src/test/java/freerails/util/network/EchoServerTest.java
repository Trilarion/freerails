/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
        } catch (InterruptedException ignored) {}

        assertEquals(1, server.getNumberActiveConnections());

        String word = "ping";
        c.sendObject(word);
        Serializable object = c.receiveObject();
        assertEquals(word, object);

        c.close();
        assertTrue(!c.isOpen());
        awaitActiveConnections(server, 0);


        server.stopRunning();
        assertTrue(!server.isRunning());
    }

    private static void awaitActiveConnections(EchoServer server, int expected) {
        long deadline = System.currentTimeMillis() + 2_000;
        while (server.getNumberActiveConnections() != expected
                && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError(e);
            }
        }
        assertEquals(expected, server.getNumberActiveConnections());
    }

}
