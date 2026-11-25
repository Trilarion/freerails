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

package freerails.controller;

/**
 * Defines what the map cursor does in the build tab's track modes.
 *
 * <p>Only {@link #BUILD_TRACK} acts on keyboard cursor moves, and only on the
 * number pad: each number pad move builds track from the old to the new tile.
 * Control and the arrow keys only move the cursor, in every mode. Upgrading and
 * removing track are done by dragging the mouse only, so that track is never
 * upgraded or removed by accident. {@link CursorController} carries out keyboard
 * cursor moves; the client's key bindings decide which keys may build track.
 */
public enum BuildMode {

    /** Dragging the mouse or moving the cursor with the number pad builds track. */
    BUILD_TRACK,
    /** Dragging the mouse removes track; the keyboard only moves the cursor. */
    REMOVE_TRACK,
    /** Dragging the mouse upgrades track to the selected type; the keyboard only moves the cursor. */
    UPGRADE_TRACK,
    /** Inspect mode: the cursor moves without building, upgrading, or removing anything. */
    INSPECT,
    /** Clicking places a station of the selected type. */
    BUILD_STATION
}
