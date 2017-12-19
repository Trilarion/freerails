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

import freerails.world.FreerailsSerializable;

import java.io.IOException;

/**
 * A connection between the a client and server in the same JVM.
 *
 */
public class LocalConnection implements ConnectionToClient, ConnectionToServer {

    /**
     *
     */
    public static final String SERVER_IN_SAME_JVM = "server in same JVM";

    private final SychronizedQueue fromServer = new SychronizedQueue();

    private final SychronizedQueue fromClient = new SychronizedQueue();

    private final SynchronizedFlag status = new SynchronizedFlag(true);

    public FreerailsSerializable[] readFromClient() throws IOException {
        if (status.isOpen()) {
            return fromClient.read();
        }
        throw new IOException();
    }

    public FreerailsSerializable waitForObjectFromClient() throws IOException,
            InterruptedException {
        synchronized (fromClient) {
            if (fromClient.size() == 0) {
                fromClient.wait();
            }

            if (status.isOpen()) {
                return fromClient.getFirst();
            }
            throw new IOException();
        }
    }

    public void writeToClient(FreerailsSerializable object) throws IOException {
        if (status.isOpen()) {
            synchronized (fromServer) {
                fromServer.write(object);
                fromServer.notifyAll();
            }
        } else {
            throw new IOException();
        }
    }

    public FreerailsSerializable[] readFromServer() throws IOException {
        if (status.isOpen()) {
            return fromServer.read();
        }
        throw new IOException();
    }

    public FreerailsSerializable waitForObjectFromServer() throws IOException,
            InterruptedException {
        if (status.isOpen()) {
            synchronized (fromServer) {
                if (fromServer.size() == 0) {
                    fromServer.wait();
                }

                return fromServer.getFirst();
            }
        }
        throw new IOException();
    }

    public void writeToServer(FreerailsSerializable object) throws IOException {
        if (status.isOpen()) {
            synchronized (fromClient) {
                fromClient.write(object);
                fromClient.notifyAll();
            }
        } else {
            throw new IOException();
        }
    }

    public boolean isOpen() {
        return status.isOpen();
    }

    public void flush() {
        // No need to do anything.
    }

    public synchronized void disconnect() {
        status.close();
    }

    /**
     *
     * @return
     */
    public String getServerDetails() {
        return SERVER_IN_SAME_JVM;
    }
}