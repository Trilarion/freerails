package freerails.util.network;

import freerails.util.Utils;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 */
public class EchoServer {

    private static long UPDATE_DURATION = 10; // not more than every 50 ms
    private volatile boolean running = false;
    private ServerSocketAcceptor serverSocketAcceptor;
    private final BlockingQueue<Socket> newlyConnectedSockets = new LinkedBlockingQueue<>();
    private final Collection<Connection> activeConnections = new LinkedList<>();
    private CountDownLatch signal;

    /**
     *
     * @return
     */
    public int getLocalPort() {
        if (!running) {
            throw new IllegalStateException("Server not running");
        }
        return serverSocketAcceptor.getLocalPort();
    }

    /**
     *
     * @throws IOException
     */
    public void startRunning(InetSocketAddress address) throws IOException {
        Utils.verifyNotNull(address);

        if (running) {
            throw new IllegalStateException("Already running.");
        }

        // setup a new ServerSocketAcceptor, which will start listening to new connections immediately
        serverSocketAcceptor = new ServerSocketAcceptor(address, newlyConnectedSockets);

        Thread thread = new Thread(() -> {
            running = true;
            signal.countDown();
            // the server loop
            while (running) {
                long startTime = System.currentTimeMillis();
                // check for new connections
                for (Socket socket: newlyConnectedSockets) {
                    newlyConnectedSockets.remove(socket);
                    Connection newConnection = null;
                    try {
                        newConnection = Connection.make(socket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    activeConnections.add(newConnection);
                }

                // remove all closed connections
                activeConnections.removeIf(connection -> !connection.isOpen());

                // read from all
                Collection<Serializable> messages = new LinkedList<>();
                for (Connection connection: activeConnections) {
                    messages.addAll(connection.getReceivedObjects());
                }

                // send to all
                for (Serializable message: messages) {
                    for (Connection connection: activeConnections) {
                        try {
                            connection.sendObject(message);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                // sleep for the rest of the interval
                try {
                    Thread.sleep(Long.max(UPDATE_DURATION - (System.currentTimeMillis() - startTime), 0));
                } catch (InterruptedException ignored) {};
            }
            // close all still open connections
            for (Connection connection: activeConnections) {
                try {
                    connection.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            // signal that we have done everything and will finish now
            signal.countDown();
        }, "Server");

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
    public boolean isRunning() {
        return running;
    }

    /**
     *
     */
    public void stopRunning() {
        if (!running) {
            return;
        }

        serverSocketAcceptor.close();
        signal = new CountDownLatch(1);
        running = false;

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
    public int getNumberActiveConnections() {
        return activeConnections.size();
    }

}
