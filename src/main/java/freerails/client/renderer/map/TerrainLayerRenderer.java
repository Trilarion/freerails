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

package freerails.client.renderer.map;

import freerails.client.ClientConstants;
import freerails.client.renderer.tile.TileRenderer;
import freerails.client.renderer.tile.TileRendererList;
import freerails.model.terrain.TerrainTile;
import freerails.model.world.UnmodifiableWorld;
import freerails.util.Vector2D;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.awt.*;

/**
 * This inner class represents the terrain of the map.
 */
public final class TerrainLayerRenderer implements MapLayerRenderer {

    private static final TaggedLogger logger = Logger.tag("TerrainLayerRenderer");

    private final TileRendererList tiles;
    private final UnmodifiableWorld world;
    private final Vector2D mapSize;

    /**
     * Creates a terrain layer renderer instance.
     *
     * @param world the game world being queried
     * @param tiles the tiles parameter
     */
    TerrainLayerRenderer(UnmodifiableWorld world, TileRendererList tiles) {
        this.world = world;
        this.tiles = tiles;
        mapSize = world.getMapSize();
    }

    /**
     * Performs the paint tile operation.
     *
     * @param g the graphics context used for drawing
     * @param tileLocation the map coordinate
     */
    @Override
    public void paintTile(Graphics g, Vector2D tileLocation) {
        Vector2D screenLocation = Vector2D.multiply(ClientConstants.TILE_SIZE, tileLocation);

        if (tileLocation.x() >= 0 && tileLocation.x() < mapSize.x() && tileLocation.y() >= 0 && tileLocation.y() < mapSize.y()) {
            TerrainTile terrainTile = world.getTile(tileLocation);

            int typeNumber = terrainTile.terrainTypeId();
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
        for (int tileX = tilesToPaint.x; tileX < tilesToPaint.x + tilesToPaint.width; tileX++) {
            for (int tileY = tilesToPaint.y; tileY < tilesToPaint.y + tilesToPaint.height; tileY++) {
                paintTile(g, new Vector2D(tileX, tileY));
            }
        }
    }

    /**
     * Performs the paint rect operation.
     *
     * @param g the graphics context used for drawing
     * @param visibleRect the visible area to render
     */
    @Override
    public void paintRect(Graphics g, Rectangle visibleRect) {
        throw new UnsupportedOperationException("Method not yet implemented.");
    }

    /**
     * Paint rectangle of tiles.
     * @param g the g
     * @param x the x
     * @param y the y
     * @param width the width
     * @param height the height
     */
    void paintRectangleOfTiles(Graphics g, int x, int y, int width, int height) {
        paintRectangleOfTiles(g, new Rectangle(x, y, width, height));
    }

    /**
     * Performs the refresh tile operation.
     *
     * @param tileLocation the map coordinate
     */
    @Override
    public void refreshTile(Vector2D tileLocation) {}

    /**
     * Performs the refresh all operation.
     */
    @Override
    public void refreshAll() {}
}
