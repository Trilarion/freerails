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

package freerails.client.renderer;

import freerails.client.renderer.map.MapLayerRenderer;
import freerails.util.IntPoint2D;

import java.awt.*;

/**
 * Provides test support for map layer renderer.
 */
public class TestMapLayerRenderer implements MapLayerRenderer {
    /**
     * Performs paint tile.
     * @param g the g to use in the test.
     * @param tileLocation the tile location to use in the test.
     */
    @Override
    public void paintTile(Graphics g, IntPoint2D tileLocation) {}

    /**
     * Performs refresh tile.
     * @param tileLocation the tile location to use in the test.
     */
    @Override
    public void refreshTile(IntPoint2D tileLocation) {}

    /**
     * Performs refresh all.
     */
    @Override
    public void refreshAll() {}

    /**
     * Performs refresh all.
     */
    @Override
    public void paintRect(Graphics g, Rectangle visibleRect) {}
}
