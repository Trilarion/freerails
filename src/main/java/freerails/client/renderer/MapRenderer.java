package freerails.client.renderer;

import java.awt.*;

/**
 * Lets the GUI component that is displaying the map known the scale at which
 * the map is being rendered.
 *
 * @author Luke
 */
public interface MapRenderer extends MapLayerRenderer {

    /**
     *
     * @return
     */
    float getScale();

    /**
     *
     * @return
     */
    Dimension getMapSizeInPixels();
}