/*
 * Copyright (C) Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
* MapView.java
*
* Created on 01 August 2001, 06:16
*/
package org.railz.client.renderer;

import java.awt.Graphics;
import java.awt.Rectangle;

/**
*
* @author  Luke Lindsay
*/
public interface MapLayerRenderer {
    /**
     * @param g Graphics context with origin at top left corner of map.
     * @param tileX map x coord of tile to paint
     * @param tileY map y coord of tile to paint
     */
    void paintTile(Graphics g, int tileX, int tileY);

    void refreshTile(int x, int y);

    /**
     * @param g Graphics context with origin pointing to top left corner of
     * viewport.
     * @param visibleRect rectangle defining area of map to draw relative to
     * origin 0,0 at top left of map, measured in pixels.
     */
    void paintRect(Graphics g, Rectangle visibleRect);
}
