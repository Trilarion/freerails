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

/*
 *  SquareTileBackgroundPainter.java
 *
 *  Created on 31 July 2001, 16:36
 */
package freerails.client.renderer.map;

import freerails.util.IntPoint2D;
import freerails.util.Utils;

import java.awt.*;

/**
 * Stores a buffer containing the terrain and track layers of current
 * visible rectangle of the map. It is responsible of painting these layers and
 * updating the buffer when the map scrolls or tiles are updated.
 */
public class SquareTileBackgroundRenderer extends BufferedTiledBackgroundRenderer {

    private final MapLayerRenderer mapLayerRenderer;

    /**
     * Creates a square tile background renderer instance.
     *
     * @param mapLayerRenderer the map layer renderer parameter
     */
    public SquareTileBackgroundRenderer(MapLayerRenderer mapLayerRenderer) {
        this.mapLayerRenderer = Utils.verifyNotNull(mapLayerRenderer);
    }

    /**
     * Paints a part of the buffer from the map layer.
     *
     * @param x the horizontal buffer coordinate
     * @param y the vertical buffer coordinate
     * @param width the width of the part to paint
     * @param height the height of the part to paint
     */
    @Override
    protected void paintBufferRectangle(int x, int y, int width, int height) {
        Rectangle bufferRect = getBufferRect();
        Graphics graphics = createBufferGraphics();
        try {
            graphics.setClip(x, y, width, height);
            graphics.translate(-bufferRect.x, -bufferRect.y);
            mapLayerRenderer.paintRect(graphics, bufferRect);
        } finally {
            graphics.dispose();
        }
    }

    /**
     * Performs the paint tile operation.
     *
     * @param g the graphics context used for drawing
     * @param tileLocation the map coordinate
     */
    @Override
    public void paintTile(Graphics g, IntPoint2D tileLocation) {
        mapLayerRenderer.paintTile(g, tileLocation);
    }

    /**
     * Performs the refresh tile operation.
     *
     * @param tileLocation the map coordinate
     */
    @Override
    public void refreshTile(IntPoint2D tileLocation) {
        // Before the first paint there is no buffer to update.
        if (hasBuffer()) {
            Rectangle bufferRect = getBufferRect();
            Graphics graphics = createBufferGraphics();
            try {
                graphics.translate(-bufferRect.x, -bufferRect.y);
                mapLayerRenderer.paintTile(graphics, tileLocation);
            } finally {
                graphics.dispose();
            }
        }
    }
}