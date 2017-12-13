/*
 * Copyright (C) 2001 Luke Lindsay
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
 *  BackgroundMapView.java
 *
 *  Created on 06 August 2001, 17:21
 */
package org.railz.client.renderer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.logging.*;

import org.railz.world.terrain.TerrainTile;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.track.*;

/**
 * This class encapsulates the objects that make-up and paint the background
 * of the map view. At present it is composed of two layers: the terrain layer
 * and the track layer.
 *
 * @author Luke Lindsay
 *   21 September 2001
 * @version 1
 */
final public class MapBackgroundRender implements MapLayerRenderer {
    private static final Logger logger = Logger.getLogger("global");

    /**
     * The building layer.
     */
    private BuildingLayerRenderer buildingLayer;

    /**
     * The terrain layer.
     */
    private TerrainLayer terrainLayer;

    /**
     * The track layer.
     */
    private TrackLayer trackLayer;
    private Dimension mapSize;

    /*Used to avoid having to create a new rectangle for each call to
     *the paint methods.
     */
    private Rectangle clipRectangle = new Rectangle();

    /**
     *  This innner class represents a view of the track on the map.
     *
     * @author     Luke Lindsay
     *     21 September 2001
     */
    final public class TrackLayer implements MapLayerRenderer {
        private ReadOnlyWorld w;
        private TrackPieceRendererList trackPieceViewList;

        /**
	 * Paints a rectangle of tiles onto the supplied
         * graphics context.
         * @param g The graphics context on which the tiles
         * get painted.
         * @param tilesToPaint The rectangle, measured in tiles, to
         * paint.
         */
        public void paintRectangleOfTiles(Graphics g, Rectangle tilesToPaint) {
	    /*
	     * Track can overlap the adjacent terrain tiles by half a tile.
	     * This means that we need to paint the track from the tiles
	     * bordering the specified rectangle *of tiles (tilesToPaint).  To
	     * prevent unnecessay painting, we set the clip to expose only the
	     * rectangle of tilesToPaint.
             */
            Graphics tempG = g;
            Point tile = new Point();

            for (tile.x = tilesToPaint.x - 1;
                    tile.x < (tilesToPaint.x + tilesToPaint.width + 1);
                    tile.x++) {
                for (tile.y = tilesToPaint.y - 1;
                        tile.y < (tilesToPaint.y + tilesToPaint.height + 1);
                        tile.y++) {
                    if ((tile.x >= 0) && (tile.x < mapSize.width) &&
                            (tile.y >= 0) && (tile.y < mapSize.height)) {
                        TrackTile tp = (TrackTile)w.getTile(tile.x,
				tile.y).getTrackTile();
			if (tp == null)
			    continue;
                        byte trackConfig = tp.getTrackConfiguration();
                        int ruleNumber = tp.getTrackRule();
                        TrackPieceRenderer trackPieceView =
                            trackPieceViewList.getTrackPieceView(ruleNumber);
                        trackPieceView.drawTrackPieceIcon(trackConfig,
                            tempG, tile.x, tile.y, TileRenderer.TILE_SIZE);
                    }
                }
            }
        }

        public void paintTile(Graphics g, int tileX, int tileY) {
            /*
	     *  Since track tiles overlap the adjacent terrain tiles, we create
	     *  a temporary Graphics object that only lets us draw on the
	     *  selected tile.
             */
            paintRectangleOfTiles(g, new Rectangle(tileX, tileY, 1, 1));
        }

        private void paintRectangleOfTiles(Graphics g, int x, int y, int width,
            int height) {
            paintRectangleOfTiles(g, new Rectangle(x, y, width, height));
        }

        public void refreshTile(int x, int y) {
        }

        public void paintRect(Graphics g, Rectangle visibleRect) {
            throw new UnsupportedOperationException(
                "Method not yet implemented.");
        }

        public TrackLayer(ReadOnlyWorld world,
            TrackPieceRendererList trackPieceViewList) {
            this.trackPieceViewList = trackPieceViewList;
            this.w = world;
        }
    }

    /**
     *  This inner class represents the terrain of the map.
     *
     * @author     Luke Lindsay
     *     21 September 2001
     */
    final public class TerrainLayer implements MapLayerRenderer {
        private TileRendererList tiles;
        private ReadOnlyWorld w;

