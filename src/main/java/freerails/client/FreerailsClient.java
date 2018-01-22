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

import freerails.client.launcher.Launcher;
import freerails.controller.*;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.move.PreMove;
import freerails.network.*;
import freerails.world.FreerailsMutableSerializable;
import freerails.world.World;
import freerails.world.game.GameModel;
import freerails.world.player.Player;
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
    private final Map<String, Serializable> properties = new HashMap<>();
    private final MoveChainFork moveFork = new MoveChainFork();;
    protected ConnectionToServer connectionToServer;
    private World world;
    private MovePrecommitter committer;

    /**
     *
     */
    public FreerailsClient() {}

    /**
     * @return
     */
    protected final MoveChainFork getMoveFork() {
        return moveFork;
    }

    /**
     * Connects this client to a remote server.
     */
    public final LogOnResponse connect(String address, int port, String username, String password) {
        logger.debug("Connect to remote server.  " + address + ':' + port);

        try {
            connectionToServer = new InetConnectionToServer(address, port);
        } catch (IOException e) {
            return LogOnResponse.rejected(e.getMessage());
        }

        try {
            Serializable request = new LogOnRequest(username, password);
            connectionToServer.writeToServer(request);

            return (LogOnResponse) connectionToServer.waitForObjectFromServer();
        } catch (Exception e) {
            try {
                connectionToServer.disconnect();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return LogOnResponse.rejected(e.getMessage());
        }
    }

    /**
     * Connects this client to a local server.
     */
    public final LogOnResponse connect(GameServer server, String username, String password) {
        try {
            Serializable request = new LogOnRequest(username, password);
            connectionToServer = new LocalConnection();
            connectionToServer.writeToServer(request);
            server.addConnection((LocalConnection) connectionToServer);

            return (LogOnResponse) connectionToServer.waitForObjectFromServer();
        } catch (Exception e) {
            try {
                connectionToServer.disconnect();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return LogOnResponse.rejected(e.getMessage());
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

    public final void setGameModel(FreerailsMutableSerializable world) {
        this.world = (World) world;
        committer = new MovePrecommitter(this.world);
        newWorld(this.world);
    }

    /**
     * Subclasses should override this method if they need to respond to the
     * world being changed.
     */
    protected void newWorld(World w) {}

    public void setProperty(ClientProperty propertyName, Serializable value) {
        properties.put(propertyName.name(), value);
    }

    /**
     * @param propertyName
     * @return
     */
    public final Serializable getProperty(ClientProperty propertyName) {
        return properties.get(propertyName.name());
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
            Launcher.emergencyStop();
        }
    }

    /**
     * Empty method called by update(), subclasses should override this method
     * instead of overriding update().
     */
    protected void clientUpdates() {

    }

    /**
     * Processes a message received from the server.
     */
    public final void processMessage(Serializable message) throws IOException {
        if (message instanceof MessageToClient) {
            MessageToClient request = (MessageToClient) message;
            MessageStatus status = request.execute(this);
            logger.debug(request.toString());

            connectionToServer.writeToServer(status);
        } else if (message instanceof Move) {
            Move move = (Move) message;
            committer.fromServer(move);
            moveFork.process(move);
        } else if (message instanceof MoveStatus) {
            MoveStatus moveStatus = (MoveStatus) message;
            committer.fromServer(moveStatus);
        } else if (message instanceof PreMove) {
            PreMove preMove = (PreMove) message;
            Move move = committer.fromServer(preMove);
            moveFork.process(move);
        } else if (message instanceof PreMoveStatus) {
            PreMoveStatus pms = (PreMoveStatus) message;
            committer.fromServer(pms);
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
        committer.toServer(move);
        moveFork.process(move);
        write(move);
    }

    /**
     * Tests a move before sending it to the server.
     */
    public final MoveStatus tryDoMove(Move move) {
        return move.tryDoMove(world, Player.AUTHORITATIVE);
    }

    /**
     * @param c
     */
    public void sendCommand(MessageToServer c) {
        write(c);
    }

    /**
     * @param preMove
     */
    public void processPreMove(PreMove preMove) {
        Move move = committer.toServer(preMove);
        moveFork.process(move);
        write(preMove);
    }

    /**
     * @return
     */
    protected long getLastTickTime() {
        return moveFork.getLastTickTime();
    }
}