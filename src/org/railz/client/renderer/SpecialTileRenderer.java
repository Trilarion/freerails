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
* SpecialTileView.java
*
* Created on 20 August 2001, 15:41
*/
package org.railz.client.renderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import org.railz.client.common.ImageManager;
import org.railz.world.terrain.TerrainType;
import org.railz.world.top.ReadOnlyWorld;


/**
*
* @author  Luke Lindsay
*/
final public class SpecialTileRenderer extends AbstractTileRenderer {
    final private TileRenderer parentTileView;

    public void renderTile(java.awt.Graphics g, int renderX, int renderY,
        int mapX, int mapY, ReadOnlyWorld w) {
        if (parentTileView != null) {
            parentTileView.renderTile(g, renderX, renderY, mapX, mapY, w);
        } else {
            System.err.println("parent tileView==null");
        }

        BufferedImage icon = this.getIcon(mapX, mapX, w);

        if (null != icon) {
            g.drawImage(icon, renderX, renderY, null);
        } else {
            System.err.println("special tileView icon==null");
        }
    }

    public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
        return 0;
    }

    public SpecialTileRenderer(ImageManager imageManager, int[] rgbValues,
        TerrainType tileModel, TileRenderer parentTileView)
        throws IOException {
        super(tileModel.getTerrainTypeName(), rgbValues, LAYER_TERRAIN);
        this.setTileIcons(new BufferedImage[1]);
	this.getTileIcons()[0] =
	    imageManager.getImage(generateRelativeFileName(0));
        this.parentTileView = parentTileView;
    }

    public void dumpImages(ImageManager imageManager) {
        imageManager.setImage(generateRelativeFileName(0), this.getTileIcons()[0]);
    }

    protected String generateFileNameNumber(int i) {
	return null;
    }
}