        public void paintTile(Graphics g, Point tile) {
            int screenX = TileRenderer.TILE_SIZE.width * tile.x;
            int screenY = TileRenderer.TILE_SIZE.height * tile.y;

            if ((tile.x >= 0) && (tile.x < mapSize.width) && (tile.y >= 0) &&
                    (tile.y < mapSize.height)) {
                TerrainTile tt = (TerrainTile)w.getTile(tile.x, tile.y);

                int typeNumber = tt.getTerrainTypeNumber();
                TileRenderer tr = tiles.getTileViewWithNumber(typeNumber);

                if (null == tr) {
                    logger.log(Level.SEVERE, 
			    "No tile renderer for " + typeNumber);
                } else {
                    tr.renderTile(g, screenX, screenY, tile.x, tile.y, w);
                }
            }
        }

        /**
	 * Paints a rectangle of tiles on the supplied graphics
         * context.
         * @param g The grahics context.
	 * @param tilesToPaint The rectangle defining the area of the map to
	 * draw, measured in tile coordinates, to paint.
         */
        public void paintRectangleOfTiles(Graphics g, Rectangle tilesToPaint) {
            Point tile = new Point();

            for (tile.x = tilesToPaint.x;
                    tile.x < (tilesToPaint.x + tilesToPaint.width); tile.x++) {
                for (tile.y = tilesToPaint.y;
                        tile.y < (tilesToPaint.y + tilesToPaint.height);
                        tile.y++) {
                    terrainLayer.paintTile(g, tile);
                }
            }
        }

        public void paintRect(Graphics g, Rectangle visibleRect) {
            throw new UnsupportedOperationException(
                "Method not yet implemented.");
        }

        public void paintTile(Graphics g, int tileX, int tileY) {
            paintTile(g, new Point(tileX, tileY));
        }

	/**
	 * @param g graphics context on which to draw, origin at top left of
	 * map origin
	 * @param x map coord in tiles of area to draw
	 * @param y map coord in tiles of area to draw
	 * @param width width in tiles of area to draw
	 * @param height height in tiles of area to draw
	 */
        private void paintRectangleOfTiles(Graphics g, int x, int y, int width,
            int height) {
            paintRectangleOfTiles(g, new Rectangle(x, y, width, height));
        }

        public void refreshTile(int x, int y) {
        }

        public TerrainLayer(ReadOnlyWorld world, TileRendererList tiles) {
            this.w = world;
            this.tiles = tiles;
        }
    }

    public MapBackgroundRender(ReadOnlyWorld w, ViewLists vl) {
        trackLayer = new TrackLayer(w, vl.getTrackPieceViewList());
        terrainLayer = new TerrainLayer(w, vl.getTileViewList());
	buildingLayer = new BuildingLayerRenderer(w, vl);
        mapSize = new Dimension(w.getMapWidth(), w.getMapHeight());
    }

    public void paintTile(Graphics g, int x, int y) {
        terrainLayer.paintTile(g, x, y);
        trackLayer.paintTile(g, x, y);
	buildingLayer.paintTile(g, x, y);
    }

    /**
     * @param g Graphics context with origin pointing to top left corner of
     * viewport.
     * @param visibleRect rectangle defining area of map to draw relative to
     * origin 0,0 at top left of map, measured in pixels.
     */
    public void paintRect(Graphics g, Rectangle visibleRect) {
        int x = visibleRect.x / TileRenderer.TILE_SIZE.width;
        int y = visibleRect.y / TileRenderer.TILE_SIZE.height;
        int width = (visibleRect.width + visibleRect.x - 1) /
	    TileRenderer.TILE_SIZE.width - x + 1;
        int height = (visibleRect.height + visibleRect.y - 1) /
	    TileRenderer.TILE_SIZE.height - y + 1;
        paintRectangleOfTiles(g, x, y, width, height);
    }

    /**
     * @param g graphics context on which to draw, origin at top left of
     * area to draw
     * @param x map coord in tiles of area to draw
     * @param y map coord in tiles of area to draw
     * @param width width in tiles of area to draw
     * @param height height in tiles of area to draw
     */
    private void paintRectangleOfTiles(Graphics g, int x, int y, int width,
        int height) {
        terrainLayer.paintRectangleOfTiles(g, x, y, width, height);
        trackLayer.paintRectangleOfTiles(g, x, y, width, height);
	buildingLayer.paintRectangleOfTiles(g, x, y, width, height);
    }

    public void refreshTile(int x, int y) {
    }
}
