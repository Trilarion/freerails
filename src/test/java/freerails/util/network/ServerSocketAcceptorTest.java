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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 */
public class ServerSocketAcceptorTest extends TestCase {

    public void testIt() throws IOException {
        // the connected sockets on the server side
        BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>(2);

        // a new ServerSocketAcceptor
        InetSocketAddress address = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
        ServerSocketAcceptor acceptor = new ServerSocketAcceptor(address, sockets);
        assertTrue(acceptor.isOpen());

        // the address for connection sockets on the client side
        address = new InetSocketAddress(InetAddress.getLoopbackAddress(), acceptor.getLocalPort());
        Socket s1 = new Socket(address.getAddress(), address.getPort());
        Socket s2 = new Socket(address.getAddress(), address.getPort());

        try {
            long deadline = System.currentTimeMillis() + 2_000;
            while (sockets.size() < 2 && System.currentTimeMillis() < deadline) {
                Thread.sleep(10);
            }
            assertEquals(2, sockets.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        } finally {
            acceptor.close();
            s1.close();
            s2.close();
            for (Socket accepted : sockets) {
                accepted.close();
            }
        }
        assertFalse(acceptor.isOpen());
    }

    public void testFullQueueClosesExcessConnectionWithoutStoppingAcceptor() throws IOException {
        BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>(1);
        InetSocketAddress address = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
        ServerSocketAcceptor acceptor = new ServerSocketAcceptor(address, sockets);
        Socket firstClient = new Socket(InetAddress.getLoopbackAddress(), acceptor.getLocalPort());
        Socket secondClient = new Socket(InetAddress.getLoopbackAddress(), acceptor.getLocalPort());
        secondClient.setSoTimeout(2_000);

        try {
            assertEquals("Excess accepted sockets should be closed.", -1,
                    secondClient.getInputStream().read());
            assertTrue("Queue saturation must not stop accepting.", acceptor.isOpen());
            assertEquals(1, sockets.size());
        } finally {
            acceptor.close();
            firstClient.close();
            secondClient.close();
            for (Socket accepted : sockets) {
                accepted.close();
            }
        }
    }
}
