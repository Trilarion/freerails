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
 *  BackgroundMapView.java
 *
 *  Created on 06 August 2001, 17:21
 */
package freerails.client.renderer;

import freerails.client.common.Painter;
import freerails.controller.ModelRoot;
import freerails.world.ReadOnlyWorld;
import freerails.world.WorldConstants;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.terrain.TerrainTile;
import freerails.world.track.NullTrackType;
import freerails.world.track.TrackPiece;
import org.apache.log4j.Logger;

import java.awt.*;

/**
 * Encapsulates the objects that make-up and paint the background of
 * the map view. At present it is composed of two layers: the terrain layer and
 * the track layer.
 */
public final class MapBackgroundRender implements MapLayerRenderer {

    private static final Logger logger = Logger.getLogger(MapBackgroundRender.class.getName());

    /**
     * The terrain layer.
     */
    private final TerrainLayer terrainLayer;

    /**
     * The track layer.
     */
    private final TrackLayer trackLayer;
    private final Dimension tileSize = new Dimension(WorldConstants.TILE_SIZE, WorldConstants.TILE_SIZE);
    private final Dimension mapSize;
    private final Painter cityNames;
    private final Painter stationNames;

    /*
     * Used to avoid having to create a new rectangle for each call to the paint
     * methods.
     */
    private Rectangle clipRectangle = new Rectangle();

    /**
     * @param world
     * @param rendererRoot
     * @param modelRoot
     */
    public MapBackgroundRender(ReadOnlyWorld world, RendererRoot rendererRoot, ModelRoot modelRoot) {
        trackLayer = new TrackLayer(world, rendererRoot);
        terrainLayer = new TerrainLayer(world, rendererRoot);
        mapSize = new Dimension(world.getMapWidth(), world.getMapHeight());
        cityNames = new CityNamesRenderer(world);
        stationNames = new StationNamesRenderer(world, modelRoot);
    }

    /**
     * @param g
     * @param tileX
     * @param tileY
     */
    public void paintTile(Graphics g, int tileX, int tileY) {
        terrainLayer.paintTile(g, tileX, tileY);
        trackLayer.paintTile(g, tileX, tileY);
        cityNames.paint((Graphics2D) g, null);
        stationNames.paint((Graphics2D) g, null);
    }

    /**
     * @param g
     * @param visibleRect
     */
    public void paintRect(Graphics g, Rectangle visibleRect) {
        int tileWidth = WorldConstants.TILE_SIZE;
        int tileHeight = WorldConstants.TILE_SIZE;

        clipRectangle = g.getClipBounds(clipRectangle);

        int x = clipRectangle.x / tileWidth;
        int y = clipRectangle.y / tileHeight;
        int width = (clipRectangle.width / tileWidth) + 2;
        int height = (clipRectangle.height) / tileHeight + 2;

        paintRectangleOfTiles(g, x, y, width, height);
        cityNames.paint((Graphics2D) g, visibleRect);
        stationNames.paint((Graphics2D) g, visibleRect);
    }

    private void paintRectangleOfTiles(Graphics g, int x, int y, int width, int height) {
        terrainLayer.paintRectangleOfTiles(g, x, y, width, height);
        trackLayer.paintRectangleOfTiles(g, x, y, width, height);
        Rectangle visibleRectangle = new Rectangle(x * WorldConstants.TILE_SIZE, y * WorldConstants.TILE_SIZE, width * WorldConstants.TILE_SIZE, height * WorldConstants.TILE_SIZE);
        cityNames.paint((Graphics2D) g, visibleRectangle);
        stationNames.paint((Graphics2D) g, visibleRectangle);
    }

    /**
     * @param x
     * @param y
     */
    public void refreshTile(int x, int y) {
        // Do nothing
    }

    /**
     *
     */
    public void refreshAll() {
        // Do nothing
    }

    /**
     * This inner class represents a view of the track on the map.
     */
    public final class TrackLayer implements MapLayerRenderer {

        private final ReadOnlyWorld world;
        private final RendererRoot rendererRoot;

        /**
         * @param world
         * @param trackPieceViewList
         */
        private TrackLayer(ReadOnlyWorld world, RendererRoot trackPieceViewList) {
            rendererRoot = trackPieceViewList;
            this.world = world;
        }

