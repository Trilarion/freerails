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

package org.railz.client.renderer;

import java.awt.image.BufferedImage;
import java.awt.Dimension;
import org.railz.client.common.ImageManager;
import org.railz.world.top.ReadOnlyWorld;


/**
*  Description of the Interface
*
* @author     Luke Lindsay
*     09 October 2001
*/
public interface TileRenderer {
    /**
     * The size of a map tile in pixels.
     */
    public static final Dimension TILE_SIZE = new Dimension(30, 30);

    BufferedImage getDefaultIcon();

    void renderTile(java.awt.Graphics g, int renderX, int renderY, int mapX,
        int mapY, ReadOnlyWorld w);

    /** Adds the images this TileRenderer uses to the specified ImageManager. */
    void dumpImages(ImageManager imageManager);
}
