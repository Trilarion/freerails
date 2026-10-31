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

import freerails.controller.BuildMode;
import freerails.model.track.BuildTrackStrategy;
import freerails.move.generator.MoveGenerator;
import freerails.move.Move;
import freerails.move.Status;
import freerails.network.command.CommandToServer;
import freerails.network.command.ServerCommandReceiver;
import freerails.client.receiver.MoveChainFork;
import freerails.server.MoveReceiver;
import freerails.client.receiver.UntriedMoveReceiver;
import freerails.util.Vec2D;
import freerails.util.Utils;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.WorldMapListener;
import freerails.model.player.Player;
import freerails.util.ui.SoundManager;

import java.util.*;

/**
 * Provides access to the World object and other data that is shared by GUI
 * components (for instance the cursor's position).
 */
public class ModelRootImpl implements ModelRoot, ServerCommandReceiver {

    private final Map<ModelRootProperty, Object> properties = new HashMap<>();
    private final Collection<ModelRootListener> listeners = new ArrayList<>();
    private MoveChainFork moveFork = new MoveChainFork();
    private UntriedMoveReceiver moveReceiver;
    private Player playerPlayer;
    private ServerCommandReceiver serverCommandReceiver;
    private UnmodifiableWorld world;

    /**
     * Creates a model root impl instance.
     */
    public ModelRootImpl() {
        properties.put(ModelRootProperty.CURSOR_POSITION, new Vec2D());
        properties.put(ModelRootProperty.SHOW_STATION_NAMES, Boolean.TRUE);
        properties.put(ModelRootProperty.SHOW_CARGO_AT_STATIONS, Boolean.TRUE);
        properties.put(ModelRootProperty.SHOW_STATION_BORDERS, Boolean.TRUE);
        properties.put(ModelRootProperty.CURSOR_MODE, ModelRootValue.BUILD_TRACK_CURSOR_MODE);
        properties.put(ModelRootProperty.PREVIOUS_CURSOR_MODE, ModelRootValue.BUILD_TRACK_CURSOR_MODE);
        properties.put(ModelRootProperty.SERVER, "server details not set!");
        properties.put(ModelRootProperty.PLAY_SOUNDS, Boolean.TRUE);
        properties.put(ModelRootProperty.IGNORE_KEY_EVENTS, Boolean.FALSE);
        properties.put(ModelRootProperty.TIME, 0.0d);
        properties.put(ModelRootProperty.TRACK_BUILDER_MODE, BuildMode.BUILD_TRACK);
        properties.put(ModelRootProperty.SAVED_GAMES_LIST, Collections.emptyList());
        addPropertyChangeListener((modelRootProperty, oldValue, newValue) -> {
            if (modelRootProperty == ModelRootProperty.PLAY_SOUNDS) {
                SoundManager.getInstance().setPlayingSounds((Boolean) newValue);
            }
        });
    }

    /**
     * Adds the complete move receiver.
     *
     * @param moveReceiver the move receiver parameter
     */
    public void addCompleteMoveReceiver(MoveReceiver moveReceiver) {
        moveFork.addCompleteMoveReceiver(moveReceiver);
    }

    /**
     * Adds the map listener.
     *
     * @param listener the listener parameter
     */
    public void addMapListener(WorldMapListener listener) {
        moveFork.addMapListener(listener);
    }

    /**
     * Adds the property change listener.
     *
     * @param listener the listener parameter
     */
    public void addPropertyChangeListener(ModelRootListener listener) {
        listeners.add(listener);
    }

    /**
     * Adds the split move receiver.
     *
     * @param moveReceiver the move receiver parameter
     */
    public void addSplitMoveReceiver(MoveReceiver moveReceiver) {
        moveFork.addSplitMoveReceiver(moveReceiver);
    }

    /**
     * Performs the apply move operation.
     *
     * @param move the move parameter
     * @return the apply move produced by this method
     */
    @Override
    public Status applyMove(Move move) {
        Status status = moveReceiver.applicable(move);
        moveReceiver.process(move);

        return status;
    }

    /**
     * Performs the do pre move operation.
     *
     * @param moveGenerator the move generator parameter
     * @return the do pre move produced by this method
     */
    @Override
    public Status doPreMove(MoveGenerator moveGenerator) {
        Move move = moveGenerator.generate(world);
        Status status = moveReceiver.applicable(move);
        moveReceiver.processMoveGenerator(moveGenerator);

        return status;
    }

    /**
     * Returns the player.
     *
     * @return the player produced by this method
     */
    @Override
    public Player getPlayer() {
        return Utils.verifyNotNull(playerPlayer);
    }

    /**
     * Returns the property.
     *
     * @param property the client property to access
     * @return the property produced by this method
     */
    @Override
    public Object getProperty(ModelRootProperty property) {
        return properties.get(property);
    }

    /**
     * Returns the world.
     *
     * @return the world produced by this method
     */
    @Override
    public UnmodifiableWorld getWorld() {
        return world;
    }

    /**
     * Performs the send command operation.
     *
     * @param message the message parameter
     */
    @Override
    public void sendCommand(CommandToServer message) {
        if (null != serverCommandReceiver) {
            serverCommandReceiver.sendCommand(message);
        } else {
            System.err.println(message.toString());
        }
    }

    /**
     * Sets the move fork.
     *
     * @param moveFork the move fork parameter
     */
    public void setMoveFork(MoveChainFork moveFork) {
        this.moveFork = moveFork;
    }

    /**
     * Sets the move receiver.
     *
     * @param moveReceiver the move receiver parameter
     */
    public void setMoveReceiver(UntriedMoveReceiver moveReceiver) {
        this.moveReceiver = moveReceiver;
    }

    /**
     * Sets the property.
     *
     * @param property the client property to access
     * @param value the value to use
     */
    @Override
    public void setProperty(ModelRootProperty property, Object value) {
        Object oldValue = properties.get(property);
        properties.put(property, value);
        for (ModelRootListener listener : listeners) {
            listener.propertyChange(property, oldValue, value);
        }
    }

    /**
     * Sets the server command receiver.
     *
     * @param serverCommandReceiver the server command receiver parameter
     */
    public void setServerCommandReceiver(ServerCommandReceiver serverCommandReceiver) {
        this.serverCommandReceiver = serverCommandReceiver;
    }

    // TODO how to avoid an illegal state at the beginning?
    /**
     * Updates the ModelRoot with those properties which are dependent upon the
     * world model. Call this when the world model is changed (e.g. new map is loaded)
     */
    public void setup(UnmodifiableWorld world, Player player) {
        this.world = Utils.verifyNotNull(world);
        assert player != null;
        playerPlayer = player;
        BuildTrackStrategy buildTrackStrategy = BuildTrackStrategy.getDefault(world);
        setProperty(ModelRootProperty.BUILD_TRACK_STRATEGY, buildTrackStrategy);
    }

    /**
     * Performs the try move operation.
     *
     * @param move the move parameter
     * @return the try move produced by this method
     */
    @Override
    public Status tryMove(Move move) {
        return moveReceiver.applicable(move);
    }
}