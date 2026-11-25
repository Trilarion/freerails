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
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the default values and change notifications of the client properties.
 */
public class ClientPropertiesTest extends TestCase {

    /**
     * Tests that new client properties are usable without a loaded game.
     */
    public void testDefaults() {
        ClientProperties properties = new ClientProperties();

        assertEquals(new IntPoint2D(), properties.getProperty(ModelRootProperty.CURSOR_POSITION));
        assertEquals(ModelRootValue.BUILD_TRACK_CURSOR_MODE, properties.getProperty(ModelRootProperty.CURSOR_MODE));
        assertEquals(BuildMode.BUILD_TRACK, properties.getProperty(ModelRootProperty.TRACK_BUILDER_MODE));
        assertEquals(Boolean.TRUE, properties.getProperty(ModelRootProperty.PLAY_SOUNDS));
        assertNull(properties.getProperty(ModelRootProperty.BUILD_TRACK_STRATEGY));
    }

    /**
     * Tests that listeners get the property with its old and new value, and nothing after removal.
     */
    public void testNotifiesListenersUntilRemoved() {
        ClientProperties properties = new ClientProperties();
        List<Object[]> changes = new ArrayList<>();
        ModelRootListener listener = (property, oldValue, newValue) ->
                changes.add(new Object[]{property, oldValue, newValue});
        properties.addPropertyChangeListener(listener);

        properties.setProperty(ModelRootProperty.SHOW_STATION_NAMES, Boolean.FALSE);
        properties.removePropertyChangeListener(listener);
        properties.setProperty(ModelRootProperty.SHOW_STATION_NAMES, Boolean.TRUE);

        assertEquals(1, changes.size());
        assertEquals(ModelRootProperty.SHOW_STATION_NAMES, changes.get(0)[0]);
        assertEquals(Boolean.TRUE, changes.get(0)[1]);
        assertEquals(Boolean.FALSE, changes.get(0)[2]);
        assertEquals(Boolean.TRUE, properties.getProperty(ModelRootProperty.SHOW_STATION_NAMES));
    }
}
