
/*
 *  SquareTileBackgroundPainter.java
 *
 *  Created on 31 July 2001, 16:36
 */
package jfreerails.client.renderer;

import java.awt.Graphics;

/**
 *  This class stores a buffer containing the terrain and track layers of
 *  current visible rectangle of the map. It is responsible of painting these
 *  layers and updating the buffer when the map scrolls or tiles are updated.
 *
 *@author     Luke Lindsay
 *     01 November 2001
 *@version    1.0
 */

final public class SquareTileBackgroundRenderer
	extends BufferedTiledBackgroundRenderer {

	private MapLayerRenderer mapView;

	private final float scale;
	private final float scaleAsInt;

	protected void paintBufferRectangle(int x, int y, int width, int height) {
		java.awt.Graphics gg = bg.create();
		gg.setClip(x, y, width, height);
		gg.translate(-bufferRect.x, -bufferRect.y);
		mapView.paintRect(gg, bufferRect);
	}


	public SquareTileBackgroundRenderer(MapLayerRenderer mv, float _scale) {

		this.scale=_scale;
		scaleAsInt=(int)scale;
		this.mapView = mv;
	}
	public void refreshRectangleOfTiles(int x, int y, int width, int height) {


		java.awt.Graphics gg = bg.create();
		gg.translate(-bufferRect.x, -bufferRect.y);
		gg.clipRect((int) (x * scaleAsInt), (int)(y * scaleAsInt), (int)(width * scaleAsInt), (int)(height * scaleAsInt));
		mapView.paintRectangleOfTiles(gg, x,  y,  width,  height);
	}

	public void paintTile(Graphics g, int tileX, int tileY) {
		mapView.paintTile(g,tileX,tileY);
	}

	public void paintRectangleOfTiles(
		Graphics g,
		int x,
		int y,
		int width,
		int height) {
		mapView.paintRectangleOfTiles(g, x,  y,  width,  height);
	}

	public void refreshTile(int x, int y) {
		Graphics gg = bg.create();
		gg.translate(-bufferRect.x, -bufferRect.y);
		mapView.paintTile(gg, x,y);
	}
}
