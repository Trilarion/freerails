package freerails.client.renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Used for testing the Map view components without setting up any map data.
 * 
 * @author Luke
 */
public class BlankMapRenderer implements MapRenderer {
    private final float scale;

    public BlankMapRenderer(float s) {
        scale = s;
    }

    public float getScale() {
        return scale;
    }

    public Dimension getMapSizeInPixels() {
        int height = (int) (400 * scale);
        int width = (int) (400 * scale);

        return new Dimension(height, width);
    }

    public void paintTile(Graphics g, int tileX, int tileY) {
        paintRect(g, null);
    }

    public void refreshTile(int x, int y) {
    }

    public void paintRect(Graphics g, Rectangle visibleRect) {
        g.setColor(Color.darkGray);
        g.fillRect(0, 0, (int) (scale * 400), (int) (scale * 400));
        g.setColor(Color.blue);

        int x = (int) (100 * scale);
        int y = (int) (100 * scale);
        int height = (int) (200 * scale);
        int width = (int) (200 * scale);
        g.fillRect(x, y, height, width);
    }

    public void refreshAll() {
        // do nothing
    }
}