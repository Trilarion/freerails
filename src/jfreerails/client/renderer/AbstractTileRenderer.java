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
package jfreerails.client.renderer;

import java.awt.image.BufferedImage;
import java.io.File;
import jfreerails.client.common.ImageManager;
import jfreerails.world.terrain.TerrainTile;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.ReadOnlyWorld;


/**
*  This class encapsulates the visible properties of a tile.
* @author  Luke Lindsay
*/
public abstract class AbstractTileRenderer implements TileRenderer {
    protected final int[] typeNumbers;
    private BufferedImage[] tileIcons;
    protected final TerrainType tileModel;

    public AbstractTileRenderer(TerrainType t, int[] rgbValues) {
        tileModel = t;
        this.typeNumbers = rgbValues;

        if (null == t) {
            throw new NullPointerException();
        }

        if (null == rgbValues) {
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

    public String getTerrainType() {
        return tileModel.getTerrainTypeName();
    }

    /** Returns an icon for the tile at x,y, which may depend on the terrain types of
     * of the surrounding tiles.
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

    protected int checkTile(int x, int y, ReadOnlyWorld w) {
        int match = 0;

        if (((x < w.getMapWidth()) && (x >= 0)) && (y < w.getMapHeight()) &&
                (y >= 0)) {
            for (int i = 0; i < typeNumbers.length; i++) {
                TerrainTile tt = (TerrainTile)w.getTile(x, y);

                if (tt.getTerrainTypeNumber() == typeNumbers[i]) {
                    match = 1;

                    //A match
                }
            }
        } else {
            match = 1; //A match

            /*If the tile we are checking is off the map, let it be a match.
            This stops coast appearing where the ocean meets the map edge.
            */
        }

        return match;
    }

    abstract public void dumpImages(ImageManager imageManager);

    protected String generateRelativeFileName(int i) {
        return "terrain" + File.separator + this.getTerrainType() + "_" +
        generateFileNameNumber(i) + ".png";
    }

    protected abstract String generateFileNameNumber(int i);

    protected void setTileIcons(BufferedImage[] tileIcons) {
        this.tileIcons = tileIcons;
    }

    protected BufferedImage[] getTileIcons() {
        return tileIcons;
    }
}
