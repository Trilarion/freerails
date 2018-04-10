/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
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

package freerails.network;

import freerails.util.Utils;

import java.io.*;
import java.net.Socket;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Provides methods send serializable objects over the Internet, and connect and disconnect gracefully.
 */
class IpConnection {

    private static final String CONNECTION_OPEN = "CONNECTION_OPEN";
    private final Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    IpConnection(Socket socket) {
        Utils.verifyNotNull(socket);
        if (!socket.isConnected() || socket.isClosed()) {
            throw new IllegalArgumentException();
        }
        this.socket = socket;
    }

    IpConnection(String host, int port) throws IOException {
        this(new Socket(host, port));
    }

    /**
     * Sets up the input and output streams, then sends the String
     * "CONNECTION_OPEN" and attempts to read the same String back.
     */
    synchronized void initialize() throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream, true);
        objectOutputStream = new ObjectOutputStream(deflaterOutputStream);
        objectOutputStream.writeObject(CONNECTION_OPEN);
        objectOutputStream.flush();

        InputStream inputStream = socket.getInputStream();

        InflaterInputStream inflaterInputStream = new InflaterInputStream(inputStream);
        objectInputStream = new ObjectInputStream(inflaterInputStream);

        try {
            String text = (String) objectInputStream.readObject();

            if (!text.equals(CONNECTION_OPEN)) {
                throw new IllegalStateException(String.format("Received %s, expected %s.", text, CONNECTION_OPEN));
            }
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    synchronized void send(Serializable object) throws IOException {
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
    }

    Serializable receive() throws IOException, ClassNotFoundException {
        Object object = objectInputStream.readObject();

        return (Serializable) object;
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