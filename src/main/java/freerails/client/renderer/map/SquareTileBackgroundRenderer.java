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
     * The map has changed.
     *
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     * @param width the width parameter
     * @param height the height parameter
     */
    @Override
    protected void paintBufferRectangle(int x, int y, int width, int height) {
        // Fix for bug [ 1303162 ]
        // If the buffer hasn't been set yet, don't try and refresh it!
        if (null != super.backgroundBuffer) {
            Graphics graphics = bg.create();
            graphics.setClip(x, y, width, height);
            graphics.translate(-bufferRect.x, -bufferRect.y);
            mapLayerRenderer.paintRect(graphics, bufferRect);
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
        // The backgroundBuffer gets created on the first call to
        // backgroundBuffer.paintRect(..)
        // so we need a check here to avoid a null pointer exception.
        if (null != super.backgroundBuffer) {
            Graphics gg = bg.create();
            gg.translate(-bufferRect.x, -bufferRect.y);
            mapLayerRenderer.paintTile(gg, tileLocation);
            gg.dispose();
        }
    }
}