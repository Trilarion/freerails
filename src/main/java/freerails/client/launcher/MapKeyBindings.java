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

import freerails.model.terrain.TileTransition;
import javafx.scene.input.KeyCode;

import java.util.Optional;

/**
 * Maps the keys of the map view to cursor moves. The number pad moves the cursor
 * and may build track; control and the arrow keys only move the cursor (see
 * {@link freerails.controller.BuildMode}).
 */
final class MapKeyBindings {

    private MapKeyBindings() {
    }

    /**
     * A cursor move requested with the keyboard.
     *
     * @param direction the direction of the move
     * @param mayBuildTrack whether the move may build track
     */
    record CursorKeyMove(TileTransition direction, boolean mayBuildTrack) {
    }

    /**
     * Returns the cursor move of a key, if it has one.
     *
     * @param code the pressed key
     * @param controlDown whether control is held
     * @return the cursor move, or empty if the key does not move the cursor
     */
    static Optional<CursorKeyMove> cursorMove(KeyCode code, boolean controlDown) {
        TileTransition numberPad = switch (code) {
            case NUMPAD1 -> TileTransition.SOUTH_WEST;
            case NUMPAD2 -> TileTransition.SOUTH;
            case NUMPAD3 -> TileTransition.SOUTH_EAST;
            case NUMPAD4 -> TileTransition.WEST;
            case NUMPAD6 -> TileTransition.EAST;
            case NUMPAD7 -> TileTransition.NORTH_WEST;
            case NUMPAD8 -> TileTransition.NORTH;
            case NUMPAD9 -> TileTransition.NORTH_EAST;
            default -> null;
        };
        if (numberPad != null) {
            return Optional.of(new CursorKeyMove(numberPad, true));
        }
        if (!controlDown) {
            return Optional.empty();
        }
        TileTransition arrow = switch (code) {
            case DOWN -> TileTransition.SOUTH;
            case LEFT -> TileTransition.WEST;
            case RIGHT -> TileTransition.EAST;
            case UP -> TileTransition.NORTH;
            default -> null;
        };
        return Optional.ofNullable(arrow).map(direction -> new CursorKeyMove(direction, false));
    }
}
