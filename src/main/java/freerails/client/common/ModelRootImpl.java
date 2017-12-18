package freerails.client.common;

import freerails.controller.*;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.network.MoveChainFork;
import freerails.network.MoveReceiver;
import freerails.network.ServerCommandReceiver;
import freerails.network.UntriedMoveReceiver;
import freerails.world.common.ImPoint;
import freerails.world.common.ImStringList;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.WorldListListener;
import freerails.world.top.WorldMapListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Provides access to the World object and other data that is shared by GUI
 * components (for instance the cursor's position).
 *
 * @author Luke
 * @author Rob
 */
public final class ModelRootImpl implements ModelRoot, ServerCommandReceiver {
    private final HashMap<Property, Object> properties = new HashMap<>();
    private final ArrayList<ModelRootListener> listeners = new ArrayList<>();
    public boolean hasBeenSetup = false;
    private MoveChainFork moveFork = new MoveChainFork();
    private UntriedMoveReceiver moveReceiver = new UntriedMoveReceiver() {
        public void processMove(Move Move) {
        }

        public void processPreMove(PreMove pm) {
        }

        public MoveStatus tryDoMove(Move move) {
            return MoveStatus.moveFailed("No move receiver set on model root!");
        }
    };
    private FreerailsPrincipal playerPrincipal;
    private ServerCommandReceiver serverCommandReceiver;
    private ReadOnlyWorld world;

    public ModelRootImpl() {
        properties.put(Property.CURSOR_POSITION, new ImPoint());
        properties.put(Property.SHOW_STATION_NAMES, Boolean.TRUE);
        properties.put(Property.SHOW_CARGO_AT_STATIONS, Boolean.TRUE);
        properties.put(Property.SHOW_STATION_BORDERS, Boolean.TRUE);
        properties.put(Property.CURSOR_MODE, Value.BUILD_TRACK_CURSOR_MODE);
        properties.put(Property.PREVIOUS_CURSOR_MODE,
                Value.BUILD_TRACK_CURSOR_MODE);
        properties.put(Property.SERVER, "server details not set!");
        properties.put(Property.PLAY_SOUNDS, Boolean.TRUE);
        properties.put(Property.IGNORE_KEY_EVENTS, Boolean.FALSE);
        properties.put(Property.TIME, 0d);
        properties.put(Property.TRACK_BUILDER_MODE,
                TrackMoveProducer.BuildMode.BUILD_TRACK);
        properties.put(Property.SAVED_GAMES_LIST, new ImStringList());
        addPropertyChangeListener(SoundManager.getSoundManager());
    }

    public void addCompleteMoveReceiver(MoveReceiver l) {
        this.moveFork.addCompleteMoveReceiver(l);
    }

    public void addListListener(WorldListListener listener) {
        this.moveFork.addListListener(listener);
    }

    public void addMapListener(WorldMapListener l) {
        this.moveFork.addMapListener(l);
    }

    public void addPropertyChangeListener(ModelRootListener l) {
        listeners.add(l);
    }

    public void addSplitMoveReceiver(MoveReceiver l) {
        this.moveFork.addSplitMoveReceiver(l);
    }

    public MoveStatus doMove(Move m) {
        MoveStatus ms = this.moveReceiver.tryDoMove(m);
        this.moveReceiver.processMove(m);

        return ms;
    }

    public MoveStatus doPreMove(PreMove pm) {
        Move m = pm.generateMove(world);
        MoveStatus ms = moveReceiver.tryDoMove(m);
        moveReceiver.processPreMove(pm);

        return ms;
    }

    public FreerailsPrincipal getPrincipal() {
        if (null == playerPrincipal) {
            throw new NullPointerException();
        }

        return playerPrincipal;
    }

    public Object getProperty(Property p) {
        return properties.get(p);
    }

    public ReadOnlyWorld getWorld() {
        return world;
    }

    public void sendCommand(Message2Server c) {
        if (null != serverCommandReceiver) {
            serverCommandReceiver.sendCommand(c);
        } else {
            System.err.println(c.toString());
        }
    }

    public void setMoveFork(MoveChainFork moveFork) {
        this.moveFork = moveFork;
    }

    public void setMoveReceiver(UntriedMoveReceiver moveReceiver) {
        this.moveReceiver = moveReceiver;
    }

    public void setProperty(Property p, Object newValue) {
        Object oldValue = properties.get(p);
        properties.put(p, newValue);
        for (ModelRootListener listener : listeners) {
            listener.propertyChange(p, oldValue, newValue);
        }

    }

    public void setServerCommandReceiver(
            ServerCommandReceiver serverCommandReceiver) {
        this.serverCommandReceiver = serverCommandReceiver;
    }

    /**
     * Updates the ModelRoot with those properties which are dependent upon the
     * world model. Call this when the world model is changed (e.g. new map is
     * loaded)
     */
    public void setup(ReadOnlyWorld world, FreerailsPrincipal p) {
        this.world = world;
        assert p != null;
        assert world.isPlayer(p);
        playerPrincipal = p;

        if (null == world) {
            throw new NullPointerException();
        }

        BuildTrackStrategy bts = BuildTrackStrategy.getDefault(world);
        setProperty(ModelRoot.Property.BUILD_TRACK_STRATEGY, bts);

        hasBeenSetup = true;

    }

    public MoveStatus tryDoMove(Move m) {
        return this.moveReceiver.tryDoMove(m);
    }

    public boolean is(ModelRoot.Property p, Object value) {
        return getProperty(p).equals(value);
    }
}