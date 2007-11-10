package jfreerails.client.common;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Paints a layer of the map view.
 * 
 * @author Luke
 */
public interface Painter {
    void paint(Graphics2D g, Rectangle newVisibleRectectangle);
}