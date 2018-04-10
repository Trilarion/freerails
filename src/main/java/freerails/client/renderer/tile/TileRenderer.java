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

package freerails.client.renderer.tile;

import freerails.model.world.ReadOnlyWorld;
import freerails.util.Vec2D;

import java.awt.*;

/**
 * Draws an icon to represent a tile.
 */
public interface TileRenderer {

    /**
     * @return
     */
    Image getDefaultIcon();

    /**
     * @param g
     * @param renderLocation
     * @param mapLocation
     * @param world
     */
    void render(Graphics g, Vec2D renderLocation, Vec2D mapLocation, ReadOnlyWorld world);

}