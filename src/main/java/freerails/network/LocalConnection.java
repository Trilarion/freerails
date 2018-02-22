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

import freerails.util.SychronizedQueue;
import freerails.util.SynchronizedFlag;

import java.io.IOException;
import java.io.Serializable;

// TODO can we not get rid of this and use local IP connection with localhost (127.0.0.1) instead? untangle FreerailsClient before
/**
 * A connection between the a client and server in the same JVM.
 */
public class LocalConnection implements ConnectionToClient, ConnectionToServer {

    public static final String LOCAL_SERVER_DESCRIPTION = "Local server in the same JVM";
    private final SychronizedQueue fromServer = new SychronizedQueue();
    private final SychronizedQueue fromClient = new SychronizedQueue();
    private final SynchronizedFlag openFlag = new SynchronizedFlag(true);

    public Serializable[] readFromClient() throws IOException {
        if (openFlag.isSet()) {
            return fromClient.read();
        }
        throw new IOException();
    }

    public Serializable waitForObjectFromClient() throws IOException, InterruptedException {
        synchronized (fromClient) {
            if (fromClient.size() == 0) {
                fromClient.wait();
            }

            if (openFlag.isSet()) {
                return fromClient.getFirst();
            }
            throw new IOException();
        }
    }

    public void writeToClient(Serializable object) throws IOException {
        if (openFlag.isSet()) {
            synchronized (fromServer) {
                fromServer.write(object);
                fromServer.notifyAll();
            }
        } else {
            throw new IOException();
        }
    }

    public Serializable[] readFromServer() throws IOException {
        if (openFlag.isSet()) {
            return fromServer.read();
        }
        throw new IOException();
    }

    public Serializable waitForObjectFromServer() throws IOException, InterruptedException {
        if (openFlag.isSet()) {
            synchronized (fromServer) {
                if (fromServer.size() == 0) {
                    fromServer.wait();
                }

                return fromServer.getFirst();
            }
        }
        throw new IOException();
    }

    public void writeToServer(Serializable object) throws IOException {
        if (openFlag.isSet()) {
            synchronized (fromClient) {
                fromClient.write(object);
                fromClient.notifyAll();
            }
        } else {
            throw new IOException();
        }
    }

    public boolean isOpen() {
        return openFlag.isSet();
    }

    public synchronized void disconnect() {
        openFlag.unset();
    }

    /**
     * @return
     */
    public String getServerDetails() {
        return LOCAL_SERVER_DESCRIPTION;
    }
}