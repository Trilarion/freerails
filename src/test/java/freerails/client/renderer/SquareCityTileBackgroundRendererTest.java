/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package freerails.client.renderer;

import freerails.client.renderer.map.MapLayerRenderer;
import freerails.client.renderer.map.SquareTileBackgroundRenderer;
import freerails.util.IntPoint2D;
import junit.framework.TestCase;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Tests square city tile background renderer behavior.
 */
public class SquareCityTileBackgroundRendererTest extends TestCase {

    private final MapLayerRenderer renderer = new TestMapLayerRenderer();

    /**
     * Testcase to reproduce bug [ 1303162 ] Unexpected Exception:
     */
    public void testRefreshBeforeBufferIsSet() {
        SquareTileBackgroundRenderer renderer = new SquareTileBackgroundRenderer(this.renderer);
        renderer.refreshAll();
        renderer.refreshTile(new IntPoint2D(1, 2));
    }

    /**
     * Tests that scrolling the buffer by less than its size, in every direction, gives the same
     * pixels as painting the new region from scratch.
     */
    public void testScrolledBufferMatchesFreshPaint() {
        Rectangle start = new Rectangle(20, 15, 40, 30);
        int[][] offsets = {{7, 5}, {-7, -5}, {13, 0}, {0, -11}, {-3, 9}};
        for (int[] offset : offsets) {
            SquareTileBackgroundRenderer scrolled = new SquareTileBackgroundRenderer(new PatternLayer());
            render(scrolled, start);
            Rectangle moved = new Rectangle(start.x + offset[0], start.y + offset[1], start.width, start.height);

            assertSamePixels("offset " + offset[0] + "," + offset[1],
                    render(new SquareTileBackgroundRenderer(new PatternLayer()), moved),
                    render(scrolled, moved));
        }
    }

    /**
     * Tests that a jump by more than the buffer size repaints the whole buffer.
     */
    public void testJumpRepaintsWholeBuffer() {
        SquareTileBackgroundRenderer jumped = new SquareTileBackgroundRenderer(new PatternLayer());
        render(jumped, new Rectangle(0, 0, 40, 30));
        Rectangle far = new Rectangle(100, 80, 40, 30);

        assertSamePixels("jump",
                render(new SquareTileBackgroundRenderer(new PatternLayer()), far), render(jumped, far));
    }

    /**
     * Tests that a refreshed tile is repainted in the buffer.
     */
    public void testRefreshTileUpdatesBuffer() {
        PatternLayer layer = new PatternLayer();
        SquareTileBackgroundRenderer renderer = new SquareTileBackgroundRenderer(layer);
        Rectangle region = new Rectangle(0, 0, 40, 30);
        render(renderer, region);

        layer.changed = new IntPoint2D(1, 2);
        renderer.refreshTile(layer.changed);
        BufferedImage image = render(renderer, region);

        int x = layer.changed.x() * PatternLayer.TILE + 1;
        int y = layer.changed.y() * PatternLayer.TILE + 1;
        assertEquals(PatternLayer.CHANGED.getRGB(), image.getRGB(x, y));
        assertEquals(PatternLayer.colorOf(0, 0).getRGB(), image.getRGB(1, 1));
    }

    /**
     * Paints a map region through the renderer into a new image of the region's size.
     *
     * @param renderer the renderer to paint with
     * @param region the map region
     * @return the painted image
     */
    private static BufferedImage render(SquareTileBackgroundRenderer renderer, Rectangle region) {
        BufferedImage image = new BufferedImage(region.width, region.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.translate(-region.x, -region.y);
            renderer.paintRect(g, region);
        } finally {
            g.dispose();
        }
        return image;
    }

    /**
     * Asserts that two images have the same size and pixels.
     *
     * @param message the failure message prefix
     * @param expected the expected image
     * @param actual the actual image
     */
    private static void assertSamePixels(String message, BufferedImage expected, BufferedImage actual) {
        assertEquals(message, expected.getWidth(), actual.getWidth());
        assertEquals(message, expected.getHeight(), actual.getHeight());
        for (int x = 0; x < expected.getWidth(); x++) {
            for (int y = 0; y < expected.getHeight(); y++) {
                assertEquals(message + " at " + x + "," + y, expected.getRGB(x, y), actual.getRGB(x, y));
            }
        }
    }

    /**
     * A map layer that paints every tile in a color derived from its map position, so that
     * misplaced pixels show up. One tile can be marked as changed.
     */
    private static final class PatternLayer implements MapLayerRenderer {

        static final int TILE = 10;
        static final Color CHANGED = new Color(255, 0, 255);
        IntPoint2D changed;

        static Color colorOf(int tileX, int tileY) {
            return new Color(Math.floorMod(tileX * 37, 256), Math.floorMod(tileY * 53, 256), 128);
        }

        @Override
        public void paintTile(Graphics g, IntPoint2D tileLocation) {
            g.setColor(tileLocation.equals(changed) ? CHANGED : colorOf(tileLocation.x(), tileLocation.y()));
            g.fillRect(tileLocation.x() * TILE, tileLocation.y() * TILE, TILE, TILE);
        }

        @Override
        public void refreshTile(IntPoint2D tileLocation) {}

        @Override
        public void refreshAll() {}

        @Override
        public void paintRect(Graphics g, Rectangle visibleRect) {
            Rectangle area = g.getClipBounds() == null ? visibleRect : g.getClipBounds();
            int firstX = Math.floorDiv(area.x, TILE);
            int firstY = Math.floorDiv(area.y, TILE);
            int lastX = Math.floorDiv(area.x + area.width - 1, TILE);
            int lastY = Math.floorDiv(area.y + area.height - 1, TILE);
            for (int tileX = firstX; tileX <= lastX; tileX++) {
                for (int tileY = firstY; tileY <= lastY; tileY++) {
                    paintTile(g, new IntPoint2D(tileX, tileY));
                }
            }
        }
    }
}
