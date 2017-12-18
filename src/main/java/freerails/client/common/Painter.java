package freerails.client.common;

import java.awt.*;

/**
 * Paints a layer of the map view.
 *
 * @author Luke
 */
public interface Painter {

    /**
     *
     * @param g
     * @param newVisibleRectectangle
     */
    void paint(Graphics2D g, Rectangle newVisibleRectectangle);
}