/*
 * Created on Apr 13, 2004
 */
package jfreerails.controller.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * When run in a thread this class accepts new connections to its ServerSocket and adds them to the
 * NewGameServer that was passed to its constructor.
 *
 * @author Luke
 */
public class InetConnectionAccepter implements Runnable {
    public static void main(String[] args) {
        try {
            NewGameServer echoGameServer = EchoGameServer.startServer();
            InetConnectionAccepter InetServer = new InetConnectionAccepter(6666,
                    echoGameServer);
            Thread t = new Thread(InetServer);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final NewGameServer gameServer;
    private final SynchronizedFlag keepRunning = new SynchronizedFlag(true);
    private final ServerSocket serverSocket;

    InetConnectionAccepter(int port, NewGameServer gameServer)
        throws IOException {
        this.gameServer = gameServer;
        serverSocket = new ServerSocket(port);
    }

    public void run() {
        Thread.currentThread().setName("InetConnectionAccepter");

        try {
            while (isKeepRunning()) {
                Socket socket = serverSocket.accept();

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
}