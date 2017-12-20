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

/*
 *
 */
package freerails.controller;

/**
 * Defines methods and constants that GUI classes can use to access shared data.
 */
public interface ModelRoot extends MoveExecutor {

    /**
     * @param c
     */
    void sendCommand(MessageToServer c);

    /**
     * @param property
     * @param newValue
     */
    void setProperty(Property property, Object newValue);

    /**
     * Tests whether the specified property has the specified value.
     *
     * @param property
     * @param value
     * @return
     */
    boolean is(Property property, Object value);

    /**
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