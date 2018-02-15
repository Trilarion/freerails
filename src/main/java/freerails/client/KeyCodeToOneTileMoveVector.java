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

import freerails.model.terrain.TileTransition;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Maps keys to OneTileMoveVectors.
 */
public class KeyCodeToOneTileMoveVector {

    private static final Map<Integer, TileTransition> keycode2vector = new HashMap<>();

    static {
        // Set up key mappings...
        // Num pad with num lock on
        keycode2vector.put(KeyEvent.VK_NUMPAD1, TileTransition.SOUTH_WEST);
        keycode2vector.put(KeyEvent.VK_NUMPAD2, TileTransition.SOUTH);
        keycode2vector.put(KeyEvent.VK_NUMPAD3, TileTransition.SOUTH_EAST);
        keycode2vector.put(KeyEvent.VK_NUMPAD4, TileTransition.WEST);
        keycode2vector.put(KeyEvent.VK_NUMPAD6, TileTransition.EAST);
        keycode2vector.put(KeyEvent.VK_NUMPAD7, TileTransition.NORTH_WEST);
        keycode2vector.put(KeyEvent.VK_NUMPAD8, TileTransition.NORTH);
        keycode2vector.put(KeyEvent.VK_NUMPAD9, TileTransition.NORTH_EAST);

        // Num pad with num lock off
        keycode2vector.put(KeyEvent.VK_END, TileTransition.SOUTH_WEST);
        keycode2vector.put(KeyEvent.VK_DOWN, TileTransition.SOUTH);
        keycode2vector.put(KeyEvent.VK_PAGE_DOWN, TileTransition.SOUTH_EAST);
        keycode2vector.put(KeyEvent.VK_LEFT, TileTransition.WEST);
        keycode2vector.put(KeyEvent.VK_RIGHT, TileTransition.EAST);
        keycode2vector.put(KeyEvent.VK_HOME, TileTransition.NORTH_WEST);
        keycode2vector.put(KeyEvent.VK_UP, TileTransition.NORTH);
        keycode2vector.put(KeyEvent.VK_PAGE_UP, TileTransition.NORTH_EAST);
    }

    private KeyCodeToOneTileMoveVector() {
    }

    /**
     * Returns the OneTileMoveVector that is mapped to the specified keycode.
     */
    public static TileTransition getInstanceMappedToKey(int keycode) throws NoSuchElementException {
        Integer integer = keycode;

        if (!keycode2vector.containsKey(integer)) {
            throw new NoSuchElementException(String.valueOf(keycode));
        }

        return keycode2vector.get(integer);
    }

}
