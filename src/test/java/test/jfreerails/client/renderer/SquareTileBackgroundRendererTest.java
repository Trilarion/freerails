/*
 * Created on 05-Dec-2005
 * 
 */
package jfreerails.client.renderer;

import java.awt.Graphics;
import java.awt.Rectangle;

import junit.framework.TestCase;

public class SquareTileBackgroundRendererTest extends TestCase {

    MapLayerRenderer renderer = new MapLayerRenderer() {

        public void paintTile(Graphics g, int tileX, int tileY) {

        }

        public void refreshTile(int x, int y) {

        }

        public void refreshAll() {

        }

        public void paintRect(Graphics g, Rectangle visibleRect) {

        }

    };

    /** Testcase to reproduce bug [ 1303162 ] Unexpected Exception: */
    public void testRefreshBeforeBufferIsSet() {
        SquareTileBackgroundRenderer stbr = new SquareTileBackgroundRenderer(
                renderer);
        stbr.refreshAll();
        stbr.refreshTile(1, 2);
    }

}
