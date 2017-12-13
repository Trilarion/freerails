/*
 * Copyright (C) Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jfreerails.controller;

import java.io.IOException;
import jfreerails.world.top.World;


/**
 * This interface represents a connection between a server and a client. This class
 * should be subclassed to provide connections over different transport media.
 *
 * The connection is responsible for guaranteeing the delivery of moves across
 * the transport medium.
 *
 * TODO eventually this class will be simplified so that it's function is only
 * to send FreerailsSerializable objects across the connection. The ulitmate
 * goal is for this class not to implement MoveReceiver and to have a sendMove()
 * method or similar.
 *
 * @author lindsal
 */
public interface ConnectionToServer extends UncommittedMoveReceiver {
    public void addMoveReceiver(SourcedMoveReceiver moveReceiver);

    public void removeMoveReceiver(SourcedMoveReceiver moveReceiver);

    public World loadWorldFromServer() throws IOException;

    /**
     * close the connection to the remote peer
     */
    public void close();

    /**
     * connect to the remote peer
     */
    public void open() throws IOException;

    public void addConnectionListener(ConnectionListener l);

    public void removeConnectionListener(ConnectionListener l);

    public void flush();

    public static class ConnectionState {
        private String state;

        /**
         * Waiting - connection has been opened, but client has not been
         * initialised with the World DB.
         */
        public static final ConnectionState WAITING = new ConnectionState(
                "Waiting");

        /**
         * Initialising - client has requested world, but it has not been sent
         * yet.
         */
        public static final ConnectionState INITIALISING = new ConnectionState(
                "Initialising");

        /**
         * Ready - client has received world and is ready to receive moves.
         */
        public static final ConnectionState READY = new ConnectionState("Ready");

        /**
         * Closed - the connection is closed.
         */
        public static final ConnectionState CLOSED = new ConnectionState(
                "Closed");

        private ConnectionState(String aState) {
            state = aState;
        }

        public String toString() {
            return state;
        }
    }

    /**
     * @return the current state of this connection
     */
    public ConnectionState getConnectionState();

    public void sendCommand(ServerCommand s);
}