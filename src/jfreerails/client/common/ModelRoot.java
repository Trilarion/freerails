/*
 * Created on Sep 13, 2004
 *
 */
package jfreerails.client.common;

import jfreerails.controller.MoveExecutor;
import jfreerails.network.MoveReceiver;
import jfreerails.network.ServerCommandReceiver;
import jfreerails.world.top.WorldListListener;
import jfreerails.world.top.WorldMapListener;


/**
 * Defines methods and constants that GUI classes can use to access shared data.
 *
 * @author Luke
 *
 */
public interface ModelRoot extends MoveExecutor, ServerCommandReceiver {
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
    public static final String SERVER = "SERVER";

    void setProperty(String property, Object newValue);

    Object getProperty(String property);

    void addListListener(WorldListListener listener);

    void addMapListener(WorldMapListener l);

    void addCompleteMoveReceiver(MoveReceiver l);

    void addSplitMoveReceiver(MoveReceiver l);
}