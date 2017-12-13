/*
 * Copyright (C) 2004 Robert Tuck
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
import java.io.File;
import java.io.IOException;

import org.railz.client.common.BinaryNumberFormatter;
import org.railz.client.common.ImageManager;
import org.railz.world.building.*;
import org.railz.world.common.*;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.track.*;

/**
 *  This class renders a station tile.
 *
 * @author rtuck99@users.berlios.de
 */
final public class StationRenderer extends AbstractTileRenderer {
    public StationRenderer(ImageManager imageManager, BuildingType
	    buildingType) throws IOException {
	super(buildingType.getName(), new int[0], LAYER_BUILDING);
	setTileIcons(new BufferedImage[256]);
	byte direction = CompassPoints.NORTH;
	for (int i = 0; i < 8; i++) {
	    getTileIcons()[(int) direction & 0xFF] =
		imageManager.getImage(generateRelativeFileName((int) direction
			    & 0xFF));
	    direction = CompassPoints.rotateClockwise(direction);
	}
	direction = CompassPoints.NORTH | CompassPoints.SOUTH;
	for (int i = 0; i < 4; i++) {
	    getTileIcons()[(int) direction & 0xFF] =
		imageManager.getImage(generateRelativeFileName((int) direction
			    & 0xFF));
	    direction = CompassPoints.rotateClockwise(direction);
	}
    }

    public BufferedImage getIcon(int x, int y, ReadOnlyWorld w) {
	TrackTile tt = w.getTile(x, y).getTrackTile();

	int trackTemplate = (int) tt.getTrackConfiguration() & 0xFF;
        return getTileIcons()[trackTemplate];
    }

    public void renderTile(java.awt.Graphics g, int screenX, int screenY,
        int mapX, int mapY, ReadOnlyWorld w) {
        BufferedImage icon = getIcon(mapX, mapY, w);

        if (null != icon) {
	    g.drawImage(icon, screenX - TileRenderer.TILE_SIZE.width / 2,
		    screenY - TileRenderer.TILE_SIZE.height / 2, null);
        }
    }

    public void dumpImages(ImageManager imageManager) {
        for (int i = 0; i < 256; i++) {
            if (getTileIcons()[i] != null) {
                String fileName = generateRelativeFileName(i);
                imageManager.setImage(fileName, getTileIcons()[i]);
            }
        }
    }

    protected String generateFileNameNumber(int i) {
        return BinaryNumberFormatter.format(i, 8);
    }

    public BufferedImage getDefaultIcon() {
	return getTileIcons()[(int) (CompassPoints.NORTHWEST |
	    CompassPoints.SOUTHEAST) & 0xFF];
    }
}

