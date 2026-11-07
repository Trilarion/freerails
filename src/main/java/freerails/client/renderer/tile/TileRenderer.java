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

package freerails.client.renderer.tile;

import freerails.model.world.UnmodifiableWorld;
import freerails.util.IntPoint2D;

import java.awt.*;

/**
 * Draws an icon to represent a tile.
 */
public interface TileRenderer {

    /**
     * Returns the default icon.
     *
     * @return the default icon produced by this method
     */
    Image getDefaultIcon();

    /**
     * Performs the render operation.
     *
     * @param g the graphics context used for drawing
     * @param renderLocation the map coordinate
     * @param mapLocation the map coordinate
     * @param world the game world being queried
     */
    void render(Graphics g, IntPoint2D renderLocation, IntPoint2D mapLocation, UnmodifiableWorld world);

}