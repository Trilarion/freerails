package jfreerails.client.common;

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.network.MoveChainFork;
import jfreerails.network.MoveReceiver;
import jfreerails.network.ServerCommand;
import jfreerails.network.ServerCommandReceiver;
import jfreerails.network.UntriedMoveReceiver;
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
public final class ModelRootImpl implements ModelRoot {
    public boolean hasBeenSetup = false;
    private UntriedMoveReceiver moveReceiver = new UntriedMoveReceiver() {
            public void processMove(Move Move) {
            }

            public MoveStatus tryDoMove(Move move) {
                return MoveStatus.moveFailed(
                    "No move receiver set on model root!");
            }
        };

    private MoveChainFork moveFork = new MoveChainFork();
    private FreerailsPrincipal playerPrincipal;
    private ReadOnlyWorld world;
    private final HashMap properties = new HashMap();
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private ServerCommandReceiver serverCommandReceiver;

    public ModelRootImpl() {
        properties.put(CURSOR_POSITION, new Point());
        properties.put(SHOW_STATION_NAMES, new Boolean(true));
        properties.put(SHOW_CARGO_AT_STATIONS, new Boolean(true));
        properties.put(SHOW_STATION_BORDERS, new Boolean(true));
        properties.put(CURSOR_MODE, BUILD_TRACK_CURSOR_MODE);
        properties.put(PREVIOUS_CURSOR_MODE, BUILD_TRACK_CURSOR_MODE);
        properties.put(SERVER, "server details not set!");
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

    public void sendCommand(ServerCommand c) {
        if (null != serverCommandReceiver) {
            serverCommandReceiver.sendCommand(c);
        } else {
            System.err.println(c.toString());
        }
    }

    public void setServerCommandReceiver(
        ServerCommandReceiver serverCommandReceiver) {
        this.serverCommandReceiver = serverCommandReceiver;
    }
}