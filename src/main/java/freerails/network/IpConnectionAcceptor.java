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

import freerails.network.gameserver.GameServer;
import freerails.util.SynchronizedFlag;
import freerails.util.Utils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * When this class is run in a thread it accepts new connections to its Server
 * Socket and adds them to the GameServer that was passed to its constructor.
 */
public class IpConnectionAcceptor implements Runnable {

    private static final Logger logger = Logger.getLogger(IpConnectionAcceptor.class.getName());
    private final GameServer gameServer;
    private final SynchronizedFlag keepRunning = new SynchronizedFlag(true);
    private final ServerSocket serverSocket;

    /**
     * @param gameServer
     * @param port
     * @throws IOException
     */
    public IpConnectionAcceptor(GameServer gameServer, int port) throws IOException {
        this.gameServer = Utils.verifyNotNull(gameServer);
        serverSocket = new ServerSocket(port);
        // TODO check that is bound? open?
    }

    public void run() {
        Thread.currentThread().setName("InetConnectionAccepter, port " + serverSocket.getLocalPort());

        try {
            logger.debug("Accepting connections on port " + serverSocket.getLocalPort());

            while (keepRunning.isSet()) {
                Socket socket = serverSocket.accept();
                logger.debug("Incoming connection from " + socket.getRemoteSocketAddress());

                synchronized (this) {
                    synchronized (gameServer) {
                        ConnectionToClient connection = new IpConnectionToClient(socket);
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
        keepRunning.unset();
        serverSocket.close();
    }
}