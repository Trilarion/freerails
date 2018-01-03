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

package freerails.client.renderer;

import freerails.client.common.ImageManager;

import java.awt.*;

/**
 * Draws an icon to represent a track piece.
 */
public interface TrackPieceRenderer {

    /**
     * @param trackTemplate
     * @return
     */
    Image getTrackPieceIcon(int trackTemplate);

    /**
     * @param trackTemplate
     * @param g
     * @param x
     * @param y
     * @param tileSize
     */
    void drawTrackPieceIcon(int trackTemplate, java.awt.Graphics g, int x,
                            int y, java.awt.Dimension tileSize);
}