package jfreerails.client.renderer;

import java.awt.Dimension;


public interface MapRenderer extends MapLayerRenderer {
    float getScale();

    Dimension getMapSizeInPixels();
}