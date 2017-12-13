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
* TileView.java
*
* Created on 04 July 2001, 07:01
*/
package org.railz.client.renderer;

import java.awt.image.BufferedImage;
import java.io.File;
import org.railz.client.common.ImageManager;
import org.railz.world.building.*;
import org.railz.world.terrain.TerrainTile;
import org.railz.world.terrain.TerrainType;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.track.*;


/**
*  This class encapsulates the visible properties of a tile.
* @author  Luke Lindsay
*/
public abstract class AbstractTileRenderer implements TileRenderer {
    private final int layer;
    protected final int[] typeNumbers;
    private BufferedImage[] tileIcons;
    private final String terrainType;

    public final static int LAYER_BUILDING = 1;
    public final static int LAYER_TERRAIN = 2;

    public AbstractTileRenderer(String terrainType, int[] matchingTypes, 
	    int layer) {
	this.layer = layer;
        this.terrainType = terrainType;
        this.typeNumbers = matchingTypes;

        if (null == matchingTypes) {
            throw new NullPointerException();
        }
    }

    public void renderTile(java.awt.Graphics g, int screenX, int screenY,
        int mapX, int mapY, ReadOnlyWorld w) {
        BufferedImage icon = this.getIcon(mapX, mapY, w);

        if (null != icon) {
            g.drawImage(icon, screenX, screenY, null);
        }
    }

    public BufferedImage getDefaultIcon() {
        return getTileIcons()[0];
    }

    /**
     * Returns an icon for the tile at x,y, which may depend on the terrain
     * types of of the surrounding tiles.
     */
    public BufferedImage getIcon(int x, int y, ReadOnlyWorld w) {
        int tile = selectTileIcon(x, y, w);

        if (getTileIcons()[tile] != null) {
            return getTileIcons()[tile];
        } else {
            throw new NullPointerException(
                "Error in TileView.getIcon: icon no. " + tile + "==null");
        }
    }

    public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
        return 0;
    }

    /**
     * @return 1 if the tile at the specified location matches any of our
     * specified types otherwise return 0. (we return an int to facilitate
     * bitwise operations)
     */
    protected int checkTile(int x, int y, ReadOnlyWorld w) {
        if (((x < w.getMapWidth()) && (x >= 0)) && (y < w.getMapHeight()) &&
                (y >= 0)) {
	    int type;
	    FreerailsTile tt = w.getTile(x, y);
	    if (layer == LAYER_TERRAIN) {
		type = tt.getTerrainTypeNumber();
	    } else {
		BuildingTile bt = tt.getBuildingTile();
		if (bt == null)
		    return 0;
		type = bt.getType();
	    }

            for (int i = 0; i < typeNumbers.length; i++) {
                if (tt.getTerrainTypeNumber() == typeNumbers[i]) {
                    return 1;
                    //A match
                }
            }
	    return 0;
        }
	return 1;
    }

    abstract public void dumpImages(ImageManager imageManager);

    /**
     * @return a '/' separated relative filename
     */
    protected String generateRelativeFileName(int i) {
	String dir;
	if (layer == LAYER_BUILDING)
	    dir = "buildings";
	else
	    dir = "terrain";

	String num = generateFileNameNumber(i);
	if (num == null) { 
	    return dir + "/" + terrainType + ".png";
	} else {
	    return dir + "/" + terrainType + "_" +
        generateFileNameNumber(i) + ".png";
	}
    }

    protected abstract String generateFileNameNumber(int i);

    protected void setTileIcons(BufferedImage[] tileIcons) {
        this.tileIcons = tileIcons;
    }

    protected BufferedImage[] getTileIcons() {
        return tileIcons;
    }
}
