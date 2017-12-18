/*
 * Created on Apr 13, 2004
 */
package freerails.network;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * When this class is run in a thread it accepts new connections to its Server
 * Socket and adds them to the NewGameServer that was passed to its constructor.
 *
 * @author Luke
 */
public class InetConnectionAccepter implements Runnable {
    private static final Logger logger = Logger
            .getLogger(InetConnectionAccepter.class.getName());
    private final GameServer gameServer;
    private final SynchronizedFlag keepRunning = new SynchronizedFlag(true);
    private final ServerSocket serverSocket;

    /**
     *
     * @param port
     * @param gameServer
     * @throws IOException
     */
    public InetConnectionAccepter(int port, GameServer gameServer)
            throws IOException {
        if (null == gameServer)
            throw new NullPointerException();
        this.gameServer = gameServer;
        serverSocket = new ServerSocket(port);
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            GameServer echoGameServer = EchoGameServer.startServer();
            InetConnectionAccepter accepter = new InetConnectionAccepter(6666,
                    echoGameServer);
            Thread t = new Thread(accepter);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Thread.currentThread().setName(
                "InetConnectionAccepter, port " + serverSocket.getLocalPort());

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Accepting connections on port "
                        + serverSocket.getLocalPort());
            }

            while (isKeepRunning()) {
                Socket socket = serverSocket.accept();
                if (logger.isDebugEnabled()) {
                    logger.debug("Incoming connection from "
                            + socket.getRemoteSocketAddress());
                }

                synchronized (this) {
                    synchronized (gameServer) {
                        InetConnection2Client connection = new InetConnection2Client(
                                socket);
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

    /**
     *
     * @throws IOException
     */
    public synchronized void stop() throws IOException {
        this.keepRunning.close();
        serverSocket.close();

        // Commented out since it causes exceptions to be thrown, fixes bug
        // 979831
        // gameServer.stop();
    }

    private boolean isKeepRunning() {
        return keepRunning.isOpen();
    }

    /**
     *
     * @return
     */
    public int getLocalPort() {
        return serverSocket.getLocalPort();
    }
}