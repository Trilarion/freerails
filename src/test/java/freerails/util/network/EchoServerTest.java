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

        try {
            Thread.sleep(20);
        } catch (InterruptedException ignored) {}

        assertEquals(0, server.getNumberActiveConnections());


        server.stopRunning();
        assertTrue(!server.isRunning());
    }


}
