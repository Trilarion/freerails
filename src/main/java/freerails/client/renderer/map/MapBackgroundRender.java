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
package freerails.client.renderer.map;

import freerails.util.ui.Painter;
import freerails.client.renderer.*;
import freerails.client.renderer.tile.TileRenderer;
import freerails.client.renderer.tile.TileRendererList;
import freerails.client.renderer.track.TrackPieceRenderer;
import freerails.client.ModelRoot;
import freerails.util.Vector2D;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.WorldConstants;
import freerails.model.terrain.FullTerrainTile;
import freerails.model.terrain.TerrainTile;
import freerails.model.track.NullTrackType;
import freerails.model.track.TrackPiece;
import org.apache.log4j.Logger;

import java.awt.*;

/**
 * Encapsulates the objects that make-up and paint the background of
 * the map view. At present it is composed of two layers: the terrain layer and
 * the track layer.
 */
public class MapBackgroundRender implements MapLayerRenderer {

    private static final Logger logger = Logger.getLogger(MapBackgroundRender.class.getName());

    /**
     * The terrain layer.
     */
    private final TerrainLayer terrainLayer;

    /**
     * The track layer.
     */
    private final TrackLayer trackLayer;
    private final Vector2D tileSize = new Vector2D(WorldConstants.TILE_SIZE, WorldConstants.TILE_SIZE);
    private final Vector2D mapSize;
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
        mapSize = world.getMapSize();
        cityNames = new CityNamesRenderer(world);
        stationNames = new StationNamesRenderer(world, modelRoot);
    }

    /**
     * @param g
     * @param tileLocation
     */
    public void paintTile(Graphics g, Vector2D tileLocation) {
        terrainLayer.paintTile(g, tileLocation);
        trackLayer.paintTile(g, tileLocation);
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
        trackLayer.paintRectangleOfTiles(g, new Vector2D(x, y), width, height);
        Rectangle visibleRectangle = new Rectangle(x * WorldConstants.TILE_SIZE, y * WorldConstants.TILE_SIZE, width * WorldConstants.TILE_SIZE, height * WorldConstants.TILE_SIZE);
        cityNames.paint((Graphics2D) g, visibleRectangle);
        stationNames.paint((Graphics2D) g, visibleRectangle);
    }

    /**
     * @param tileLocation
     */
    public void refreshTile(Vector2D tileLocation) {}

    /**
     *
     */
    public void refreshAll() {}

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
            for (int tileX = tilesToPaint.x - 1; tileX < (tilesToPaint.x + tilesToPaint.width + 1); tileX++) {
                for (int tileY = tilesToPaint.y - 1; tileY < (tilesToPaint.y + tilesToPaint.height + 1); tileY++) {
                    if ((tileX >= 0) && (tileX < mapSize.x) && (tileY >= 0) && (tileY < mapSize.y)) {
                        Vector2D tileLocation = new Vector2D(tileX, tileY);
                        FullTerrainTile fullTerrainTile = (FullTerrainTile) world.getTile(tileLocation);
                        TrackPiece trackPiece = fullTerrainTile.getTrackPiece();
                        int graphicsNumber = trackPiece.getTrackGraphicID();

                        int ruleNumber = trackPiece.getTrackTypeID();
                        if (ruleNumber != NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER) {
                            TrackPieceRenderer trackPieceView = rendererRoot.getTrackPieceView(ruleNumber);
                            trackPieceView.drawTrackPieceIcon(g, graphicsNumber, tileLocation, tileSize);
                        }
                    }
                }
            }
        }

        /**
         * @param g
         * @param tileLocation
         */
        public void paintTile(Graphics g, Vector2D tileLocation) {
            /*
             * Since track tiles overlap the adjacent terrain tiles, we create a
             * temporary Graphics object that only lets us draw on the selected
             * tile.
             */
            paintRectangleOfTiles(g, new Rectangle(tileLocation.x, tileLocation.y, 1, 1));
        }

        private void paintRectangleOfTiles(Graphics g, Vector2D p, int width, int height) {
            paintRectangleOfTiles(g, new Rectangle(p.x, p.y, width, height));
        }

        /**
         * @param tileLocation
         */
        public void refreshTile(Vector2D tileLocation) {}

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
         * @param tileLocation
         */
        public void paintTile(Graphics g, Vector2D tileLocation) {
            Vector2D screenLocation = Vector2D.multiply(tileSize, tileLocation);

            if ((tileLocation.x >= 0) && (tileLocation.x < mapSize.x) && (tileLocation.y >= 0) && (tileLocation.y < mapSize.y)) {
                TerrainTile tt = (TerrainTile) world.getTile(tileLocation);

                int typeNumber = tt.getTerrainTypeID();
                TileRenderer tr = tiles.getTileRendererByIndex(typeNumber);

                if (null == tr) {
                    logger.warn("No tile renderer for " + typeNumber);
                } else {
                    tr.render(g, screenLocation, tileLocation, world);
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
                    terrainLayer.paintTile(g, new Vector2D(tileX, tileY));
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

         private void paintRectangleOfTiles(Graphics g, int x, int y, int width, int height) {
            paintRectangleOfTiles(g, new Rectangle(x, y, width, height));
        }

        /**
         * @param tileLocation
         */
        public void refreshTile(Vector2D tileLocation) {}

        /**
         *
         */
        public void refreshAll() {}
    }
}