package jfreerails.client.renderer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import jfreerails.world.terrain.TerrainMap;
import jfreerails.world.track.TrackAndTerrainTileMap;
import jfreerails.world.track.TrackMap;

final public class ZoomedOutMapRenderer implements MapRenderer {


	private TerrainMap terrainMap;

	private TrackMap trackSystem;

	private BufferedImage mapImage;//, scaledMapImage;


	protected GraphicsConfiguration defaultConfiguration =
		GraphicsEnvironment
			.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice()
			.getDefaultConfiguration();

	public ZoomedOutMapRenderer(TrackAndTerrainTileMap map) {
		this.terrainMap = map;
		this.trackSystem = map;
		this.refresh();		
		//mapImage.setRGB(0, 0, mapWidth, mapHeight, rgbArrary, 0, mapWidth);

		
	}

	
	
	/*
	 * @see NewMapView#getScale()
	 */
	public float getScale() {
		return 1;
	}

	
	

	/*
	 * @see NewMapView#paintRect(Graphics, Rectangle)
	 */
	public void paintRect(Graphics g, Rectangle visibleRect) {
		g.drawImage(mapImage, 0, 0, null);
	}

	

	/*
	 * @see NewMapView#refreshTile(Point)
	 */
	public void refreshTile(Point tile) {

		int rgb;
//		TrackNode node = trackSystem.getTrackNode(tile);
//		if (node != null) {
//			rgb = node.getRGB();
//		} else {
//			rgb = terrainMap.
			rgb = trackSystem.getRGB(tile);
		//}

		mapImage.setRGB(tile.x, tile.y, rgb);

		//mapImage.setRGB(tile.x, tile.y, 0);
	}

	/*
	 * @see NewMapView#refresh()
	 */
	public void refresh() {
		int mapWidth = terrainMap.getWidth();
		int mapHeight = terrainMap.getHeight();
		mapImage =
			defaultConfiguration.createCompatibleImage(
				mapWidth,
				mapHeight,
				Transparency.OPAQUE);

		//int[] rgbArrary=new int[mapWidth*mapWidth];

		Point tile = new Point();

		for (tile.x = 0; tile.x < mapWidth; tile.x++) {
			for (tile.y = 0; tile.y < mapHeight; tile.y++) {
				refreshTile(tile);
				//rgbArrary[x+(y*mapWidth)]=terrainMap.getTerrainTileType(x,y);
			}
		}
	}

	

	/*
	 * @see NewMapView#getMapSizeInPixels()
	 */
	public Dimension getMapSizeInPixels() {
		return new Dimension(terrainMap.getWidth(), terrainMap.getHeight());
	}
	
	public void paintTile(Graphics g, int tileX, int tileY) {
		g.drawImage(mapImage, 0, 0, null);
	}

	public void paintRectangleOfTiles(
		Graphics g,
		int x,
		int y,
		int width,
		int height) {
			g.drawImage(mapImage, 0, 0, null);
	}

	public void refreshTile(int x, int y) {
		refreshTile(new Point(x,y));
	}

	public void refreshRectangleOfTiles(int x, int y, int width, int height) {
		for (int xx=x;xx<x+width;xx++){
			for(int yy=y;yy<y+height;yy++){
				refreshTile(new Point (xx,yy));
			}
		}				
	}

}
