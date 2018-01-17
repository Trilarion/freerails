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

import freerails.util.SynchronizedFlag;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * When this class is run in a thread it accepts new connections to its Server
 * Socket and adds them to the NewGameServer that was passed to its constructor.
 */
public class InetConnectionAccepter implements Runnable {

    private static final Logger logger = Logger.getLogger(InetConnectionAccepter.class.getName());
    private final GameServer gameServer;
    private final SynchronizedFlag keepRunning = new SynchronizedFlag(true);
    private final ServerSocket serverSocket;

    /**
     * @param port
     * @param gameServer
     * @throws IOException
     */
    public InetConnectionAccepter(int port, GameServer gameServer) throws IOException {
        if (null == gameServer) throw new NullPointerException();
        this.gameServer = gameServer;
        serverSocket = new ServerSocket(port);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            GameServer echoGameServer = EchoGameServer.startServer();
            InetConnectionAccepter accepter = new InetConnectionAccepter(6666, echoGameServer);
            Thread t = new Thread(accepter);
            t.start();
        } catch (IOException e) {
        }
    }

    public void run() {
        Thread.currentThread().setName("InetConnectionAccepter, port " + serverSocket.getLocalPort());

        try {
            logger.debug("Accepting connections on port " + serverSocket.getLocalPort());

            while (isKeepRunning()) {
                Socket socket = serverSocket.accept();
                logger.debug("Incoming connection from " + socket.getRemoteSocketAddress());

                synchronized (this) {
                    synchronized (gameServer) {
                        ConnectionToClient connection = new InetConnectionToClient(socket);
                        gameServer.addConnection(connection);
                    }
                }
            }
        } catch (IOException e) {
            try {
                stop();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * @throws IOException
     */
    public synchronized void stop() throws IOException {
        keepRunning.close();
        serverSocket.close();

        // Commented out since it causes exceptions to be thrown, fixes bug
        // 979831
        // gameServer.stop();
    }

    private boolean isKeepRunning() {
        return keepRunning.isOpen();
    }

    /**
     * @return
     */
    public int getLocalPort() {
        return serverSocket.getLocalPort();
    }
}