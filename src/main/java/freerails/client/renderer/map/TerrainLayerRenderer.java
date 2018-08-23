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

package freerails.client.renderer.map;

import freerails.client.ClientConstants;
import freerails.client.renderer.tile.TileRenderer;
import freerails.client.renderer.tile.TileRendererList;
import freerails.model.terrain.TerrainTile;
import freerails.model.world.UnmodifiableWorld;
import freerails.util.Vec2D;
import org.apache.log4j.Logger;

import java.awt.*;

/**
 * This inner class represents the terrain of the map.
 */
public final class TerrainLayerRenderer implements MapLayerRenderer {

    private static final Logger logger = Logger.getLogger(TerrainLayerRenderer.class.getName());

    private final TileRendererList tiles;
    private final UnmodifiableWorld world;
    private final Vec2D mapSize;

    /**
     * @param world
     * @param tiles
     */
    TerrainLayerRenderer(UnmodifiableWorld world, TileRendererList tiles) {
        this.world = world;
        this.tiles = tiles;
        mapSize = world.getMapSize();
    }

    /**
     * @param g
     * @param tileLocation
     */
    @Override
    public void paintTile(Graphics g, Vec2D tileLocation) {
        Vec2D screenLocation = Vec2D.multiply(ClientConstants.TILE_SIZE, tileLocation);

        if ((tileLocation.x >= 0) && (tileLocation.x < mapSize.x) && (tileLocation.y >= 0) && (tileLocation.y < mapSize.y)) {
            TerrainTile terrainTile = world.getTile(tileLocation);

            int typeNumber = terrainTile.getTerrainTypeId();
            TileRenderer tileRenderer = tiles.getTileRendererByIndex(typeNumber);

            if (null == tileRenderer) {
                logger.warn("No tile renderer for " + typeNumber);
            } else {
                tileRenderer.render(g, screenLocation, tileLocation, world);
            }
        }
    }

    /**
     * Paints a rectangle of tiles on the supplied graphics context.
     *
     * @param g            The graphics context.
     * @param tilesToPaint The rectangle, measured in tiles, to paint.
     */
    private void paintRectangleOfTiles(Graphics g, Rectangle tilesToPaint) {
        for (int tileX = tilesToPaint.x; tileX < (tilesToPaint.x + tilesToPaint.width); tileX++) {
            for (int tileY = tilesToPaint.y; tileY < (tilesToPaint.y + tilesToPaint.height); tileY++) {
                paintTile(g, new Vec2D(tileX, tileY));
            }
        }
    }

    /**
     * @param g
     * @param visibleRect
     */
    @Override
    public void paintRect(Graphics g, Rectangle visibleRect) {
        throw new UnsupportedOperationException("Method not yet implemented.");
    }

    void paintRectangleOfTiles(Graphics g, int x, int y, int width, int height) {
        paintRectangleOfTiles(g, new Rectangle(x, y, width, height));
    }

    /**
     * @param tileLocation
     */
    @Override
    public void refreshTile(Vec2D tileLocation) {}

    /**
     *
     */
    @Override
    public void refreshAll() {}
}
