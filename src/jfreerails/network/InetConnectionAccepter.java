/*
 * Created on Apr 13, 2004
 */
package jfreerails.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;


/**
 * When this class is run in a thread it accepts new connections to its Server Socket
 *  and adds them to the NewGameServer that was passed to its constructor.
 *
 * @author Luke
 */
public class InetConnectionAccepter implements Runnable {
    private static final Logger logger = Logger.getLogger(InetConnectionAccepter.class.getName());

    public static void main(String[] args) {
        try {
            NewGameServer echoGameServer = EchoGameServer.startServer();
            InetConnectionAccepter accepter = new InetConnectionAccepter(6666,
                    echoGameServer);
            Thread t = new Thread(accepter);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final NewGameServer gameServer;
    private final SynchronizedFlag keepRunning = new SynchronizedFlag(true);
    private final ServerSocket serverSocket;

    public InetConnectionAccepter(int port, NewGameServer gameServer)
        throws IOException {
        this.gameServer = gameServer;
        serverSocket = new ServerSocket(port);
    }

    public void run() {
        Thread.currentThread().setName("InetConnectionAccepter, port " +
            serverSocket.getLocalPort());

        try {
            logger.fine("Accepting connections on port " +
                serverSocket.getLocalPort());

            while (isKeepRunning()) {
                Socket socket = serverSocket.accept();
                logger.fine("Incoming connection from " +
                    socket.getRemoteSocketAddress());

                synchronized (this) {
                    synchronized (gameServer) {
                        InetConnection2Client connection = new InetConnection2Client(socket);
                        gameServer.addConnection(connection);
                    }
                }
            }
        } catch (IOException e) {
            try {
                stop();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    public synchronized void stop() throws IOException {
        this.keepRunning.close();
        serverSocket.close();

        //Commented out since it causes execeptions to be thrown, fixes bug 979831         
        //gameServer.stop();
    }

    private boolean isKeepRunning() {
        return keepRunning.isOpen();
    }

    public int getLocalPort() {
        return serverSocket.getLocalPort();
    }
}