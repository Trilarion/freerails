package jfreerails.client.common;

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import jfreerails.controller.MoveChainFork;
import jfreerails.controller.MoveExecutor;
import jfreerails.controller.MoveReceiver;
import jfreerails.controller.UntriedMoveReceiver;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.WorldListListener;
import jfreerails.world.top.WorldMapListener;


/**
 * Provides access to the World object and other data that is shared by GUI components (for instance
 * the cursor's position).
 *
 * @author Luke
 * @author Rob
 */
public final class ModelRoot implements MoveExecutor {
    public boolean hasBeenSetup = false;
    private UntriedMoveReceiver moveReceiver;
    private MoveChainFork moveFork;
    private FreerailsPrincipal playerPrincipal;
    private ReadOnlyWorld world;
    private final HashMap properties = new HashMap();
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    public static final String CURSOR_POSITION = "CURSOR_POSITION";
    public static final String CURSOR_MODE = "CURSOR_MODE";
    public static final String PREVIOUS_CURSOR_MODE = "PREVIOUS_CURSOR_MODE";
    public static final String PLACE_STATION_CURSOR_MODE = "PLACE_STATION_CURSOR_MODE";
    public static final String BUILD_TRACK_CURSOR_MODE = "BUILD_TRACK_CURSOR_MODE";
    public static final String CURSOR_MESSAGE = "CURSOR_MESSAGE";
    public static final String QUICK_MESSAGE = "QUICK_MESSAGE";
    public static final String PERMANENT_MESSAGE = "PERMANENT_MESSAGE";
    public static final String SHOW_STATION_NAMES = "SHOW_STATION_NAMES";
    public static final String SHOW_CARGO_AT_STATIONS = "SHOW_CARGO_AT_STATIONS";
    public static final String SHOW_STATION_BORDERS = "SHOW_STATION_BORDERS";

    public ModelRoot() {
        properties.put(CURSOR_POSITION, new Point());
        properties.put(SHOW_STATION_NAMES, new Boolean(true));
        properties.put(SHOW_CARGO_AT_STATIONS, new Boolean(true));
        properties.put(SHOW_STATION_BORDERS, new Boolean(true));
        properties.put(CURSOR_MODE, BUILD_TRACK_CURSOR_MODE);
        properties.put(PREVIOUS_CURSOR_MODE, BUILD_TRACK_CURSOR_MODE);
    }

    public FreerailsPrincipal getPrincipal() {
        if (null == playerPrincipal) {
            throw new NullPointerException();
        }

        return playerPrincipal;
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

        hasBeenSetup = true;
    }

    public ReadOnlyWorld getWorld() {
        return world;
    }

    public void setMoveFork(MoveChainFork moveFork) {
        this.moveFork = moveFork;
    }

    public void setMoveReceiver(UntriedMoveReceiver moveReceiver) {
        this.moveReceiver = moveReceiver;
    }

    public void setProperty(String property, Object newValue) {
        Object oldValue = properties.get(property);
        properties.put(property, newValue);
        propertyChangeSupport.firePropertyChange(property, oldValue, newValue);
    }

    public Object getProperty(String property) {
        return properties.get(property);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    public MoveStatus doMove(Move m) {
        MoveStatus ms = this.moveReceiver.tryDoMove(m);
        this.moveReceiver.processMove(m);

        return ms;
    }

    public MoveStatus tryDoMove(Move m) {
        return this.moveReceiver.tryDoMove(m);
    }

    public void addListListener(WorldListListener listener) {
        this.moveFork.addListListener(listener);
    }

    public void addMapListener(WorldMapListener l) {
        this.moveFork.addMapListener(l);
    }

    public void addCompleteMoveReceiver(MoveReceiver l) {
        this.moveFork.addCompleteMoveReceiver(l);
    }

    public void addSplitMoveReceiver(MoveReceiver l) {
        this.moveFork.addSplitMoveReceiver(l);
    }
}