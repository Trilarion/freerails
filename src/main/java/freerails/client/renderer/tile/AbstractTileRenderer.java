/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.Terrain;
import freerails.util.Vector2D;
import freerails.util.Utils;
import freerails.model.world.UnmodifiableWorld;

import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Encapsulates the visible properties of a tile.
 */
public abstract class AbstractTileRenderer implements TileRenderer {

    private final List<Integer> typesTreatedAsSame;
    private final Terrain terrainType;
    private Image[] tileIcons;

    // TODO if only mapSize is needed, do not send the full world here
    /**
     * Creates a abstract tile renderer instance.
     * @param terrainType the terrain type
     * @param typesTreatedAsSame the types treated as same
     * @param numberTileIcons the number tile icons
     */
    public AbstractTileRenderer(Terrain terrainType, List<Integer> typesTreatedAsSame, int numberTileIcons) {
        this.terrainType = Utils.verifyNotNull(terrainType);
        this.typesTreatedAsSame = Utils.verifyNotNull(typesTreatedAsSame);
        tileIcons = new Image[numberTileIcons]; // TODO check > 0
    }

    /**
     * Performs the render operation.
     *
     * @param g the graphics context used for drawing
     * @param renderLocation the map coordinate
     * @param mapLocation the map coordinate
     * @param world the game world being queried
     */
    @Override
    public void render(Graphics g, Vector2D renderLocation, Vector2D mapLocation, UnmodifiableWorld world) {
        Image icon = getIcon(mapLocation, world);

        if (null != icon) {
            g.drawImage(icon, renderLocation.x(), renderLocation.y(), null);
        }
    }

    /**
     * Returns the default icon.
     *
     * @return the default icon produced by this method
     */
    @Override
    public Image getDefaultIcon() {
        return tileIcons[0];
    }

    /**
     * Returns the terrain type name.
     * @return the terrain type name
     */
    public String getTerrainTypeName() {
        return terrainType.getName();
    }

    /**
     * Returns an icon for the tile at x,y, which may also depend on the terrain types of the surrounding tiles.
     * <p>
     * In the standard implementation it doesn't though.
     */
    public Image getIcon(Vector2D mapLocation, UnmodifiableWorld world) {
        int index = selectTileIconIndex(mapLocation, world);

        // TODO here we can assume that all icons in the array are not null, however, make sure of that before
        return Utils.verifyNotNull(tileIcons[index], String.format("TileRenderer.getIcon: icon at index %d is null", index));
    }

    /**
     * Which of the stored icons to use for that specific map location. May also depend on surrounding icons.
     *
     * @param mapLocation the map coordinate
     * @param world the game world being queried
     * @return the tile icon index produced by this method
     */
    public abstract int selectTileIconIndex(Vector2D mapLocation, UnmodifiableWorld world);

    // TODO remove world !
    /**
     * Checks whether the renderer handles the tile at the specified location.
     * @param location the location
     * @param world the world
     * @return {@code true} if the tile is handled by this renderer
     */
    public int checkTile(Vector2D location, UnmodifiableWorld world) {
        int match = 0;

        Vector2D mapSize = world.getMapSize();
        if (location.below(mapSize) && location.aboveOrEqual(Vector2D.ZERO)) {
            for (int typeNumber : typesTreatedAsSame) {
                TerrainTile terrainTile = world.getTile(location);

                if (terrainTile.terrainTypeId() == typeNumber) {
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

    /**
     * Generates the relative filename for the specified tile icon.
     * @param i the i
     * @return the relative icon filename
     */
    public String generateRelativeFileName(int i) {
        return "terrain" + File.separator + getTerrainTypeName() + '_' + generateFileNameNumber(i) + ".png";
    }

    /**
     * Performs the generate file name number operation.
     *
     * @param i the index of the item
     * @return the file name number produced by this method
     */
    protected abstract String generateFileNameNumber(int i);

    /**
     * Only for subclasses.
     *
     * @return the tile icons produced by this method
     */
    protected Image[] getTileIcons() {
        return tileIcons;
    }
}