        /**
         * Paints a rectangle of tiles onto the supplied graphics context.
         *
         * @param g            The graphics context on which the tiles get painted.
         * @param tilesToPaint The rectangle, measured in tiles, to paint.
         */
        private void paintRectangleOfTiles(Graphics g, Rectangle tilesToPaint) {
            /*
             * Track can overlap the adjacent terrain tiles by half a tile. This
             * means that we need to paint the track from the tiles bordering
             * the specified rectangle of tiles (tilesToPaint). To prevent
             * unnecessary painting, we set the clip to expose only the rectangle
             * of tilesToPaint.
             */
            Point tile = new Point();

            for (tile.x = tilesToPaint.x - 1; tile.x < (tilesToPaint.x + tilesToPaint.width + 1); tile.x++) {
                for (tile.y = tilesToPaint.y - 1; tile.y < (tilesToPaint.y + tilesToPaint.height + 1); tile.y++) {
                    if ((tile.x >= 0) && (tile.x < mapSize.width) && (tile.y >= 0) && (tile.y < mapSize.height)) {
                        FullTerrainTile ft = (FullTerrainTile) world.getTile(tile.x, tile.y);
                        TrackPiece tp = ft.getTrackPiece();

                        int graphicsNumber = tp.getTrackGraphicID();

                        int ruleNumber = tp.getTrackTypeID();
                        if (ruleNumber != NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER) {
                            TrackPieceRenderer trackPieceView = rendererRoot.getTrackPieceView(ruleNumber);

                            trackPieceView.drawTrackPieceIcon(graphicsNumber, g, tile.x, tile.y, tileSize);
                        }
                    }
                }
            }
        }

        /**
         * @param g
         * @param tileX
         * @param tileY
         */
        public void paintTile(Graphics g, int tileX, int tileY) {
            /*
             * Since track tiles overlap the adjacent terrain tiles, we create a
             * temporary Graphics object that only lets us draw on the selected
             * tile.
             */
            paintRectangleOfTiles(g, new Rectangle(tileX, tileY, 1, 1));
        }

        private void paintRectangleOfTiles(Graphics g, int x, int y, int width, int height) {
            paintRectangleOfTiles(g, new Rectangle(x, y, width, height));
        }

        /**
         * @param x
         * @param y
         */
        public void refreshTile(int x, int y) {
        }

        /**
         * @param g
         * @param visibleRect
         */
        public void paintRect(Graphics g, Rectangle visibleRect) {
            throw new UnsupportedOperationException("Method not yet implemented.");
        }

        /**
         *
         */
        public void refreshAll() {
        }
    }

    /**
     * This inner class represents the terrain of the map.
     */
    public final class TerrainLayer implements MapLayerRenderer {
        private final TileRendererList tiles;

        private final ReadOnlyWorld world;

        /**
         * @param world
         * @param tiles
         */
        private TerrainLayer(ReadOnlyWorld world, TileRendererList tiles) {
            this.world = world;
            this.tiles = tiles;
        }

        /**
         * @param g
         * @param tile
         */
        private void paintTile(Graphics g, Point tile) {
            int screenX = tileSize.width * tile.x;
            int screenY = tileSize.height * tile.y;

            if ((tile.x >= 0) && (tile.x < mapSize.width) && (tile.y >= 0) && (tile.y < mapSize.height)) {
                TerrainTile tt = (TerrainTile) world.getTile(tile.x, tile.y);

                int typeNumber = tt.getTerrainTypeID();
                TileRenderer tr = tiles.getTileViewWithNumber(typeNumber);

                if (null == tr) {
                    logger.warn("No tile renderer for " + typeNumber);
                } else {
                    tr.renderTile(g, screenX, screenY, tile.x, tile.y, world);
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
            Point tile = new Point();

            for (tile.x = tilesToPaint.x; tile.x < (tilesToPaint.x + tilesToPaint.width); tile.x++) {
                for (tile.y = tilesToPaint.y; tile.y < (tilesToPaint.y + tilesToPaint.height); tile.y++) {
                    terrainLayer.paintTile(g, tile);
                }
            }
        }

        /**
         * @param g
         * @param visibleRect
         */
        public void paintRect(Graphics g, Rectangle visibleRect) {
            throw new UnsupportedOperationException("Method not yet implemented.");
        }

        /**
         * @param g
         * @param tileX
         * @param tileY
         */
        public void paintTile(Graphics g, int tileX, int tileY) {
            paintTile(g, new Point(tileX, tileY));
        }

        private void paintRectangleOfTiles(Graphics g, int x, int y, int width, int height) {
            paintRectangleOfTiles(g, new Rectangle(x, y, width, height));
        }

        /**
         * @param x
         * @param y
         */
        public void refreshTile(int x, int y) {
        }

        /**
         *
         */
        public void refreshAll() {
        }
    }
}