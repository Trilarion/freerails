/*
* MapView.java
*
* Created on 01 August 2001, 06:16
*/
package jfreerails.client.renderer;

import java.awt.Graphics;
import java.awt.Rectangle;


/**
*  Paints a layer of the map which might be buffered.
* @author  Luke Lindsay
*/
public interface MapLayerRenderer {
    void paintTile(Graphics g, int tileX, int tileY);

    void refreshTile(int x, int y);

    void refreshAll();

    void paintRect(Graphics g, Rectangle visibleRect);
}