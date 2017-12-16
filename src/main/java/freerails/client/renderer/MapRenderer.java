package freerails.client.renderer;

import java.awt.Dimension;

/**
 * Lets the GUI component that is displaying the map known the scale at which
 * the map is being rendered.
 * 
 * @author Luke
 */
public interface MapRenderer extends MapLayerRenderer {
    float getScale();

    Dimension getMapSizeInPixels();
}