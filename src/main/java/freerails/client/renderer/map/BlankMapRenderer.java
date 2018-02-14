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

package freerails.client.renderer.map;

import freerails.util.Vector2D;

import java.awt.*;

/**
 * Used for testing the Map view components without setting up any map data.
 */
public class BlankMapRenderer implements MapRenderer {

    private final float scale;

    /**
     * @param s
     */
    public BlankMapRenderer(float s) {
        scale = s;
    }

    /**
     * @return
     */
    public float getScale() {
        return scale;
    }

    /**
     * @return
     */
    public Dimension getMapSizeInPixels() {
        int height = (int) (400 * scale);
        int width = (int) (400 * scale);

        return new Dimension(height, width);
    }

    /**
     * @param g
     * @param tileX
     * @param tileY
     */
    public void paintTile(Graphics g, Vector2D tileP) {
        paintRect(g, null);
    }

    /**
     * @param x
     * @param y
     */
    public void refreshTile(Vector2D p) {}

    /**
     * @param g
     * @param visibleRect
     */
    public void paintRect(Graphics g, Rectangle visibleRect) {
        g.setColor(Color.darkGray);
        g.fillRect(0, 0, (int) (scale * 400), (int) (scale * 400));
        g.setColor(Color.blue);

        int x = (int) (100 * scale);
        int y = (int) (100 * scale);
        int height = (int) (200 * scale);
        int width = (int) (200 * scale);
        g.fillRect(x, y, height, width);
    }

    /**
     *
     */
    public void refreshAll() {}
}