/*
 * Created on Apr 13, 2004
 */
package jfreerails.controller.net;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import jfreerails.world.common.FreerailsSerializable;


/**
 * @author Luke
 *
 */
class NewInetConnection {
    private final Socket socket;
    private DeflaterOutputStream deflaterOutputStream;
    private InflaterInputStream inflaterInputStream;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private static final String CONNECTION_OPEN = "CONNECTION_OPEN";

    NewInetConnection(Socket acceptedConnection) throws IOException {
        socket = acceptedConnection;
    }

    NewInetConnection(String s, int port) throws IOException {
        this(new Socket(s, port));
    }

    /**
     * Sets up the input and output streams, then sends the String
     * "CONNECTION_OPEN" and attempts to read the same String back.
     */
    synchronized void open() throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        deflaterOutputStream = new DeflaterOutputStream(outputStream);
        objectOutputStream = new ObjectOutputStream(deflaterOutputStream);
        objectOutputStream.writeObject(CONNECTION_OPEN);
        objectOutputStream.flush();

        InputStream inputStream = socket.getInputStream();
        inflaterInputStream = new InflaterInputStream(inputStream);
        objectInputStream = new ObjectInputStream(inflaterInputStream);

        try {
            String s = (String)objectInputStream.readObject();

            if (!s.equals(CONNECTION_OPEN)) {
                throw new IllegalStateException(s);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    synchronized void send(FreerailsSerializable object)
        throws IOException {
        objectOutputStream.writeObject(object);
        flush();
    }

    FreerailsSerializable receive() throws IOException, ClassNotFoundException {
        Object object = objectInputStream.readObject();

        return (FreerailsSerializable)object;
    }

    synchronized boolean isOpen() {
        boolean isClosed = socket.isClosed();

        return !isClosed;
    }

    synchronized void flush() throws IOException {
        objectOutputStream.flush();
        deflaterOutputStream.flush();
        deflaterOutputStream.finish();
        deflaterOutputStream.flush();
    }

    synchronized void shutdownOutput() throws IOException {
        socket.shutdownOutput();

        if (socket.isInputShutdown() && socket.isOutputShutdown()) {
            socket.close();
        }
    }

    synchronized void shutdownInput() throws IOException {
        socket.shutdownInput();

        if (socket.isInputShutdown() && socket.isOutputShutdown()) {
            socket.close();
        }
    }
}