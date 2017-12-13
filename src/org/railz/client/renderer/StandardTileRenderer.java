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
* StandardTileIconSelecter.java
*
* Created on 07 July 2001, 12:11
*/
package org.railz.client.renderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import org.railz.client.common.ImageManager;
import org.railz.world.building.*;
import org.railz.world.terrain.TerrainType;


/**
*
* @author  Luke Lindsay
*/
final public class StandardTileRenderer
    extends org.railz.client.renderer.AbstractTileRenderer {
    public StandardTileRenderer(ImageManager imageManager, int[] rgbValues,
        TerrainType tileModel) throws IOException {
        super(tileModel.getTerrainTypeName(), rgbValues, LAYER_TERRAIN);
        this.setTileIcons(new BufferedImage[1]);
	this.getTileIcons()[0] =
	    imageManager.getImage(generateRelativeFileName(0));
    }

    public StandardTileRenderer(ImageManager imageManager, int[] buildingTypes,
	    BuildingType buildingType) throws IOException {
	super(buildingType.getName(), buildingTypes, LAYER_BUILDING); 
	setTileIcons(new BufferedImage[1]);
	getTileIcons()[0] = imageManager.getImage(generateRelativeFileName(0));
    }

    public void dumpImages(ImageManager imageManager) {
	imageManager.setImage(generateRelativeFileName(0),
		this.getTileIcons()[0]);
    }

    protected String generateFileNameNumber(int i) {
	return null;
    }
}
