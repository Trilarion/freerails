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

import freerails.controller.*;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.world.FreerailsMutableSerializable;
import freerails.world.GameModel;
import freerails.world.World;
import freerails.world.player.Player;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * A client for FreerailsGameServer.
 */
public class FreerailsClient implements ClientControlInterface, GameModel,
        UntriedMoveReceiver, ServerCommandReceiver {
    private static final Logger logger = Logger.getLogger(FreerailsClient.class
            .getName());
    private final HashMap<String, Serializable> properties = new HashMap<>();
    private final MoveChainFork moveFork;

    /**
     *
     */
    protected ConnectionToServer connectionToServer;
    private World world;

    private MovePrecommitter committer;

    /**
     *
     */
    public FreerailsClient() {
        moveFork = new MoveChainFork();
    }

    /**
     * @return
     */
    public final MoveChainFork getMoveFork() {
        return moveFork;
    }

    /**
     * Connects this client to a remote server.
     *
     * @param address
     * @param port
     * @param password
     * @param username
     * @return
     */
    public final LogOnResponse connect(String address, int port,
                                       String username, String password) {
        if (logger.isDebugEnabled()) {
            logger.debug("Connect to remote server.  " + address + ":" + port);
        }

        try {
            connectionToServer = new InetConnectionToServer(address, port);
        } catch (IOException e) {
            return LogOnResponse.rejected(e.getMessage());
        }

        try {
            LogOnRequest request = new LogOnRequest(username, password);
            connectionToServer.writeToServer(request);
            connectionToServer.flush();

            return (LogOnResponse) connectionToServer
                    .waitForObjectFromServer();
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
     *
     * @param server
     * @param username
     * @param password
     * @return
     */
    public final LogOnResponse connect(GameServer server, String username,
                                       String password) {
        try {
            LogOnRequest request = new LogOnRequest(username, password);
            connectionToServer = new LocalConnection();
            connectionToServer.writeToServer(request);
            server.addConnection((LocalConnection) connectionToServer);

            return (LogOnResponse) connectionToServer
                    .waitForObjectFromServer();
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
        }
    }

    public final void setGameModel(FreerailsMutableSerializable o) {
        world = (World) o;
        committer = new MovePrecommitter(world);
        newWorld(world);
    }

    /**
     * Subclasses should override this method if they need to respond the the
     * world being changed.
     *
     * @param w
     */
    protected void newWorld(World w) {
    }

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

    /**
     * @param newProperties
     */
    public final void resetProperties(HashMap newProperties) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    final Serializable read() {
        try {
            return this.connectionToServer.waitForObjectFromServer();
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
        }

        throw new IllegalStateException();
    }

    final void write(Serializable fs) {
        try {
            connectionToServer.writeToServer(fs);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    /**
     * Reads and deals with all outstanding messages from the server.
     */
    final public void update() {
        try {
            Serializable[] messages = connectionToServer
                    .readFromServer();

            for (Serializable message : messages) {
                processMessage(message);
            }

            connectionToServer.flush();
            clientUpdates();
        } catch (IOException e) {
            ReportBugTextGenerator.unexpectedException(e);
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
    final void processMessage(Serializable message) throws IOException {
        if (message instanceof MessageToClient) {
            MessageToClient request = (MessageToClient) message;
            MessageStatus status = request.execute(this);
            if (logger.isDebugEnabled()) {
                logger.debug(request.toString());
            }
            connectionToServer.writeToServer(status);
        } else if (message instanceof Move) {
            Move m = (Move) message;
            committer.fromServer(m);
            moveFork.process(m);
        } else if (message instanceof MoveStatus) {
            MoveStatus ms = (MoveStatus) message;
            committer.fromServer(ms);
        } else if (message instanceof PreMove) {
            PreMove pm = (PreMove) message;
            Move m = committer.fromServer(pm);
            moveFork.process(m);
        } else if (message instanceof PreMoveStatus) {
            PreMoveStatus pms = (PreMoveStatus) message;
            committer.fromServer(pms);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(message.toString());
            }
        }
    }

    /**
     * @return
     */
    final public World getWorld() {
        return world;
    }

    /**
     * Sends move to the server.
     *
     * @param move
     */
    final public void process(Move move) {
        committer.toServer(move);
        moveFork.process(move);
        write(move);
    }

    /**
     * Tests a move before sending it to the server.
     *
     * @param move
     * @return
     */
    final public MoveStatus tryDoMove(Move move) {
        return move.tryDoMove(world, Player.AUTHORITATIVE);
    }

    /**
     * @param c
     */
    public void sendCommand(MessageToServer c) {
        write(c);
    }

    /**
     * @param pm
     */
    public void processPreMove(PreMove pm) {
        Move m = committer.toServer(pm);
        moveFork.process(m);
        write(pm);
    }

    /**
     * @return
     */
    protected long getLastTickTime() {
        return moveFork.getLastTickTime();
    }
}