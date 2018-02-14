/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
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
import java.awt.image.VolatileImage;

/**
 * This abstract class stores a buffer of the background of the current visible
 * rectangle of the map. Code that is independent of how tiles are represented,
 * e.g. whether they are square or isometric, should go here.
 */
public abstract class BufferedTiledBackgroundRenderer implements MapLayerRenderer {

    /**
     * This is used to create images that are compatible with the default
     * graphics configuration. Such images can be drawn to the screen quickly
     * since no conversion is needed.
     */
    private final GraphicsConfiguration defaultConfig = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    /**
     * The bounds and location of the map region that is stored in the offscreen
     * Image backgroundBuffer.
     */
    public final Rectangle bufferRect = new Rectangle();
    /**
     * Used to draw on the backbuffer.
     */
    public Graphics bg;
    /**
     * An offscreen image storing the background of a region of the map.
     */
    public VolatileImage backgroundBuffer;
    /**
     * Used to draw on the backbuffer. It is translated so that to its users, it
     * appears they are drawing on the actual map, not a buffered region of the
     * map.
     *
     * translatedBg equals bg.translate(-bufferRect.x , -bufferRect.y);
     */
    private Graphics translatedBg;

    /**
     * Updates the backbuffer as necessary, then draws it on to the Graphics
     * object passed.
     *
     * @param g           Once it has been updated, the backbuffer is drawn onto this
     *                    Graphics object.
     * @param visibleRect The region of the map that the backbuffer must be updated to
     *                    display.
     */
    public void paintRect(Graphics g, Rectangle visibleRect) {
        boolean contentsLost;
        do {
            /*
             * If this is the first call to the paint method or the component
             * has just been resized, we need to create a new backgroundBuffer.
             */
            if ((backgroundBuffer == null) || (visibleRect.height != bufferRect.height) || (visibleRect.width != bufferRect.width)) {
                setbackgroundBuffer(visibleRect.width, visibleRect.height);
            }

            // Test if image is lost and restore it.
            int valCode = backgroundBuffer.validate(defaultConfig);

            if (valCode == VolatileImage.IMAGE_INCOMPATIBLE) {
                setbackgroundBuffer(visibleRect.width, visibleRect.height);
            } else if (valCode == VolatileImage.IMAGE_RESTORED) {
                refreshBackground();
            }

            /*
             * Has the VisibleRectangle moved since the last paint?
             */
            if ((bufferRect.x != visibleRect.x) || (bufferRect.y != visibleRect.y)) {
                int dx = bufferRect.x - visibleRect.x;
                int dy = bufferRect.y - visibleRect.y;
                scrollbackgroundBuffer(dx, dy);
                bufferRect.setBounds(visibleRect);
            }

            if ((bufferRect.width != visibleRect.width) && (bufferRect.height != visibleRect.height)) {
                paintBufferRectangle(visibleRect.x - bufferRect.x, visibleRect.y - bufferRect.y, visibleRect.width, visibleRect.height);
            }

            g.drawImage(backgroundBuffer, visibleRect.x, visibleRect.y, null);
            bufferRect.setBounds(visibleRect);
            contentsLost = backgroundBuffer.contentsLost();
        } while (contentsLost);
    }

    private void refreshBackground() {
        paintBufferRectangle(0, 0, bufferRect.width, bufferRect.height);
    }

    /**
     *
     */
    public void refreshAll() {
        refreshBackground();
    }

    private void setbackgroundBuffer(int w, int h) {
        // Releases VRAM used by backgroundBuffer.
        if (backgroundBuffer != null) {
            backgroundBuffer.flush();
        }

        // Create new backgroundBuffer.
        backgroundBuffer = defaultConfig.createCompatibleVolatileImage(w, h);
        bufferRect.height = backgroundBuffer.getHeight(null);
        bufferRect.width = backgroundBuffer.getWidth(null);

        if (bg != null) {
            bg.dispose();
        }

        bg = backgroundBuffer.getGraphics();

        if (translatedBg != null) {
            translatedBg.dispose();
        }

        translatedBg = bg.create();
        translatedBg.translate(-bufferRect.x, -bufferRect.y);
        bg.clearRect(0, 0, w, h);
        refreshBackground();
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     */
    protected abstract void paintBufferRectangle(int x, int y, int width, int height);

    private void scrollbackgroundBuffer(int dx, int dy) {
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