package freerails.client.common;

import java.awt.*;

/**
 * Paints a layer of the map view.
 *
 * @author Luke
 */
public interface Painter {
    void paint(Graphics2D g, Rectangle newVisibleRectectangle);
}