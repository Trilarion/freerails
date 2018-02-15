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
import freerails.controller.BuildTrackStrategy;
import freerails.controller.ModelRoot;
import freerails.move.premove.PreMove;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.network.*;
import freerails.network.message.MessageToServer;
import freerails.network.movereceiver.MoveChainFork;
import freerails.network.movereceiver.MoveReceiver;
import freerails.network.movereceiver.UntriedMoveReceiver;
import freerails.util.Vector2D;
import freerails.util.Utils;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.WorldListListener;
import freerails.model.WorldMapListener;
import freerails.model.player.FreerailsPrincipal;

import java.util.*;

/**
 * Provides access to the World object and other data that is shared by GUI
 * components (for instance the cursor's position).
 */
public class ModelRootImpl implements ModelRoot, ServerCommandReceiver {

    private final Map<Property, Object> properties = new HashMap<>();
    private final Collection<ModelRootListener> listeners = new ArrayList<>();
    public boolean hasBeenSetup = false;
    private MoveChainFork moveFork = new MoveChainFork();
    private UntriedMoveReceiver moveReceiver;
    private FreerailsPrincipal playerPrincipal;
    private ServerCommandReceiver serverCommandReceiver;
    private ReadOnlyWorld world;

    /**
     *
     */
    public ModelRootImpl() {
        properties.put(Property.CURSOR_POSITION, new Vector2D());
        properties.put(Property.SHOW_STATION_NAMES, Boolean.TRUE);
        properties.put(Property.SHOW_CARGO_AT_STATIONS, Boolean.TRUE);
        properties.put(Property.SHOW_STATION_BORDERS, Boolean.TRUE);
        properties.put(Property.CURSOR_MODE, Value.BUILD_TRACK_CURSOR_MODE);
        properties.put(Property.PREVIOUS_CURSOR_MODE, Value.BUILD_TRACK_CURSOR_MODE);
        properties.put(Property.SERVER, "server details not set!");
        properties.put(Property.PLAY_SOUNDS, Boolean.TRUE);
        properties.put(Property.IGNORE_KEY_EVENTS, Boolean.FALSE);
        properties.put(Property.TIME, 0.0d);
        properties.put(Property.TRACK_BUILDER_MODE, BuildMode.BUILD_TRACK);
        properties.put(Property.SAVED_GAMES_LIST, Collections.emptyList());
        addPropertyChangeListener(SoundManager.getSoundManager());
    }

    /**
     * @param l
     */
    public void addCompleteMoveReceiver(MoveReceiver l) {
        moveFork.addCompleteMoveReceiver(l);
    }

    /**
     * @param listener
     */
    public void addListListener(WorldListListener listener) {
        moveFork.addListListener(listener);
    }

    /**
     * @param l
     */
    public void addMapListener(WorldMapListener l) {
        moveFork.addMapListener(l);
    }

    /**
     * @param l
     */
    public void addPropertyChangeListener(ModelRootListener l) {
        listeners.add(l);
    }

    /**
     * @param l
     */
    public void addSplitMoveReceiver(MoveReceiver l) {
        moveFork.addSplitMoveReceiver(l);
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
     * @param preMove
     * @return
     */
    public MoveStatus doPreMove(PreMove preMove) {
        Move move = preMove.generateMove(world);
        MoveStatus moveStatus = moveReceiver.tryDoMove(move);
        moveReceiver.processPreMove(preMove);

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
    public Object getProperty(Property property) {
        return properties.get(property);
    }

    /**
     * @return
     */
    public ReadOnlyWorld getWorld() {
        return world;
    }

    /**
     * @param c
     */
    public void sendCommand(MessageToServer c) {
        if (null != serverCommandReceiver) {
            serverCommandReceiver.sendCommand(c);
        } else {
            System.err.println(c.toString());
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
     * @param newValue
     */
    public void setProperty(Property property, Object newValue) {
        Object oldValue = properties.get(property);
        properties.put(property, newValue);
        for (ModelRootListener listener : listeners) {
            listener.propertyChange(property, oldValue, newValue);
        }
    }

    /**
     * @param serverCommandReceiver
     */
    public void setServerCommandReceiver(ServerCommandReceiver serverCommandReceiver) {
        this.serverCommandReceiver = serverCommandReceiver;
    }

    /**
     * Updates the ModelRoot with those properties which are dependent upon the
     * world model. Call this when the world model is changed (e.g. new map is
     * loaded)
     */
    public void setup(ReadOnlyWorld world, FreerailsPrincipal principal) {
        this.world = Utils.verifyNotNull(world);
        assert principal != null;
        assert world.isPlayer(principal);
        playerPrincipal = principal;
        BuildTrackStrategy bts = BuildTrackStrategy.getDefault(world);
        setProperty(ModelRoot.Property.BUILD_TRACK_STRATEGY, bts);
        hasBeenSetup = true;
    }

    /**
     * @param move
     * @return
     */
    public MoveStatus tryDoMove(Move move) {
        return moveReceiver.tryDoMove(move);
    }

    public boolean is(ModelRoot.Property property, Object value) {
        return getProperty(property).equals(value);
    }

}