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

package freerails.client;

import freerails.client.launcher.LauncherFrame;
import freerails.move.Move;
import freerails.move.MovePrecommitter;
import freerails.move.MoveStatus;
import freerails.move.TryMoveStatus;
import freerails.move.generator.MoveGenerator;
import freerails.network.*;
import freerails.server.GameServer;
import freerails.network.command.*;
import freerails.move.receiver.MoveChainFork;
import freerails.move.receiver.UntriedMoveReceiver;
import freerails.model.world.World;
import freerails.server.GameModel;
import freerails.model.player.Player;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A client for FreerailsGameServer.
 */
public class FreerailsClient implements ClientControlInterface, GameModel, UntriedMoveReceiver, ServerCommandReceiver {

    private static final Logger logger = Logger.getLogger(FreerailsClient.class.getName());
    private final Map<ClientProperty, Serializable> properties = new HashMap<>();
    private final MoveChainFork moveChainFork = new MoveChainFork();
    protected ConnectionToServer connectionToServer;
    private World world;
    private MovePrecommitter movePrecommitter;

    /**
     *
     */
    public FreerailsClient() {}

    /**
     * @return
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
            connectionToServer = new IpConnectionToServer(address, port);
        } catch (IOException e) {
            return new LogOnResponse(false, e.getMessage());
        }

        try {
            Serializable request = new LogOnCredentials(username, password);
            connectionToServer.writeToServer(request);

            return (LogOnResponse) connectionToServer.waitForObjectFromServer();
        } catch (Exception e) {
            try {
                connectionToServer.disconnect();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return new LogOnResponse(false, e.getMessage());
        }
    }

    /**
     * Connects this client to a local server.
     */
    public final LogOnResponse connect(GameServer server, String username, String password) {
        try {
            Serializable request = new LogOnCredentials(username, password);
            connectionToServer = new IpConnectionToServer("127.0.0.1", 55000); // TODO get selected port
            connectionToServer.writeToServer(request);

            return (LogOnResponse) connectionToServer.waitForObjectFromServer();
        } catch (Exception e) {
            try {
                connectionToServer.disconnect();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return new LogOnResponse(false, e.getMessage());
        }
    }

    /**
     * Disconnect the client from the server.
     */
    public final void disconnect() {
        try {
            connectionToServer.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public void setProperty(ClientProperty property, Serializable value) {
        properties.put(property, value);
    }

    /**
     * @param property
     * @return
     */
    public final Serializable getProperty(ClientProperty property) {
        return properties.get(property);
    }

    public final Serializable read() {
        try {
            return connectionToServer.waitForObjectFromServer();
        } catch (IOException | InterruptedException e) {
        }

        throw new IllegalStateException();
    }

    public final void write(Serializable fs) {
        try {
            connectionToServer.writeToServer(fs);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    /**
     * Reads and deals with all outstanding messages from the server.
     */
    public final void update() {
        try {
            Serializable[] messages = connectionToServer.readFromServer();

            for (Serializable message : messages) {
                processMessage(message);
            }

            clientUpdates();
        } catch (IOException e) {
            LauncherFrame.emergencyStop();
        }
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
        if (message instanceof CommandToClient) {
            CommandToClient request = (CommandToClient) message;
            CommandStatus status = request.execute(this);
            logger.debug(request.toString());

            connectionToServer.writeToServer(status);
        } else if (message instanceof Move) {
            Move move = (Move) message;
            movePrecommitter.fromServer(move);
            moveChainFork.process(move);
        } else if (message instanceof MoveStatus) {
            MoveStatus moveStatus = (MoveStatus) message;
            movePrecommitter.fromServer(moveStatus);
        } else if (message instanceof MoveGenerator) {
            MoveGenerator moveGenerator = (MoveGenerator) message;
            Move move = movePrecommitter.fromServer(moveGenerator);
            moveChainFork.process(move);
        } else if (message instanceof TryMoveStatus) {
            TryMoveStatus tryMoveStatus = (TryMoveStatus) message;
            movePrecommitter.fromServer(tryMoveStatus);
        } else {
            logger.debug(message.toString());
        }
    }

    /**
     * @return
     */
    public final World getWorld() {
        return world;
    }

    /**
     * Sends move to the server.
     */
    public final void process(Move move) {
        movePrecommitter.toServer(move);
        moveChainFork.process(move);
        write(move);
    }

    /**
     * Tests a move before sending it to the server.
     */
    public final MoveStatus tryDoMove(Move move) {
        return move.tryDoMove(world, Player.AUTHORITATIVE);
    }

    /**
     * @param message
     */
    public void sendCommand(CommandToServer message) {
        write(message);
    }

    /**
     * @param moveGenerator
     */
    public void processMoveGenerator(MoveGenerator moveGenerator) {
        Move move = movePrecommitter.toServer(moveGenerator);
        moveChainFork.process(move);
        write(moveGenerator);
    }

    /**
     * @return
     */
    protected long getLastTickTime() {
        return moveChainFork.getLastTickTime();
    }
}