/*
* MapView.java
*
* Created on 01 August 2001, 06:16
*/
package jfreerails.client.renderer;

import java.awt.Graphics;
import java.awt.Rectangle;


/**
*
* @author  Luke Lindsay
*/
public interface MapLayerRenderer {
    void paintTile(Graphics g, int tileX, int tileY);

    void refreshTile(int x, int y);

    void paintRect(Graphics g, Rectangle visibleRect);
}