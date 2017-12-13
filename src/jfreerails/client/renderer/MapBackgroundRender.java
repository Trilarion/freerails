/*
 *  BackgroundMapView.java
 *
 *  Created on 06 August 2001, 17:21
 */
package jfreerails.client.renderer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import jfreerails.world.terrain.TerrainTile;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.track.TrackPiece;

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
    /**
     * The terrain layer.
     */
    protected TerrainLayer terrainLayer;

    /**
     * The track layer.
     */
    protected TrackLayer trackLayer;
    private Dimension tileSize = new Dimension(30, 30);
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
                        TrackPiece tp = (TrackPiece)w.getTile(tile.x, tile.y);

                        int graphicsNumber = tp.getTrackGraphicNumber();

                        int ruleNumber = tp.getTrackRule().getRuleNumber();
                        jfreerails.client.renderer.TrackPieceRenderer trackPieceView =
                            trackPieceViewList.getTrackPieceView(ruleNumber);
                        trackPieceView.drawTrackPieceIcon(graphicsNumber,
                            tempG, tile.x, tile.y, tileSize);
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
            int screenX = tileSize.width * tile.x;
            int screenY = tileSize.height * tile.y;

            if ((tile.x >= 0) && (tile.x < mapSize.width) && (tile.y >= 0) &&
                    (tile.y < mapSize.height)) {
                TerrainTile tt = (TerrainTile)w.getTile(tile.x, tile.y);

                int typeNumber = tt.getTerrainTypeNumber();
                TileRenderer tr = tiles.getTileViewWithNumber(typeNumber);

                if (null == tr) {
                    System.err.println("No tile renderer for " + typeNumber);
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

    public MapBackgroundRender(ReadOnlyWorld w, TileRendererList tiles,
        TrackPieceRendererList trackPieceViewList) {
        trackLayer = new TrackLayer(w, trackPieceViewList);
        terrainLayer = new TerrainLayer(w, tiles);
        mapSize = new Dimension(w.getMapWidth(), w.getMapHeight());
    }

    public void paintTile(Graphics g, int x, int y) {
        terrainLayer.paintTile(g, x, y);
        trackLayer.paintTile(g, x, y);
    }

    /**
     * @param g Graphics context with origin pointing to top left corner of
     * viewport.
     * @param visibleRect rectangle defining area of map to draw relative to
     * origin 0,0 at top left of map, measured in pixels.
     */
    public void paintRect(Graphics g, Rectangle visibleRect) {
        int tileWidth = 30;
        int tileHeight = 30;

        int x = visibleRect.x / tileWidth;
        int y = visibleRect.y / tileHeight;
        int width = visibleRect.width / tileWidth + 2;
        int height = visibleRect.height / tileHeight + 2;
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
    }

    public void refreshTile(int x, int y) {
    }
}
