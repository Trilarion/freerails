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

/*
 *  SquareTileBackgroundPainter.java
 *
 *  Created on 31 July 2001, 16:36
 */
package freerails.client.renderer.map;

import freerails.util.Vec2D;
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
     * @param mapLayerRenderer
     */
    public SquareTileBackgroundRenderer(MapLayerRenderer mapLayerRenderer) {
        this.mapLayerRenderer = Utils.verifyNotNull(mapLayerRenderer);
    }

    /**
     * The map has changed.
     *
     * @param x
     * @param y
     * @param width
     * @param height
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
     * @param g
     * @param tileLocation
     */
    public void paintTile(Graphics g, Vec2D tileLocation) {
        mapLayerRenderer.paintTile(g, tileLocation);
    }

    /**
     * @param tileLocation
     */
    public void refreshTile(Vec2D tileLocation) {
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