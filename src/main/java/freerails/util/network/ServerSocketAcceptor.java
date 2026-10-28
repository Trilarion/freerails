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

import freerails.util.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

/**
 * Accepts incoming sockets and offers them to a caller-provided queue.
 */
public class ServerSocketAcceptor implements AutoCloseable {

    private final ServerSocket serverSocket;
    private final BlockingQueue<Socket> sockets;
    private final Thread acceptThread;
    private volatile boolean open;
    private volatile IOException failure;

    public ServerSocketAcceptor(InetSocketAddress address, BlockingQueue<Socket> sockets)
            throws IOException {
        this.sockets = Utils.verifyNotNull(sockets);
        Utils.verifyNotNull(address);
        serverSocket = new ServerSocket(address.getPort(), 0, address.getAddress());
        open = true;
        acceptThread = new Thread(this::acceptConnections, "Server Connection Acceptor");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    private void acceptConnections() {
        try {
            while (open) {
                Socket socket = serverSocket.accept();
                if (!sockets.offer(socket)) {
                    socket.close();
                }
            }
        } catch (IOException e) {
            if (open) {
                failure = e;
            }
        } finally {
            open = false;
            if (!serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    if (failure == null) {
                        failure = e;
                    } else {
                        failure.addSuppressed(e);
                    }
                }
            }
        }
    }

    public int getLocalPort() {
        return serverSocket.getLocalPort();
    }

    public boolean isOpen() {
        return open;
    }

    /**
     * Returns an unexpected accept failure, or {@code null} if none occurred.
     */
    public IOException getFailure() {
        return failure;
    }

    @Override
    public void close() {
        open = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new IllegalStateException("Could not close server socket.", e);
        } finally {
            try {
                acceptThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while closing server socket.", e);
            }
        }
    }
}
