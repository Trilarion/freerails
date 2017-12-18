/*
 * MapView.java
 *
 * Created on 01 August 2001, 06:16
 */
package freerails.client.renderer;

import java.awt.*;

/**
 * Paints a layer of the map which might be buffered.
 *
 * @author Luke Lindsay
 */
public interface MapLayerRenderer {

    /**
     *
     * @param g
     * @param tileX
     * @param tileY
     */
    void paintTile(Graphics g, int tileX, int tileY);

    /**
     *
     * @param x
     * @param y
     */
    void refreshTile(int x, int y);

    /**
     *
     */
    void refreshAll();

    /**
     *
     * @param g
     * @param visibleRect
     */
    void paintRect(Graphics g, Rectangle visibleRect);
}