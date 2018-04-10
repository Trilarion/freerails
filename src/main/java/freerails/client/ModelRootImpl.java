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

import freerails.controller.BuildMode;
import freerails.model.track.BuildTrackStrategy;
import freerails.move.generator.MoveGenerator;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.network.command.CommandToServer;
import freerails.network.command.ServerCommandReceiver;
import freerails.move.receiver.MoveChainFork;
import freerails.move.receiver.MoveReceiver;
import freerails.move.receiver.UntriedMoveReceiver;
import freerails.util.Vec2D;
import freerails.util.Utils;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.WorldListListener;
import freerails.model.WorldMapListener;
import freerails.model.player.FreerailsPrincipal;
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
    private FreerailsPrincipal playerPrincipal;
    private ServerCommandReceiver serverCommandReceiver;
    private ReadOnlyWorld world;

    /**
     *
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
     * @param moveReceiver
     */
    public void addCompleteMoveReceiver(MoveReceiver moveReceiver) {
        moveFork.addCompleteMoveReceiver(moveReceiver);
    }

    /**
     * @param listener
     */
    public void addListListener(WorldListListener listener) {
        moveFork.addListListener(listener);
    }

    /**
     * @param listener
     */
    public void addMapListener(WorldMapListener listener) {
        moveFork.addMapListener(listener);
    }

    /**
     * @param listener
     */
    public void addPropertyChangeListener(ModelRootListener listener) {
        listeners.add(listener);
    }

    /**
     * @param moveReceiver
     */
    public void addSplitMoveReceiver(MoveReceiver moveReceiver) {
        moveFork.addSplitMoveReceiver(moveReceiver);
    }

    /**
     * @param move
     * @return
     */
    public MoveStatus doMove(Move move) {
        MoveStatus moveStatus = moveReceiver.tryDoMove(move);
        moveReceiver.process(move);

        return moveStatus;
    }

    /**
     * @param moveGenerator
     * @return
     */
    public MoveStatus doPreMove(MoveGenerator moveGenerator) {
        Move move = moveGenerator.generate(world);
        MoveStatus moveStatus = moveReceiver.tryDoMove(move);
        moveReceiver.processMoveGenerator(moveGenerator);

        return moveStatus;
    }

    /**
     * @return
     */
    public FreerailsPrincipal getPrincipal() {
        return Utils.verifyNotNull(playerPrincipal);
    }

    /**
     * @param property
     * @return
     */
    public Object getProperty(ModelRootProperty property) {
        return properties.get(property);
    }

    /**
     * @return
     */
    public ReadOnlyWorld getWorld() {
        return world;
    }

    /**
     * @param message
     */
    public void sendCommand(CommandToServer message) {
        if (null != serverCommandReceiver) {
            serverCommandReceiver.sendCommand(message);
        } else {
            System.err.println(message.toString());
        }
    }

    /**
     * @param moveFork
     */
    public void setMoveFork(MoveChainFork moveFork) {
        this.moveFork = moveFork;
    }

    /**
     * @param moveReceiver
     */
    public void setMoveReceiver(UntriedMoveReceiver moveReceiver) {
        this.moveReceiver = moveReceiver;
    }

    /**
     * @param property
     * @param value
     */
    public void setProperty(ModelRootProperty property, Object value) {
        Object oldValue = properties.get(property);
        properties.put(property, value);
        for (ModelRootListener listener : listeners) {
            listener.propertyChange(property, oldValue, value);
        }
    }

    /**
     * @param serverCommandReceiver
     */
    public void setServerCommandReceiver(ServerCommandReceiver serverCommandReceiver) {
        this.serverCommandReceiver = serverCommandReceiver;
    }

    // TODO how to avoid an illegal state at the beginning?
    /**
     * Updates the ModelRoot with those properties which are dependent upon the
     * world model. Call this when the world model is changed (e.g. new map is loaded)
     */
    public void setup(ReadOnlyWorld world, FreerailsPrincipal principal) {
        this.world = Utils.verifyNotNull(world);
        assert principal != null;
        assert world.isPlayer(principal);
        playerPrincipal = principal;
        BuildTrackStrategy buildTrackStrategy = BuildTrackStrategy.getDefault(world);
        setProperty(ModelRootProperty.BUILD_TRACK_STRATEGY, buildTrackStrategy);
    }

    /**
     * @param move
     * @return
     */
    public MoveStatus tryDoMove(Move move) {
        return moveReceiver.tryDoMove(move);
    }
}