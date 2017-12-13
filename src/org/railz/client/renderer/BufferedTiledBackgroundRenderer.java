/*
 * Copyright (C) 2001 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 *  TiledBackgroundPainter.java
 *
 *  Created on 31 July 2001, 16:22
 */
package org.railz.client.renderer;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.VolatileImage;


/**
 *  This abstract class stores a buffer of the backgound of the current visible
 *  rectangle of the map. Code that is independent of how tiles are represented,
 *  e.g. whether they are square or isometric, should go here.
 *
 *@author     Luke Lindsay
 *     06 October 2001
 *@version    1.0
 *
 */
public abstract class BufferedTiledBackgroundRenderer
    implements MapLayerRenderer {
    /**
     *  This is used to create images that are compatible with the default
     *  graphics configuration. Such images can be drawn to the screen quickly
     *  since no conversion is needed.
     */
    protected GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                                              .getDefaultScreenDevice()
                                                                              .getDefaultConfiguration();

    /**
     *  Used to draw on the backbuffer
     */
    protected Graphics bg;

    /**
     *  The bounds and location of the map region that is stored in the
     *  offscreen Image backgraoundBuffer
     */
    protected Rectangle bufferRect = new Rectangle();

    /**
     *  An offscreen image storing the background of a region of the map
     */
    protected VolatileImage backgroundBuffer;

    /**
     *  Updates the backbuffer as necessay, then draws it on to the Graphics
     *  object passed.
     *
     *@param  outputGraphics          Once it has been updated, the backbuffer
     *      is drawn onto this Graphics object.
     *@param  newVisibleRectectangle  The region of the map that the backbuffer
     *      must be updated to display.
     */
    public void paintRect(Graphics outputGraphics,
        Rectangle newVisibleRectectangle) {
        int iterations = 0;

        do {
            iterations++;

            /*
             *  If this is the first call to the paint method or the component has just been resized,
             *  we need to create a new backgroundBuffer.
             */
            if ((backgroundBuffer == null) ||
                    (newVisibleRectectangle.height != bufferRect.height) ||
                    (newVisibleRectectangle.width != bufferRect.width)) {
                setbackgroundBuffer(newVisibleRectectangle.width,
                    newVisibleRectectangle.height);
            }

            //	Test if image is lost and restore it.
            int valCode = backgroundBuffer.validate(defaultConfiguration);

            // No need to check for IMAGE_RESTORED since we are
            // going to re-render the image anyway.
            if (valCode == VolatileImage.IMAGE_INCOMPATIBLE) {
                setbackgroundBuffer(newVisibleRectectangle.width,
                    newVisibleRectectangle.height);
            }

            /*
             *  Has the VisibleRectangle moved since the last paint?
             */
            if ((bufferRect.x != newVisibleRectectangle.x) ||
                    (bufferRect.y != newVisibleRectectangle.y)) {
                int dx = bufferRect.x - newVisibleRectectangle.x;
                int dy = bufferRect.y - newVisibleRectectangle.y;
                scrollbackgroundBuffer(dx, dy);
                bufferRect.setBounds(newVisibleRectectangle);
            }

            if ((bufferRect.width != newVisibleRectectangle.width) &&
                    (bufferRect.height != newVisibleRectectangle.height)) {
                paintBufferRectangle(newVisibleRectectangle.x - bufferRect.x,
                    newVisibleRectectangle.y - bufferRect.y,
                    newVisibleRectectangle.width, newVisibleRectectangle.height);
            }

            outputGraphics.drawImage(backgroundBuffer,
                newVisibleRectectangle.x, newVisibleRectectangle.y, null);
            bufferRect.setBounds(newVisibleRectectangle);
        } while (backgroundBuffer.contentsLost());
    }

    protected void refreshBackground() {
        paintBufferRectangle(0, 0, bufferRect.width, bufferRect.height);
    }

    protected void setbackgroundBuffer(int w, int h) {
        //backgroundBuffer = defaultConfiguration.createCompatibleImage(w, h);
	if (backgroundBuffer != null) {
	    backgroundBuffer.flush();
	}
        backgroundBuffer = defaultConfiguration.createCompatibleVolatileImage(w,
                h);
        bufferRect.height = backgroundBuffer.getHeight(null);
        bufferRect.width = backgroundBuffer.getWidth(null);

        if (bg != null) {
            bg.dispose();
        }

        bg = backgroundBuffer.getGraphics();
        bg.clearRect(0, 0, w, h);
        refreshBackground();
    }

    protected abstract void paintBufferRectangle(int x, int y, int width,
        int height);

    /**
     *  Description of the Method
     *
     *@param  dx  Description of Parameter
     *@param  dy  Description of Parameter
     */
    protected void scrollbackgroundBuffer(int dx, int dy) {
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
                paintBufferRectangle(bufferRect.width + dx, 0, -dx,
                    bufferRect.height);
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
                paintBufferRectangle(0, bufferRect.height + dy,
                    bufferRect.width, -dy);
            }
        }

        bg.setClip(0, 0, bufferRect.width, bufferRect.height);
    }
}
