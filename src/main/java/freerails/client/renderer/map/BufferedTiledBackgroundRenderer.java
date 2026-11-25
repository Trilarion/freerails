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
 *  TiledBackgroundPainter.java
 *
 *  Created on 31 July 2001, 16:22
 */
package freerails.client.renderer.map;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Keeps an off-screen buffer of the background of the map region painted
 * last. When the next region overlaps it, the buffer is scrolled and only the
 * newly exposed strips are painted. Code that is independent of how tiles are
 * represented, e.g. whether they are square or isometric, should go here.
 * <p>
 * The buffer is a plain in-memory image. The map view copies the background
 * into an ARGB image for JavaFX anyway, so a screen-compatible or volatile
 * image would gain nothing.
 */
public abstract class BufferedTiledBackgroundRenderer implements MapLayerRenderer {

    /**
     * The bounds and location of the map region that is stored in the buffer.
     */
    private final Rectangle bufferRect = new Rectangle();
    /**
     * An off-screen image storing the background of a region of the map, or
     * {@code null} before the first call to {@link #paintRect(Graphics, Rectangle)}.
     */
    private BufferedImage backgroundBuffer;
    /**
     * Used to draw on the buffer.
     */
    private Graphics2D bg;

    /**
     * Updates the buffer as necessary, then draws it on to the Graphics object
     * passed.
     *
     * @param g           Once it has been updated, the buffer is drawn onto this
     *                    Graphics object.
     * @param visibleRect The region of the map that the buffer must be updated to
     *                    display.
     */
    @Override
    public void paintRect(Graphics g, Rectangle visibleRect) {
        boolean repaintBuffer = false;
        // On the first call, or when the size has changed, create a new buffer.
        if (backgroundBuffer == null || visibleRect.height != bufferRect.height || visibleRect.width != bufferRect.width) {
            setBackgroundBuffer(visibleRect);
            repaintBuffer = true;
        }

        if (!repaintBuffer && (bufferRect.x != visibleRect.x || bufferRect.y != visibleRect.y)) {
            int dx = bufferRect.x - visibleRect.x;
            int dy = bufferRect.y - visibleRect.y;
            if (Math.abs((long) dx) >= bufferRect.width
                    || Math.abs((long) dy) >= bufferRect.height) {
                bufferRect.setLocation(visibleRect.x, visibleRect.y);
                repaintBuffer = true;
            } else {
                scrollBackgroundBuffer(dx, dy);
            }
        }

        if (repaintBuffer) {
            refreshBackground();
        }

        g.drawImage(backgroundBuffer, visibleRect.x, visibleRect.y, null);
        bufferRect.setBounds(visibleRect);
    }

    /**
     * Repaints the whole buffer, if there is one yet.
     */
    private void refreshBackground() {
        if (hasBuffer()) {
            paintBufferRectangle(0, 0, bufferRect.width, bufferRect.height);
        }
    }

    /**
     * Called when the map has changed.
     */
    @Override
    public void refreshAll() {
        refreshBackground();
    }

    /**
     * Returns whether the buffer exists. It is created on the first call to
     * {@link #paintRect(Graphics, Rectangle)}; before that, refreshes have
     * nothing to update.
     *
     * @return whether the buffer exists
     */
    protected boolean hasBuffer() {
        return backgroundBuffer != null;
    }

    /**
     * Returns a new graphics context for drawing on the buffer, in buffer
     * coordinates. The caller must dispose it. Only valid if
     * {@link #hasBuffer()} is {@code true}.
     *
     * @return a graphics context for the buffer
     */
    protected Graphics createBufferGraphics() {
        return bg.create();
    }

    /**
     * Returns the map region that the buffer currently holds.
     *
     * @return a copy of the buffered map region
     */
    protected Rectangle getBufferRect() {
        return new Rectangle(bufferRect);
    }

    /**
     * Creates a new, cleared buffer for the specified map region.
     *
     * @param visibleRect the map bounds to buffer
     */
    private void setBackgroundBuffer(Rectangle visibleRect) {
        if (bg != null) {
            bg.dispose();
        }

        backgroundBuffer = new BufferedImage(
                Math.max(1, visibleRect.width), Math.max(1, visibleRect.height), BufferedImage.TYPE_INT_RGB);
        bufferRect.setBounds(visibleRect.x, visibleRect.y,
                backgroundBuffer.getWidth(), backgroundBuffer.getHeight());

        bg = backgroundBuffer.createGraphics();
        bg.clearRect(0, 0, bufferRect.width, bufferRect.height);
    }

    /**
     * Paints a part of the buffer from the map. Only called while the buffer
     * exists.
     *
     * @param x the horizontal buffer coordinate
     * @param y the vertical buffer coordinate
     * @param width the width of the part to paint
     * @param height the height of the part to paint
     */
    protected abstract void paintBufferRectangle(int x, int y, int width, int height);

    /**
     * Scrolls the buffer by the specified offset and paints the exposed strips.
     *
     * @param dx the horizontal offset
     * @param dy the vertical offset
     */
    private void scrollBackgroundBuffer(int dx, int dy) {
        int copyWidth = bufferRect.width;
        int copyHeight = bufferRect.height;
        int copySourceX = 0;
        int copySourceY = 0;

        if (dx > 0) {
            copyWidth -= dx;
        } else {
            copyWidth += dx;
            copySourceX = -dx;
        }

        if (dy > 0) {
            copyHeight -= dy;
        } else {
            copyHeight += dy;
            copySourceY = -dy;
        }

        bg.copyArea(copySourceX, copySourceY, copyWidth, copyHeight, dx, dy);
        bufferRect.x -= dx;
        bufferRect.y -= dy;

        // paint exposed areas
        if (dx != 0) {
            if (dx > 0) {
                bg.setClip(0, 0, dx, bufferRect.height);
                bg.clearRect(0, 0, dx, bufferRect.height);
                paintBufferRectangle(0, 0, dx, bufferRect.height);
            } else {
                bg.setClip(bufferRect.width + dx, 0, -dx, bufferRect.height);
                bg.clearRect(bufferRect.width + dx, 0, -dx, bufferRect.height);
                paintBufferRectangle(bufferRect.width + dx, 0, -dx, bufferRect.height);
            }
        }

        if (dy != 0) {
            if (dy > 0) {
                bg.setClip(0, 0, bufferRect.width, dy);
                bg.clearRect(0, 0, bufferRect.width, dy);
                paintBufferRectangle(0, 0, bufferRect.width, dy);
            } else {
                bg.setClip(0, bufferRect.height + dy, bufferRect.width, -dy);
                bg.clearRect(0, bufferRect.height + dy, bufferRect.width, -dy);
                paintBufferRectangle(0, bufferRect.height + dy, bufferRect.width, -dy);
            }
        }

        bg.setClip(0, 0, bufferRect.width, bufferRect.height);
    }
}
