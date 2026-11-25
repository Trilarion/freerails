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

import freerails.client.receiver.MoveChainFork;
import freerails.client.receiver.UntriedMoveReceiver;
import freerails.model.player.Player;
import freerails.model.track.BuildTrackStrategy;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.WorldMapListener;
import freerails.move.Move;
import freerails.move.MoveReceiver;
import freerails.move.Status;
import freerails.move.generator.MoveGenerator;
import freerails.network.command.CommandToServer;
import freerails.network.command.ServerCommandReceiver;

import java.util.Objects;

/**
 * One loaded game as seen by the client: the world, the local player, the receivers that moves and
 * server commands are sent to, and the long-lived client properties. All parts are fixed at
 * construction; when a new world is loaded, a new instance is created.
 */
public class ModelRootImpl implements ModelRoot {

    private final ClientProperties properties;
    private final UnmodifiableWorld world;
    private final Player player;
    private final UntriedMoveReceiver moveReceiver;
    private final ServerCommandReceiver serverCommandReceiver;
    private final MoveChainFork moveFork;

    /**
     * Creates the model root for a loaded game and resets the world-dependent client properties
     * (currently the build track strategy) to their defaults for this world.
     *
     * @param properties long-lived client properties
     * @param world the loaded game world
     * @param player the local player
     * @param moveReceiver receives the moves this client tries and applies
     * @param serverCommandReceiver receives the commands this client sends to the server
     * @param moveFork distributes committed moves to registered listeners
     */
    public ModelRootImpl(
            ClientProperties properties,
            UnmodifiableWorld world,
            Player player,
            UntriedMoveReceiver moveReceiver,
            ServerCommandReceiver serverCommandReceiver,
            MoveChainFork moveFork) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.world = Objects.requireNonNull(world, "world");
        this.player = Objects.requireNonNull(player, "player");
        this.moveReceiver = Objects.requireNonNull(moveReceiver, "moveReceiver");
        this.serverCommandReceiver = Objects.requireNonNull(serverCommandReceiver, "serverCommandReceiver");
        this.moveFork = Objects.requireNonNull(moveFork, "moveFork");
        properties.setProperty(ModelRootProperty.BUILD_TRACK_STRATEGY, BuildTrackStrategy.getDefault(world));
    }

    /**
     * Adds a receiver for every committed move.
     *
     * @param receiver the receiver to add
     */
    public void addCompleteMoveReceiver(MoveReceiver receiver) {
        moveFork.addCompleteMoveReceiver(receiver);
    }

    /**
     * Removes a receiver for every committed move.
     *
     * @param receiver the receiver to remove
     */
    public void removeCompleteMoveReceiver(MoveReceiver receiver) {
        moveFork.removeCompleteMoveReceiver(receiver);
    }

    /**
     * Adds a receiver for the individual leaves of committed moves.
     *
     * @param receiver the receiver to add
     */
    public void addSplitMoveReceiver(MoveReceiver receiver) {
        moveFork.addSplitMoveReceiver(receiver);
    }

    /**
     * Adds a listener for map updates.
     *
     * @param listener the listener to add
     */
    public void addMapListener(WorldMapListener listener) {
        moveFork.addMapListener(listener);
    }

    /**
     * Removes a listener for map updates.
     *
     * @param listener listener to remove
     */
    public void removeMapListener(WorldMapListener listener) {
        moveFork.removeMapListener(listener);
    }

    /**
     * Checks a move and passes it on to the move receiver.
     *
     * @param move the move to apply
     * @return whether the move was applicable when it was checked
     */
    @Override
    public Status applyMove(Move move) {
        Status status = moveReceiver.applicable(move);
        moveReceiver.process(move);
        return status;
    }

    /**
     * Checks the move of a move generator and passes the generator on to the move receiver.
     *
     * @param moveGenerator the move generator
     * @return whether the generated move was applicable when it was checked
     */
    @Override
    public Status doPreMove(MoveGenerator moveGenerator) {
        Move move = moveGenerator.generate(world);
        Status status = moveReceiver.applicable(move);
        moveReceiver.processMoveGenerator(moveGenerator);
        return status;
    }

    /**
     * Checks whether a move is applicable without applying it.
     *
     * @param move the move to check
     * @return whether the move is applicable
     */
    @Override
    public Status tryMove(Move move) {
        return moveReceiver.applicable(move);
    }

    /**
     * Returns the local player.
     *
     * @return the local player
     */
    @Override
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the loaded world.
     *
     * @return the loaded world
     */
    @Override
    public UnmodifiableWorld getWorld() {
        return world;
    }

    /**
     * Returns the long-lived client properties.
     *
     * @return the client properties
     */
    @Override
    public ClientProperties getProperties() {
        return properties;
    }

    /**
     * Sends a command to the server.
     *
     * @param message the command to send
     */
    @Override
    public void sendCommand(CommandToServer message) {
        serverCommandReceiver.sendCommand(message);
    }
}
