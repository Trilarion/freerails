package freerails.util.network;

import freerails.util.Utils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Wrapper around a Socket and ObjectInput/OutputStreams so that Serializable objects
 * can be sent and read. The reading is done within a Thread that continually listens
 * for incoming objects.
 */
public class Connection {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private volatile boolean open = false;
    // blocking queue with capacity of Integer.MAX_VALUE
    private final BlockingQueue<Serializable> receivedObjects = new LinkedBlockingQueue<>();
    private CountDownLatch signal;

    /**
     *
     * @param socket
     * @throws IOException
     */
    private Connection(Socket socket) throws IOException {

        Utils.verifyNotNull(socket);
        if (!socket.isConnected() || socket.isClosed()) {
            throw new IllegalArgumentException("Socket is not open or connected.");
        }
        this.socket = socket;

        // connect streams output before input
        OutputStream outputStream = socket.getOutputStream();
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream, true);
        out = new ObjectOutputStream(deflaterOutputStream);
        // otherwise the header is not sent and new ObjectInputStream blocks
        out.flush();

        InputStream inputStream = socket.getInputStream();
        InflaterInputStream inflaterInputStream = new InflaterInputStream(inputStream);
        // ObjectInputStream will block until the other side constructs the output stream
        // https://docs.oracle.com/javase/9/docs/api/java/io/ObjectInputStream.html#ObjectInputStream--
        in = new ObjectInputStream(inflaterInputStream);

        // thread that reads incoming objects and puts them in a blocking queue
        Thread thread = new Thread(() -> {
            open = true;
            signal.countDown();
            while (open) {
                try {
                    // read new object (blocks unless socket is closed)
                    Serializable object = (Serializable) in.readObject();

                    // add to queue (doesn't block)
                    receivedObjects.add(object);
                } catch (EOFException e) {
                    // we cannot read anymore, the other side must have closed the connection
                    close();
                } catch (SocketException e ) {
                    // close was called from outside
                    assert open == false;
                } catch (IOException | ClassNotFoundException e) {
                    // unexpected
                    throw new RuntimeException(e);
                }
            }
            signal.countDown();
        }, "Connection Inbound Message Reader");

        // start the thread
        signal = new CountDownLatch(1);
        thread.start();

        // wait until thread is running
        try {
            signal.await();
        } catch (InterruptedException e) {
            // unexpected
            throw new RuntimeException(e);
        }
    }

    public static Connection make(Socket socket) throws IOException{
        Utils.verifyNotNull(socket);
        Connection connection = new Connection(socket);
        return connection;
    }

    /**
     *
     * @param address
     * @return
     * @throws IOException
     */
    public static Connection make(InetSocketAddress address) throws IOException {
        Utils.verifyNotNull(address);
        Socket socket = new Socket(address.getAddress(), address.getPort());
        return make(socket);
    }

    /**
     *
     * This does not block.
     *
     * @return
     */
    public List<Serializable> getReceivedObjects() {
        List<Serializable> objects = new LinkedList<>();
        for (Serializable object: receivedObjects) {
            receivedObjects.remove(object);
            objects.add(object);
        }
        return objects;
    }

    /**
     * This blocks.
     *
     * @return
     */
    public Serializable receiveObject() {
        try {
            return receivedObjects.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param object
     * @throws IOException
     */
    public synchronized void sendObject(Serializable object) throws IOException {
        if (!open) {
            throw new IllegalStateException("Connection is not open");
        }
        out.writeObject(object);
        out.flush();
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
        open = false;
        signal = new CountDownLatch(1);
        try {
            socket.close();
            // wait until the thread has ended
            signal.await();
        } catch (IOException | InterruptedException e) {
            // unexpected
            throw new RuntimeException(e);
        }
    }
}
