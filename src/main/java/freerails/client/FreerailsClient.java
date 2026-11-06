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

package freerails.client;

import freerails.client.launcher.LauncherFrame;
import freerails.move.Move;
import freerails.move.MovePrecommitter;
import freerails.move.Status;
import freerails.move.TryMoveStatus;
import freerails.move.generator.MoveGenerator;
import freerails.network.*;
import freerails.network.command.*;
import freerails.client.receiver.MoveChainFork;
import freerails.client.receiver.UntriedMoveReceiver;
import freerails.model.world.World;
import freerails.model.GameModel;
import freerails.util.network.Connection;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A client for FreerailsGameServer.
 */
public class FreerailsClient implements ClientControlInterface, GameModel, UntriedMoveReceiver, ServerCommandReceiver {

    private static final TaggedLogger logger = Logger.tag("FreerailsClient");
    private final Map<ClientProperty, Serializable> properties = new HashMap<>();
    private final MoveChainFork moveChainFork = new MoveChainFork();
    protected Connection connectionToServer;
    private World world;
    private MovePrecommitter movePrecommitter;
    private boolean readerFailureReported;

    /**
     * Creates a freerails client instance.
     */
    public FreerailsClient() {}

    /**
     * Returns the move fork.
     *
     * @return the move fork produced by this method
     */
    protected final MoveChainFork getMoveFork() {
        return moveChainFork;
    }

    /**
     * Connects this client to a remote server.
     */
    public final LogOnResponse connect(String address, int port, String username, String password) {
        logger.debug("Connect to remote server.  " + address + ':' + port);

        try {
            connectionToServer = Connection.make(new InetSocketAddress(address, port));
            readerFailureReported = false;
        } catch (IOException e) {
            return new LogOnResponse(false, e.getMessage());
        }

        try {
            Serializable request = new LogOnCredentials(username, password);
            connectionToServer.sendObject(request);

            return (LogOnResponse) connectionToServer.receiveObject();
        } catch (Exception e) {
            connectionToServer.close();
            return new LogOnResponse(false, e.getMessage());
        }
    }

    /**
     * Connects this client to a local server.
     */
    public final LogOnResponse connect(String username, String password) {
        try {
            Serializable request = new LogOnCredentials(username, password);
            connectionToServer = Connection.make(new InetSocketAddress(InetAddress.getLoopbackAddress(), 55000)); // TODO get selected port
            readerFailureReported = false;
            connectionToServer.sendObject(request);

            return (LogOnResponse) connectionToServer.receiveObject();
        } catch (Exception e) {
            connectionToServer.close();
            return new LogOnResponse(false, e.getMessage());
        }
    }

    /**
     * Disconnect the client from the server.
     */
    public final void disconnect() {
        if (connectionToServer != null) {
            connectionToServer.close();
        }
    }

    /**
     * Sets the game model.
     * @param world the world
     */
    @Override
    public final void setGameModel(World world) {
        this.world = world;
        movePrecommitter = new MovePrecommitter(this.world);
        newWorld(this.world);
    }

    /**
     * Subclasses should override this method if they need to respond to the
     * world being changed.
     */
    protected void newWorld(World world) {}

    /**
     * Sets the property.
     * @param property the property
     * @param value the value
     */
    @Override
    public void setProperty(ClientProperty property, Serializable value) {
        properties.put(property, value);
    }

    /**
     * Returns the property.
     *
     * @param property the client property to access
     * @return the property produced by this method
     */
    public final Serializable getProperty(ClientProperty property) {
        return properties.get(property);
    }

    /**
     * Reads the next object received from the server.
     * @return the received object
     */
    public final Serializable read() {
        return connectionToServer.receiveObject();
    }

    /**
     * Write.
     * @param fs the fs
     */
    public final void write(Serializable fs) {
        try {
            connectionToServer.sendObject(fs);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Reads and deals with all outstanding messages from the server.
     */
    @Override
    public final void update() {
        try {
            List<Serializable> messages = connectionToServer.getReceivedObjects();

            for (Serializable message : messages) {
                processMessage(message);
            }

            IOException readerFailure = connectionToServer.getReaderFailure();
            if (readerFailure != null && !readerFailureReported) {
                readerFailureReported = true;
                logger.warn(readerFailure, "Connection to server closed after a reader failure.");
            }

            clientUpdates();
        } catch (IOException e) {
            onUpdateFailure(e);
        }
    }

    /**
     * Handles a failure while reading or processing server updates.
     *
     * @param exception update failure
     */
    protected void onUpdateFailure(IOException exception) {
        logger.error(exception, "Could not update client from server.");
        LauncherFrame.emergencyStop();
    }

    /**
     * Empty method called by update(), subclasses should override this method
     * instead of overriding update().
     */
    protected void clientUpdates() {
    }

    /**
     * Processes a message received from the server. Called by update().
     */
    public final void processMessage(Serializable message) throws IOException {
        switch (message) {
            case CommandToClient request -> {
                CommandStatus status = request.execute(this);
                logger.debug(request.toString());

                connectionToServer.sendObject(status);
            }
            case Move move -> {
                movePrecommitter.fromServer(move);
                moveChainFork.process(move);
            }
            case Status status -> movePrecommitter.fromServer(status);
            case MoveGenerator moveGenerator -> {
                Move move = movePrecommitter.fromServer(moveGenerator);
                moveChainFork.process(move);
            }
            case TryMoveStatus tryMoveStatus -> movePrecommitter.fromServer(tryMoveStatus);
            default -> logger.debug(message.toString());
        }
    }

    /**
     * Returns the world.
     *
     * @return the world produced by this method
     */
    public final World getWorld() {
        return world;
    }

    /**
     * Sends move to the server.
     */
    @Override
    public final void process(Move move) {
        movePrecommitter.toServer(move);
        moveChainFork.process(move);
        write(move);
    }

    /**
     * Tests a move before sending it to the server.
     */
    @Override
    public final Status applicable(Move move) {
        return move.applicable(world);
    }

    /**
     * Performs the send command operation.
     *
     * @param message the message parameter
     */
    @Override
    public void sendCommand(CommandToServer message) {
        write(message);
    }

    /**
     * Performs the process move generator operation.
     *
     * @param moveGenerator the move generator parameter
     */
    @Override
    public void processMoveGenerator(MoveGenerator moveGenerator) {
        Move move = movePrecommitter.toServer(moveGenerator);
        moveChainFork.process(move);
        write(moveGenerator);
    }

    /**
     * Returns the last tick time.
     *
     * @return the last tick time produced by this method
     */
    protected long getLastTickTime() {
        return moveChainFork.getLastTickTime();
    }
}