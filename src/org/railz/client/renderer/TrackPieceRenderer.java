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
import org.railz.client.common.ImageManager;


/**
*  Description of the Interface
*
*@author     Luke Lindsay
*     09 October 2001
*/
public interface TrackPieceRenderer {
    /**
     * @param trackConfig a byte representing a set of CompassPoints
     */
    BufferedImage getTrackPieceIcon(byte trackConfig);

    /**
     * @param trackConfig a byte representing a set of CompassPoints
     */
    void drawTrackPieceIcon(byte trackConfig, java.awt.Graphics g, int x,
        int y, java.awt.Dimension tileSize);

    /** Adds the images this TileRenderer uses to the specified ImageManager. */
    void dumpImages(ImageManager imageManager);
}
