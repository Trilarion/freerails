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
 * MapView.java
 *
 * Created on 01 August 2001, 06:16
 */
package freerails.client.renderer;

import java.awt.*;

/**
 * Paints a layer of the map which might be buffered.
 */
public interface MapLayerRenderer {

    /**
     * @param g
     * @param tileX
     * @param tileY
     */
    void paintTile(Graphics g, int tileX, int tileY);

    /**
     * @param x
     * @param y
     */
    void refreshTile(int x, int y);

    /**
     *
     */
    void refreshAll();

    /**
     * @param g
     * @param visibleRect
     */
    void paintRect(Graphics g, Rectangle visibleRect);
}