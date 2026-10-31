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

package freerails.client.renderer.track;

import freerails.util.Vector2D;

import java.awt.*;

/**
 * Draws an icon to represent a track piece.
 */
public interface TrackPieceRenderer {

    /**
     * Returns the track piece icon.
     *
     * @param trackTemplate the track template parameter
     * @return the track piece icon produced by this method
     */
    Image getTrackPieceIcon(int trackTemplate);

    /**
     * Draws the track piece icon.
     *
     * @param g the graphics context used for drawing
     * @param trackTemplate the track template parameter
     * @param tileLocation the map coordinate
     * @param tileSize the map coordinate
     */
    void drawTrackPieceIcon(Graphics g, int trackTemplate, Vector2D tileLocation, Vector2D tileSize);
}