package freerails.client.common;

import java.awt.*;

/**
 * Paints a layer of the map view.
 *
 */
public interface Painter {

    /**
     *
     * @param g
     * @param newVisibleRectectangle
     */
    void paint(Graphics2D g, Rectangle newVisibleRectectangle);
}