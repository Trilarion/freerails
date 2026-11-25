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

package freerails.client.launcher;

import freerails.client.launcher.MapKeyBindings.CursorKeyMove;
import freerails.model.terrain.TileTransition;
import javafx.scene.input.KeyCode;
import junit.framework.TestCase;

import java.util.Map;
import java.util.Optional;

/**
 * Tests which keys move the map cursor and which of them may build track.
 */
public class MapKeyBindingsTest extends TestCase {

    /**
     * Tests that the number pad moves the cursor in its eight directions and may
     * build track, with or without control.
     */
    public void testNumberPadMovesAndMayBuildTrack() {
        Map<KeyCode, TileTransition> numberPad = Map.of(
                KeyCode.NUMPAD1, TileTransition.SOUTH_WEST,
                KeyCode.NUMPAD2, TileTransition.SOUTH,
                KeyCode.NUMPAD3, TileTransition.SOUTH_EAST,
                KeyCode.NUMPAD4, TileTransition.WEST,
                KeyCode.NUMPAD6, TileTransition.EAST,
                KeyCode.NUMPAD7, TileTransition.NORTH_WEST,
                KeyCode.NUMPAD8, TileTransition.NORTH,
                KeyCode.NUMPAD9, TileTransition.NORTH_EAST);

        for (Map.Entry<KeyCode, TileTransition> key : numberPad.entrySet()) {
            for (boolean controlDown : new boolean[]{false, true}) {
                assertEquals(key.getKey().toString(), Optional.of(new CursorKeyMove(key.getValue(), true)),
                        MapKeyBindings.cursorMove(key.getKey(), controlDown));
            }
        }
    }

    /**
     * Tests that control and the arrow keys only move the cursor.
     */
    public void testControlArrowsOnlyMoveTheCursor() {
        Map<KeyCode, TileTransition> arrows = Map.of(
                KeyCode.UP, TileTransition.NORTH,
                KeyCode.DOWN, TileTransition.SOUTH,
                KeyCode.LEFT, TileTransition.WEST,
                KeyCode.RIGHT, TileTransition.EAST);

        for (Map.Entry<KeyCode, TileTransition> key : arrows.entrySet()) {
            assertEquals(key.getKey().toString(), Optional.of(new CursorKeyMove(key.getValue(), false)),
                    MapKeyBindings.cursorMove(key.getKey(), true));
            assertEquals(key.getKey().toString(), Optional.empty(), MapKeyBindings.cursorMove(key.getKey(), false));
        }
    }

    /**
     * Tests that other keys do not move the cursor.
     */
    public void testOtherKeysDoNotMoveTheCursor() {
        for (KeyCode key : new KeyCode[]{KeyCode.NUMPAD5, KeyCode.NUMPAD0, KeyCode.C, KeyCode.ESCAPE, KeyCode.SPACE}) {
            assertEquals(key.toString(), Optional.empty(), MapKeyBindings.cursorMove(key, false));
            assertEquals(key.toString(), Optional.empty(), MapKeyBindings.cursorMove(key, true));
        }
    }
}
