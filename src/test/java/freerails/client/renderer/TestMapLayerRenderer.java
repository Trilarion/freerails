package freerails.client.renderer;

import freerails.client.renderer.map.MapLayerRenderer;
import freerails.util.Vector2D;

import java.awt.*;

/**
 *
 */
public class TestMapLayerRenderer implements MapLayerRenderer {
    public void paintTile(Graphics g, Vector2D tileLocation) {}

    public void refreshTile(Vector2D tileLocation) {}

    public void refreshAll() {}

    public void paintRect(Graphics g, Rectangle visibleRect) {}
}
