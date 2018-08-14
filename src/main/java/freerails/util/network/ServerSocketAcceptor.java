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

import freerails.util.Utils;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class ServerSocketAcceptor {

    private volatile boolean open = false;
    private final ServerSocket serverSocket;
    private CountDownLatch signal;

    /**
     *
     * @param sockets
     * @throws IOException indicates that the creation of the ServerSocket may have failed
     */
    public ServerSocketAcceptor(InetSocketAddress address, BlockingQueue<Socket> sockets) throws IOException {
        Utils.verifyNotNull(address);
        Utils.verifyNotNull(sockets);
        sockets.clear();

        // new socket
        serverSocket = new ServerSocket(address.getPort(), 0, address.getAddress());

        // thread that listens for connections and adds new sockets to the sockets queue
        Thread thread = new Thread(() -> {
            open = true;
            signal.countDown();
            while (open) {
                try {
                    // wait for new connected socket (blocks unless the ServerSocket is closed)
                    Socket socket = serverSocket.accept();

                    // add new connected socket to queue (won't block, but may throw an exception)
                    sockets.add(socket);
                } catch (SocketException e) {
                    // socket is closed, probably by a call to close()
                    assert open == false;
                } catch (Exception e) {
                    // the queue might be full
                    throw new RuntimeException(e);
                }
            }
            signal.countDown();
        }, "Server Connection Acceptor");

        // start the thread
        signal = new CountDownLatch(1);
        thread.start();

        // wait until the thread is running
        try {
            signal.await();
        } catch (InterruptedException e) {
            // unexpected
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return
     */
    public int getLocalPort() {
        return serverSocket.getLocalPort();
    }

    /**
     *
     * @return
     */
    public boolean isOpen() {
        return open;
    }

    /**
     *
     */
    public void close() {
        if (!open) {
            return;
        }

        open = false;
        signal = new CountDownLatch(1);
        try {
            serverSocket.close();
            // wait until the thread has ended
            signal.await();
        } catch (IOException | InterruptedException e) {
            // unexpected
            throw new RuntimeException(e);
        }

    }

}
