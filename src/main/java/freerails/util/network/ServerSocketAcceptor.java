package freerails.util.network;

import freerails.util.Utils;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
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
