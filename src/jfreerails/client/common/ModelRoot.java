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
	
	public enum Property {CURSOR_POSITION, CURSOR_MODE, TRACK_BUILDER_MODE, PREVIOUS_CURSOR_MODE, 
		 CURSOR_MESSAGE,QUICK_MESSAGE, PERMANENT_MESSAGE, SHOW_STATION_NAMES, SHOW_CARGO_AT_STATIONS, SHOW_STATION_BORDERS,
		SERVER, PLAY_SOUNDS, BUILD_TRACK_STRATEGY, IGNORE_KEY_EVENTS, PROPOSED_TRACK, THINKING_POINT};

	public enum Value {PLACE_STATION_CURSOR_MODE, BUILD_TRACK_CURSOR_MODE};

    void setProperty(Property property, Object newValue);
    
    /** Tests whether the specified property has the specified value.*/
    boolean is(Property property, Object value);

    Object getProperty(Property property);

    void addListListener(WorldListListener listener);

    void addMapListener(WorldMapListener l);

    void addCompleteMoveReceiver(MoveReceiver l);

    void addSplitMoveReceiver(MoveReceiver l);
}