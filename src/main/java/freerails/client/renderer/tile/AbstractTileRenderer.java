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
    private final TerrainType terrainType;
    private Image[] tileIcons;

    // TODO if only mapSize is needed, do not send the full world here
    public AbstractTileRenderer(TerrainType terrainType, int[] rgbValues, int numberTileIcons) {
        this.terrainType = Utils.verifyNotNull(terrainType);
        typeNumbers = Utils.verifyNotNull(rgbValues);
        tileIcons = new Image[numberTileIcons]; // TODO check > 0
    }

    /**
     * @param g
     * @param renderLocation
     * @param mapLocation
     * @param world
     */
    public void render(Graphics g, Vector2D renderLocation, Vector2D mapLocation, ReadOnlyWorld world) {
        Image icon = getIcon(mapLocation, world);

        if (null != icon) {
            g.drawImage(icon, renderLocation.x, renderLocation.y, null);
        }
    }

    /**
     * @return
     */
    public Image getDefaultIcon() {
        return tileIcons[0];
    }

    public String getTerrainType() {
        return terrainType.getTerrainTypeName();
    }

    /**
     * Returns an icon for the tile at x,y, which may also depend on the terrain types of of the surrounding tiles.
     *
     * In the standard implementation it doesn't though.
     */
    public Image getIcon(Vector2D mapLocation, ReadOnlyWorld world) {
        int index = selectTileIconIndex(mapLocation, world);

        return Utils.verifyNotNull(tileIcons[index], String.format("TileRenderer.getIcon: icon at index %d is null", index));
    }

    /**
     * Which of the stored icons to use for that specific map location. May also depend on surrounding icons.
     *
     * @param mapLocation
     * @param world
     * @return
     */
    public abstract int selectTileIconIndex(Vector2D mapLocation, ReadOnlyWorld world);

    // TODO remove world !
    public int checkTile(Vector2D location, ReadOnlyWorld world) {
        int match = 0;

        // TODO vector2D arithmetics
        Vector2D mapSize = world.getMapSize();
        if ((location.x < mapSize.x) && (location.x >= 0) && (location.y < mapSize.y) && (location.y >= 0)) {
            for (int typeNumber : typeNumbers) {
                TerrainTile terrainTile = (TerrainTile) world.getTile(location);

                if (terrainTile.getTerrainTypeID() == typeNumber) {
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

    /**
     * Only for subclasses.
     *
     * @return
     */
    protected Image[] getTileIcons() {
        return tileIcons;
    }
}