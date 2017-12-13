/*
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

package jfreerails.client.renderer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackPiece;

/**
 * This class draws the voerview map.
 */
final public class ZoomedOutMapRenderer implements MapRenderer {
    private final int imageWidth;
    private final int imageHeight;
    private final int mapWidth;
    private final int mapHeight;
    private final int mapX;
    private final int mapY;
    private ReadOnlyWorld w;
    private BufferedImage one2oneImage;
    private BufferedImage mapImage;
    private final AffineTransform affineTransform;
    private Graphics2D mapGraphics;
    protected GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                                              .getDefaultScreenDevice()
                                                                              .getDefaultConfiguration();

    public ZoomedOutMapRenderer(ReadOnlyWorld world) {
	this(world, world.getMapHeight());
    }

    public ZoomedOutMapRenderer(ReadOnlyWorld world, int height) {
	this(world, height * world.getMapWidth() / world.getMapHeight(),
		height, 0, 0, world.getMapWidth(), world.getMapHeight());
    }

    public ZoomedOutMapRenderer(ReadOnlyWorld world, int width, int height, int
	    mapX, int mapY, int mapWidth, int mapHeight) {
	w = world;
	this.mapWidth = mapWidth;
	this.mapHeight = mapHeight;
	imageHeight = height;
	imageWidth = width;
	double scalingFactor = ((double) imageHeight) / mapHeight;
	affineTransform = AffineTransform.getScaleInstance(scalingFactor,
		scalingFactor);
	this.mapX = mapX;
	this.mapY = mapY;
	refresh();
    }

    public float getScale() {
        return imageHeight / mapHeight;
    }

    public void paintRect(Graphics g, Rectangle visibleRect) {
        g.drawImage(mapImage, 0, 0, null);
    }

    /**
     * @param tile map coords of tile to draw
     */
    private void refreshTile(Point tile) {
        int rgb;

        FreerailsTile tt = w.getTile(tile.x, tile.y);

        if (tt.getTrackPiece().equals(NullTrackPiece.getInstance())) {
		int typeNumber = tt.getTerrainTypeNumber();
		TerrainType terrainType = (TerrainType)w.get(KEY.TERRAIN_TYPES,
			typeNumber);
		one2oneImage.setRGB(tile.x, tile.y, terrainType.getRGB());
        } else {
            /* black with alpha of 1 */
            one2oneImage.setRGB(tile.x, tile.y, 0xff000000);
        }
	int scaledX = (tile.x - mapX) * imageWidth / mapWidth;
	int scaledY = (tile.y - mapY) * imageHeight / mapHeight;
	int minx = scaledX < 1 ? 0 : scaledX - 1;
	int miny = scaledY < 1 ? 0 : scaledY - 1;
	int maxx = scaledX > imageWidth - 2 ? imageWidth : scaledX + 2;
	int maxy = scaledY > imageHeight - 2 ? imageHeight : scaledY + 2;

	mapGraphics.setClip(minx, miny, maxx - minx, maxy - miny);
	mapGraphics.drawImage(one2oneImage, affineTransform,  null);
    }

    /**
     * redraw the whole map onto a new buffer
     */
    private void refresh() {
	/* free up memory used by the old image */
	if (mapImage != null)
	    mapImage.flush();
	if (one2oneImage != null)
	    one2oneImage.flush();
	if (mapGraphics != null)
	    mapGraphics.dispose();

	/* generate a 1:1 map of the terrain layer */
	one2oneImage =
	    defaultConfiguration.createCompatibleImage(mapWidth, mapHeight);

	mapImage = defaultConfiguration.createCompatibleImage(imageWidth,
		imageHeight);

	mapGraphics = mapImage.createGraphics();

        Point tile = new Point();
        for (tile.x = mapX; tile.x < mapWidth + mapX; tile.x++) {
            for (tile.y = mapY; tile.y < mapHeight + mapY; tile.y++) {
		int rgb;

		FreerailsTile tt = w.getTile(tile.x, tile.y);

		if (tt.getTrackPiece().equals(NullTrackPiece.getInstance())) {
		    int typeNumber = tt.getTerrainTypeNumber();
		    TerrainType terrainType = (TerrainType)w.get(KEY.TERRAIN_TYPES,
			    typeNumber);
		    one2oneImage.setRGB(tile.x - mapX, tile.y - mapY,
			    terrainType.getRGB());
		} else {
		    /* black with alpha of 1 */
		    one2oneImage.setRGB(tile.x - mapX,
			    tile.y - mapY, 0xff000000);
		}
            }
        }
	mapGraphics.setClip(0, 0, imageWidth, imageHeight);
	mapGraphics.drawImage(one2oneImage, affineTransform,  null);
    }

    /*
     * @see NewMapView#getMapSizeInPixels()
     */
    public Dimension getMapSizeInPixels() {
        return new Dimension(imageWidth, imageHeight);
    }

    public void paintTile(Graphics g, int tileX, int tileY) {
        g.drawImage(mapImage, 0, 0, null);
    }

    public void refreshTile(int x, int y) {
        refreshTile(new Point(x, y));
    }
}
