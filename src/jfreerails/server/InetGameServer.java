/*
 * Created on Feb 18, 2004
 */
package jfreerails.server;

import java.io.IOException;
import java.net.SocketException;
import jfreerails.controller.ConnectionToServer;
import jfreerails.controller.InetConnection;


/**
 * Sits in a loop and accepts incoming connections over the network.
 * @author rob
 */
class InetGameServer implements Runnable {
    private final InetConnection serverSocket;
    private final ServerGameController sgc;

    public InetGameServer(InetConnection serverSocket, ServerGameController sgc) {
        this.serverSocket = serverSocket;
        this.sgc = sgc;
    }

    public void run() {
        Thread.currentThread().setName("GameServer.InetGameServer");

        try {
            while (true) {
                try {
                    ConnectionToServer c = serverSocket.accept();
                    sgc.addConnection(c);
                } catch (IOException e) {
                    if (e instanceof SocketException) {
                        throw (SocketException)e;
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SocketException e) {
            /* The socket was probably closed, exit */
        }
    }
}