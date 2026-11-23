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

import freerails.util.SerializationFilter;
import freerails.util.Utils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Sends serializable FreeRails protocol objects over a socket and queues
 * objects received by a dedicated reader thread.
 */
public class Connection implements AutoCloseable {

    private static final SerializationFilter INPUT_FILTER =
            new SerializationFilter(100, 1_000_000, 64L * 1024 * 1024, 1_000_000);
    private static final int MAX_QUEUED_MESSAGES = 32;

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final BlockingQueue<Serializable> receivedObjects =
            new LinkedBlockingQueue<>(MAX_QUEUED_MESSAGES);
    private final Thread readerThread;
    private volatile boolean open;
    private volatile IOException readerFailure;

    /**
     * Initializes object streams and starts the inbound reader for a connected socket.
     *
     * @param socket connected socket to wrap
     * @throws IOException if the streams cannot be initialized
     * @throws IllegalArgumentException if the socket is not open and connected
     */
    private Connection(Socket socket) throws IOException {
        Utils.verifyNotNull(socket);
        if (!socket.isConnected() || socket.isClosed()) {
            throw new IllegalArgumentException("Socket is not open or connected.");
        }
        this.socket = socket;

        try {
            OutputStream outputStream = new DeflaterOutputStream(socket.getOutputStream(), true);
            out = new ObjectOutputStream(outputStream);
            out.flush();

            InputStream inputStream = new InflaterInputStream(socket.getInputStream());
            in = new ObjectInputStream(inputStream);
            in.setObjectInputFilter(INPUT_FILTER);
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException closeException) {
                e.addSuppressed(closeException);
            }
            throw e;
        }

        readerThread = new Thread(this::readMessages, "Connection Inbound Message Reader");
        readerThread.setDaemon(true);
        open = true;
        readerThread.start();
    }

    /**
     * Reads serialized objects until the connection closes and places them
     * on the inbound queue.
     */
    private void readMessages() {
        try {
            while (open) {
                Object object = in.readObject();
                if (!(object instanceof Serializable serializable)) {
                    throw new InvalidClassException("Received object is not serializable.");
                }
                queueMessage(serializable);
            }
        } catch (EOFException e) {
            // Socket closure does not finish the compressed stream.
        } catch (SocketException e) {
            if (open) {
                readerFailure = e;
            }
        } catch (IOException | ClassNotFoundException e) {
            if (open) {
                readerFailure = e instanceof IOException ioException
                        ? ioException : new IOException("Could not deserialize inbound message.", e);
            }
        } finally {
            open = false;
            try {
                socket.close();
            } catch (IOException e) {
                if (readerFailure == null) {
                    readerFailure = e;
                }
            }
        }
    }

    /**
     * Adds an inbound message to the bounded queue, waiting while it is full.
     *
     * @param message serializable message to queue
     * @throws IOException if waiting is interrupted
     */
    private void queueMessage(Serializable message) throws IOException {
        while (open) {
            try {
                if (receivedObjects.offer(message, 100, TimeUnit.MILLISECONDS)) {
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while queueing an inbound message.", e);
            }
        }
    }

    /**
     * Wraps an already connected socket in a message connection.
     *
     * @param socket connected socket
     * @return connection using the socket
     * @throws IOException if stream setup fails
     * @throws NullPointerException if {@code socket} is null
     * @throws IllegalArgumentException if the socket is not open and connected
     */
    public static Connection make(Socket socket) throws IOException {
        Utils.verifyNotNull(socket);
        return new Connection(socket);
    }

    /**
     * Connects to an address and creates a message connection.
     *
     * @param address remote socket address
     * @return connected message connection
     * @throws IOException if connecting or stream setup fails
     * @throws NullPointerException if {@code address} is null
     */
    public static Connection make(InetSocketAddress address) throws IOException {
        Utils.verifyNotNull(address);
        Socket socket = new Socket();
        try {
            socket.connect(address);
            return make(socket);
        } catch (IOException | RuntimeException e) {
            try {
                socket.close();
            } catch (IOException closeException) {
                e.addSuppressed(closeException);
            }
            throw e;
        }
    }

    /**
     * Removes and returns all messages currently queued by the reader thread.
     *
     * @return messages available at the time of the call, in receive order
     */
    public List<Serializable> getReceivedObjects() {
        List<Serializable> objects = new ArrayList<>();
        receivedObjects.drainTo(objects);
        return objects;
    }

    /**
     * Waits for the next message.
     *
     * @return the next message
     * @throws IllegalStateException if the connection closes before another
     *                                message arrives or reading fails
     */
    public Serializable receiveObject() {
        while (true) {
            try {
                Serializable object = receivedObjects.poll(100, TimeUnit.MILLISECONDS);
                if (object != null) {
                    return object;
                }
                if (!open) {
                    if (readerFailure != null) {
                        throw new IllegalStateException("Failed to read from connection.", readerFailure);
                    }
                    throw new IllegalStateException("Connection closed before a message arrived.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for a message.", e);
            }
        }
    }

    /**
     * Sends one serializable object over the connection.
     *
     * <p>If writing fails, the object stream may hold a partially written
     * message, so the connection is closed before the failure is rethrown.
     *
     * @param object message to send
     * @throws IOException if writing to the socket fails
     * @throws NullPointerException if {@code object} is null
     * @throws IllegalStateException if the connection is closed
     */
    public synchronized void sendObject(Serializable object) throws IOException {
        Utils.verifyNotNull(object, "object");
        if (!open) {
            throw new IllegalStateException("Connection is not open");
        }
        try {
            out.writeObject(object);
            // Required: without a reset the stream sends a back-reference for any
            // object it has written before, so the peer would receive its stale
            // earlier copy instead of the current state, and both ends would
            // retain every object ever sent.
            out.reset();
            out.flush();
        } catch (IOException | RuntimeException e) {
            closeAfterSendFailure(e);
            throw e;
        }
    }

    /**
     * Closes the connection after a failed send, attaching any close failure
     * to the original exception.
     *
     * @param failure exception that interrupted the send
     */
    private void closeAfterSendFailure(Exception failure) {
        try {
            close();
        } catch (IllegalStateException closeFailure) {
            failure.addSuppressed(closeFailure);
        }
    }

    /**
     * Indicates whether the socket connection is still open.
     *
     * @return {@code true} while the connection remains open
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Returns an unexpected reader failure, or {@code null} if none occurred.
     *
     * @return recorded reader failure, if any
     */
    public IOException getReaderFailure() {
        return readerFailure;
    }

    /**
     * Closes the socket and waits for the inbound reader thread to finish.
     *
     * @throws IllegalStateException if closing fails or the caller is
     * interrupted while waiting
     */
    @Override
    public void close() {
        open = false;
        IllegalStateException closeFailure = null;
        try {
            socket.close();
        } catch (IOException e) {
            closeFailure = new IllegalStateException("Could not close connection socket.", e);
        }
        if (Thread.currentThread() != readerThread) {
            try {
                readerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                IllegalStateException interrupted =
                        new IllegalStateException("Interrupted while closing connection.", e);
                if (closeFailure != null) {
                    interrupted.addSuppressed(closeFailure);
                }
                throw interrupted;
            }
        }
        if (closeFailure != null) {
            throw closeFailure;
        }
    }
}
