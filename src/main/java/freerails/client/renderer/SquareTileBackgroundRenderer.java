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
package freerails.client.renderer;

import java.awt.*;

/**
 * This class stores a buffer containing the terrain and track layers of current
 * visible rectangle of the map. It is responsible of painting these layers and
 * updating the buffer when the map scrolls or tiles are updated.
 */
public final class SquareTileBackgroundRenderer extends
        BufferedTiledBackgroundRenderer {
    private final MapLayerRenderer mapView;

    /**
     * @param mv
     */
    public SquareTileBackgroundRenderer(MapLayerRenderer mv) {
        if (null == mv) {
            throw new NullPointerException();
        }

        this.mapView = mv;
    }

    /**
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
            Graphics gg = bg.create();
            gg.setClip(x, y, width, height);
            gg.translate(-bufferRect.x, -bufferRect.y);
            mapView.paintRect(gg, bufferRect);
            gg.dispose();
        }
    }

    /**
     * @param g
     * @param tileX
     * @param tileY
     */
    public void paintTile(Graphics g, int tileX, int tileY) {
        mapView.paintTile(g, tileX, tileY);
    }

    /**
     * @param x
     * @param y
     */
    public void refreshTile(int x, int y) {
        // The backgroundBuffer gets created on the first call to
        // backgroundBuffer.paintRect(..)
        // so we need a check here to avoid a null pointer exception.
        if (null != super.backgroundBuffer) {
            Graphics gg = bg.create();
            gg.translate(-bufferRect.x, -bufferRect.y);
            mapView.paintTile(gg, x, y);
            gg.dispose();
        }
    }
}