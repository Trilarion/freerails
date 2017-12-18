/*
 * Created on Sep 13, 2004
 *
 */
package freerails.controller;

/**
 * Defines methods and constants that GUI classes can use to access shared data.
 *
 * @author Luke
 */
public interface ModelRoot extends MoveExecutor {

    /**
     *
     * @param c
     */
    void sendCommand(Message2Server c);

    /**
     *
     * @param property
     * @param newValue
     */
    void setProperty(Property property, Object newValue);

    /**
     * Tests whether the specified property has the specified value.
     * @param property
     * @param value
     * @return 
     */
    boolean is(Property property, Object value);

    /**
     *
     * @param property
     * @return
     */
    Object getProperty(Property property);

    /**
     *
     */
    enum Property {

        /**
         *
         */
        CURSOR_POSITION,

        /**
         *
         */
        CURSOR_MODE,

        /**
         *
         */
        TRACK_BUILDER_MODE,

        /**
         *
         */
        PREVIOUS_CURSOR_MODE,

        /**
         *
         */
        CURSOR_MESSAGE,

        /**
         *
         */
        QUICK_MESSAGE,

        /**
         *
         */
        PERMANENT_MESSAGE,

        /**
         *
         */
        SHOW_STATION_NAMES,

        /**
         *
         */
        SHOW_CARGO_AT_STATIONS,

        /**
         *
         */
        SHOW_STATION_BORDERS,

        /**
         *
         */
        SERVER,

        /**
         *
         */
        PLAY_SOUNDS,

        /**
         *
         */
        BUILD_TRACK_STRATEGY,

        /**
         *
         */
        IGNORE_KEY_EVENTS,

        /**
         *
         */
        PROPOSED_TRACK,

        /**
         *
         */
        SAVED_GAMES_LIST,

        /**
         *
         */
        THINKING_POINT,

        /**
         *
         */
        TIME
    }

    /**
     *
     */
    enum Value {

        /**
         *
         */
        PLACE_STATION_CURSOR_MODE,

        /**
         *
         */
        BUILD_TRACK_CURSOR_MODE
    }

}