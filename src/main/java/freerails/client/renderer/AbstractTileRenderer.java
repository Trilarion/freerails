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
 * TileView.java
 *
 */
package freerails.client.renderer;

import freerails.world.ReadOnlyWorld;
import freerails.world.terrain.TerrainTile;
import freerails.world.terrain.TerrainType;

import java.awt.*;
import java.io.File;

/**
 * Encapsulates the visible properties of a tile.
 */
public abstract class AbstractTileRenderer implements TileRenderer {
    private final int[] typeNumbers;
    private final int mapWidth;
    private final int mapHeight;
    private final TerrainType tileModel;
    private Image[] tileIcons;

    AbstractTileRenderer(TerrainType t, int[] rgbValues, ReadOnlyWorld w) {
        mapWidth = w.getMapWidth();
        mapHeight = w.getMapHeight();

        tileModel = t;
        typeNumbers = rgbValues;

        if (null == t) {
            throw new NullPointerException();
        }

        if (null == rgbValues) {
            throw new NullPointerException();
        }
    }

    /**
     * @param g
     * @param screenX
     * @param screenY
     * @param mapX
     * @param mapY
     * @param w
     */
    public void renderTile(java.awt.Graphics g, int screenX, int screenY,
                           int mapX, int mapY, ReadOnlyWorld w) {
        Image icon = getIcon(mapX, mapY, w);

        if (null != icon) {
            g.drawImage(icon, screenX, screenY, null);
        }
    }

    /**
     * @return
     */
    public Image getDefaultIcon() {
        return getTileIcons()[0];
    }

    String getTerrainType() {
        return tileModel.getTerrainTypeName();
    }

    /**
     * Returns an icon for the tile at x,y, which may depend on the terrain
     * types of of the surrounding tiles.
     */
    Image getIcon(int x, int y, ReadOnlyWorld w) {
        int tile = selectTileIcon(x, y, w);

        if (getTileIcons()[tile] != null) {
            return getTileIcons()[tile];
        }
        throw new NullPointerException("Error in TileView.getIcon: icon no. "
                + tile + "==null");
    }

    int selectTileIcon(int x, int y, ReadOnlyWorld w) {
        return 0;
    }

    // 666 remove wo !
    int checkTile(int x, int y, ReadOnlyWorld w) {
        int match = 0;

        if ((x < mapWidth) && (x >= 0) && (y < mapHeight) && (y >= 0)) {
            for (int typeNumber : typeNumbers) {
                TerrainTile tt = (TerrainTile) w.getTile(x, y);

                if (tt.getTerrainTypeID() == typeNumber) {
                    match = 1;
                    // A match
                }
            }
        } else {
            match = 1; // A match
            /*
             * If the tile we are checking is off the map, let it be a match.
             * This stops coast appearing where the ocean meets the map edge.
             */
        }
        return match;
    }

    String generateRelativeFileName(int i) {
        return "terrain" + File.separator + getTerrainType() + '_'
                + generateFileNameNumber(i) + ".png";
    }

    /**
     * @param i
     * @return
     */
    protected abstract String generateFileNameNumber(int i);

    Image[] getTileIcons() {
        return tileIcons;
    }

    void setTileIcons(Image[] tileIcons) {
        this.tileIcons = tileIcons;
    }
}