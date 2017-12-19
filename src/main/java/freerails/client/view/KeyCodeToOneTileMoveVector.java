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

package freerails.client.view;

import freerails.world.common.Step;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * Maps keys to OneTileMoveVectors.
 */
public class KeyCodeToOneTileMoveVector {

    private static final HashMap<Integer, Step> keycode2vector = new HashMap<>();

    static {
        // Set up key mappings...
        // Num pad with num lock on
        keycode2vector.put(KeyEvent.VK_NUMPAD1, Step.SOUTH_WEST);
        keycode2vector.put(KeyEvent.VK_NUMPAD2, Step.SOUTH);
        keycode2vector.put(KeyEvent.VK_NUMPAD3, Step.SOUTH_EAST);
        keycode2vector.put(KeyEvent.VK_NUMPAD4, Step.WEST);
        keycode2vector.put(KeyEvent.VK_NUMPAD6, Step.EAST);
        keycode2vector.put(KeyEvent.VK_NUMPAD7, Step.NORTH_WEST);
        keycode2vector.put(KeyEvent.VK_NUMPAD8, Step.NORTH);
        keycode2vector.put(KeyEvent.VK_NUMPAD9, Step.NORTH_EAST);

        // Num pad with num lock off
        keycode2vector.put(KeyEvent.VK_END, Step.SOUTH_WEST);
        keycode2vector.put(KeyEvent.VK_DOWN, Step.SOUTH);
        keycode2vector.put(KeyEvent.VK_PAGE_DOWN, Step.SOUTH_EAST);
        keycode2vector.put(KeyEvent.VK_LEFT, Step.WEST);
        keycode2vector.put(KeyEvent.VK_RIGHT, Step.EAST);
        keycode2vector.put(KeyEvent.VK_HOME, Step.NORTH_WEST);
        keycode2vector.put(KeyEvent.VK_UP, Step.NORTH);
        keycode2vector.put(KeyEvent.VK_PAGE_UP, Step.NORTH_EAST);

    }

    /**
     * Returns the OneTileMoveVector that is mapped to the specified keycode.
     *
     * @param keycode
     * @return
     */
    public static Step getInstanceMappedToKey(int keycode)
            throws NoSuchElementException {
        Integer integer = keycode;

        if (!keycode2vector.containsKey(integer)) {
            throw new NoSuchElementException(String.valueOf(keycode));
        }

        return keycode2vector.get(integer);
    }

}
