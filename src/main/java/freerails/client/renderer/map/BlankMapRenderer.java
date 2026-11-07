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

package freerails.client.renderer.map;

import freerails.util.IntPoint2D;

import java.awt.*;

/**
 * Used for testing the Map view components without setting up any map data. Also used in the overview map.
 *
 * @param scale scale factor applied to the map dimensions
 */
public record BlankMapRenderer(float scale) implements MapRenderer {

    /**
     * Creates a blank map renderer instance.
     *
     * @param scale the scale parameter
     */
    public BlankMapRenderer {
    }

    /**
     * Performs the scale operation.
     *
     * @return the scale produced by this method
     */
    @Override
    public float scale() {
        return scale;
    }

    /**
     * Returns the map size in pixels.
     *
     * @return the map size in pixels produced by this method
     */
    @Override
    public IntPoint2D getMapSizeInPixels() {
        int height = (int) (400 * scale);
        int width = (int) (400 * scale);

        return new IntPoint2D(width, height);
    }

    /**
     * Performs the paint tile operation.
     *
     * @param g the graphics context used for drawing
     * @param tileLocation the map coordinate
     */
    @Override
    public void paintTile(Graphics g, IntPoint2D tileLocation) {
        paintRect(g, null);
    }

    /**
     * Performs the refresh tile operation.
     *
     * @param tileLocation the map coordinate
     */
    @Override
    public void refreshTile(IntPoint2D tileLocation) {
    }

    /**
     * Performs the paint rect operation.
     *
     * @param g the graphics context used for drawing
     * @param visibleRect the visible area to render
     */
    @Override
    public void paintRect(Graphics g, Rectangle visibleRect) {
        g.setColor(Color.darkGray);
        g.fillRect(0, 0, (int) (scale * 400), (int) (scale * 400));
        g.setColor(Color.blue);

        int x = (int) (100 * scale);
        int y = (int) (100 * scale);
        int height = (int) (200 * scale);
        int width = (int) (200 * scale);
        // g.fillRect(x, y, height, width);
        g.fillRect(x, y, width, height);
    }

    /**
     * Performs the refresh all operation.
     */
    @Override
    public void refreshAll() {
    }
}