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

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * A whole map as the map view sees it: something it can measure, paint and
 * invalidate. Implemented by the main (detail) map and the overview map.
 * <ul>
 * <li>Geometry: {@link #scale()} and {@link #getMapSizeInPixels()} convert
 * between pixels and tiles.</li>
 * <li>Cache invalidation: {@link #refreshTile(IntPoint2D)} and
 * {@link #refreshAll()} are called when the world changes.</li>
 * <li>Painting: {@link #paintBackgroundRect(Graphics, Rectangle)} paints the
 * cached imagery, {@link #paintDynamicRect(Graphics, Rectangle)} the
 * animated overlays on top of it.</li>
 * </ul>
 */
public interface MapRenderer {

    /**
     * Paints map imagery that remains stable while trains and cursor overlays
     * are animated.
     *
     * @param g graphics context
     * @param visibleRect visible map area
     */
    void paintBackgroundRect(Graphics g, Rectangle visibleRect);

    /**
     * Paints dynamic map overlays above the cached map background.
     *
     * @param g graphics context
     * @param visibleRect visible map area
     */
    default void paintDynamicRect(Graphics g, Rectangle visibleRect) {
    }

    /**
     * Marks one tile as changed so that it is repainted from the world.
     *
     * @param tileLocation the changed tile
     */
    void refreshTile(IntPoint2D tileLocation);

    /**
     * Marks the whole map as changed so that it is repainted from the world.
     */
    void refreshAll();

    /**
     * Returns the number of pixels per tile.
     *
     * @return the pixels per tile
     */
    float scale();

    /**
     * Returns the map size in pixels.
     *
     * @return the map size in pixels
     */
    IntPoint2D getMapSizeInPixels();
}