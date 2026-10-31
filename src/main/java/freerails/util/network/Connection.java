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

import freerails.util.Utils;

import java.io.*;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private static final long MAX_DEPTH = 100;
    private static final long MAX_REFERENCES = 1_000_000;
    private static final long MAX_STREAM_BYTES = 64L * 1024 * 1024;
    private static final long MAX_ARRAY_LENGTH = 1_000_000;
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
            in.setObjectInputFilter(Connection::filterInput);
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
     * Applies size and complexity limits, then allows only protocol and
     * standard value/container classes during deserialization.
     *
     * @param info metadata for the object currently being read
     * @return the filter decision for the object
     */
    private static ObjectInputFilter.Status filterInput(ObjectInputFilter.FilterInfo info) {
        if (info.depth() > MAX_DEPTH || info.references() > MAX_REFERENCES
                || info.streamBytes() > MAX_STREAM_BYTES
                || info.arrayLength() > MAX_ARRAY_LENGTH) {
            return ObjectInputFilter.Status.REJECTED;
        }

        Class<?> type = info.serialClass();
        if (type == null) {
            return ObjectInputFilter.Status.UNDECIDED;
        }
        return isAllowedProtocolClass(type)
                ? ObjectInputFilter.Status.ALLOWED
                : ObjectInputFilter.Status.REJECTED;
    }

    /**
     * Determines whether a serialized class belongs to the supported protocol
     * type set.
     *
     * @param type class being checked
     * @return {@code true} if the class is permitted by the input filter
     */
    private static boolean isAllowedProtocolClass(Class<?> type) {
        if (type.isArray()) {
            Class<?> component = type.getComponentType();
            return component == Object.class || component == Map.Entry.class || component.isPrimitive()
                    || isAllowedProtocolClass(component);
        }
        if (Proxy.isProxyClass(type)) {
            return false;
        }
        String name = type.getName();
        if (name.startsWith("freerails.")) {
            return true;
        }
        if (type.isEnum()) {
            return name.startsWith("freerails.");
        }
        if (type == String.class || type == Boolean.class || type == Byte.class
                || type == Short.class || type == Integer.class || type == Long.class
                || type == Float.class || type == Double.class || type == Character.class
                || type == Enum.class || type == Number.class || type == BigInteger.class
                || type == BigDecimal.class) {
            return true;
        }
        if (type == ArrayList.class || type == java.util.LinkedList.class
                || type == java.util.HashMap.class || type == java.util.LinkedHashMap.class
                || type == java.util.TreeMap.class || type == java.util.HashSet.class
                || type == java.util.LinkedHashSet.class || type == java.util.TreeSet.class
                || type == java.util.Vector.class || type == java.util.Hashtable.class
                || name.equals("java.util.Arrays$ArrayList")) {
            return true;
        }
        return name.startsWith("java.util.Collections$")
                || name.startsWith("java.util.ImmutableCollections$")
                || name.equals("java.util.CollSer")
                || name.equals("java.util.HashMap$Node")
                || name.equals("java.util.LinkedHashMap$Entry")
                || name.equals("java.util.TreeMap$Entry");
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
        out.writeObject(object);
        out.reset();
        out.flush();
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
