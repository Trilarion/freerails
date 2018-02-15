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
package freerails.client.renderer.tile;

import freerails.util.Vector2D;
import freerails.util.Utils;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TerrainType;

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

    public AbstractTileRenderer(TerrainType t, int[] rgbValues, ReadOnlyWorld world) {
        mapWidth = world.getMapWidth();
        mapHeight = world.getMapHeight();

        tileModel = Utils.verifyNotNull(t);
        typeNumbers = Utils.verifyNotNull(rgbValues);
    }

    /**
     * @param g
     * @param renderX
     * @param renderY
     * @param mapX
     * @param mapY
     * @param world
     */
    public void renderTile(java.awt.Graphics g, int renderX, int renderY, int mapX, int mapY, ReadOnlyWorld world) {
        Image icon = getIcon(mapX, mapY, world);

        if (null != icon) {
            g.drawImage(icon, renderX, renderY, null);
        }
    }

    /**
     * @return
     */
    public Image getDefaultIcon() {
        return getTileIcons()[0];
    }

    public String getTerrainType() {
        return tileModel.getTerrainTypeName();
    }

    /**
     * Returns an icon for the tile at x,y, which may depend on the terrain
     * types of of the surrounding tiles.
     */
    public Image getIcon(int x, int y, ReadOnlyWorld world) {
        int tile = selectTileIcon(x, y, world);

        if (getTileIcons()[tile] != null) {
            return getTileIcons()[tile];
        }
        throw new NullPointerException("Error in TileView.getIcon: icon no. " + tile + "==null");
    }

    public int selectTileIcon(int x, int y, ReadOnlyWorld world) {
        return 0;
    }

    // TODO remove wo !
    public int checkTile(int x, int y, ReadOnlyWorld world) {
        int match = 0;

        if ((x < mapWidth) && (x >= 0) && (y < mapHeight) && (y >= 0)) {
            for (int typeNumber : typeNumbers) {
                TerrainTile tt = (TerrainTile) world.getTile(new Vector2D(x, y));

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

    public String generateRelativeFileName(int i) {
        return "terrain" + File.separator + getTerrainType() + '_' + generateFileNameNumber(i) + ".png";
    }

    /**
     * @param i
     * @return
     */
    protected abstract String generateFileNameNumber(int i);

    public Image[] getTileIcons() {
        return tileIcons;
    }

    public void setTileIcons(Image[] tileIcons) {
        this.tileIcons = tileIcons;
    }
}