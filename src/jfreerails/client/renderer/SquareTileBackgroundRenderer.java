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
    private final MapLayerRenderer mapView;

    protected void paintBufferRectangle(int x, int y, int width, int height) {
        Graphics gg = bg.create();
        gg.setClip(x, y, width, height);
        gg.translate(-bufferRect.x, -bufferRect.y);
        mapView.paintRect(gg, bufferRect);
    }

    public SquareTileBackgroundRenderer(MapLayerRenderer mv, float _scale) {
        if (null == mv) {
            throw new NullPointerException();
        }

        this.mapView = mv;
    }

    public void paintTile(Graphics g, int tileX, int tileY) {
        mapView.paintTile(g, tileX, tileY);
    }

    public void refreshTile(int x, int y) {
        //The backgroundBuffer gets created on the first call to backgroundBuffer.paintRect(..)
        //so we need a check here to avoid a null pointer exception.
        if (null != super.backgroundBuffer) {
            Graphics gg = bg.create();
            gg.translate(-bufferRect.x, -bufferRect.y);
            mapView.paintTile(gg, x, y);
        }
    }
}