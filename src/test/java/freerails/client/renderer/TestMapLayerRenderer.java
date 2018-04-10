package freerails.client.renderer;

import freerails.client.renderer.map.MapLayerRenderer;
import freerails.util.Vec2D;

import java.awt.*;

/**
 *
 */
public class TestMapLayerRenderer implements MapLayerRenderer {
    public void paintTile(Graphics g, Vec2D tileLocation) {}

    public void refreshTile(Vec2D tileLocation) {}

    public void refreshAll() {}

    public void paintRect(Graphics g, Rectangle visibleRect) {}
}
