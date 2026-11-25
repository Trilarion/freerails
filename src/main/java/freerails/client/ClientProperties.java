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
import freerails.util.IntPoint2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Client-side GUI state (for instance the cursor's position and display options) that outlives
 * individual game sessions. It is always valid, also before any world has been loaded.
 */
public class ClientProperties {

    private final Map<ModelRootProperty, Object> properties = new EnumMap<>(ModelRootProperty.class);
    private final Collection<ModelRootListener> listeners = new ArrayList<>();

    /**
     * Creates client properties holding the default values.
     */
    public ClientProperties() {
        properties.put(ModelRootProperty.CURSOR_POSITION, new IntPoint2D());
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
    }

    /**
     * Returns the value of a property.
     *
     * @param property the client property to access
     * @return the current value, or {@code null} if the property has never been set
     */
    public Object getProperty(ModelRootProperty property) {
        return properties.get(property);
    }

    /**
     * Sets the value of a property and notifies all listeners.
     *
     * @param property the client property to change
     * @param value the new value
     */
    public void setProperty(ModelRootProperty property, Object value) {
        Object oldValue = properties.put(property, value);
        for (ModelRootListener listener : new ArrayList<>(listeners)) {
            listener.propertyChange(property, oldValue, value);
        }
    }

    /**
     * Adds a property-change listener.
     *
     * @param listener listener to add
     */
    public void addPropertyChangeListener(ModelRootListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /**
     * Removes a property-change listener.
     *
     * @param listener listener to remove
     */
    public void removePropertyChangeListener(ModelRootListener listener) {
        listeners.remove(listener);
    }
}
